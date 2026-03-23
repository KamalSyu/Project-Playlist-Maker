package com.practicum.playlistmaker.player.ui

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Фрагмент аудиоплеера: воспроизведение трека, отображение прогресса, управление воспроизведением.
 */
class AudioPlayerFragment : Fragment() {

    private val viewModel: AudioPlayerViewModel by viewModel()

    // UI‑компоненты
    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: PlayerTrackAdapter

    // Для периодического обновления прогресса воспроизведения
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // Трек, который воспроизводится
    private lateinit var track: Track

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_audio_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем отступы под системные панели
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }

        track = getTrackFromIntent() // Получаем трек из интента
        viewModel.setCurrentTrack(track) // Передаём данные в ViewModel
        setupRecyclerView(track) // Настраиваем UI в фрагменте

        // Подписываемся на изменения состояния UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }

        viewModel.setupPlaybackCompletionListener()

        if (savedInstanceState == null) {
            viewModel.initPlayback(track.previewUrl)
        } else {
            // Восстановление состояния после поворота экрана
            val isPlaying = savedInstanceState.getBoolean("isPlaying")
            val savedPosition = savedInstanceState.getLong("savedPosition")
            viewModel.restorePlaybackState(isPlaying, savedPosition)
            if (isPlaying) startPolling()
        }
    }

    /** Получаем трек из интента и преобразуем в доменную модель */
    private fun getTrackFromIntent(): Track {
        // Сначала пробуем получить из аргументов фрагмента
        val parcelableTrack: ParcelableTrack? = arguments?.getParcelable("track")

        // Если нет аргументов, пробуем получить из интента (для обратной совместимости)
        val fromIntent: ParcelableTrack? = requireActivity().intent.getParcelableExtra("track")

        val finalTrack = parcelableTrack ?: fromIntent

        if (finalTrack == null) {
            throw IllegalArgumentException("Track is required but not provided.")
        }

        return viewModel.processTrack(finalTrack)
    }

    /** Инициализация RecyclerView с адаптером */
    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = requireView().findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(requireContext())

        adapter = PlayerTrackAdapter(
            tracks = listOf(track),
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = { track ->
                // TODO: реализовать добавление в плейлист
                Log.d("AudioPlayer", "Add to playlist: ${track.trackName}")
            },
            onFavorite = { track ->
                // TODO: реализовать отметку избранного
                Log.d("AudioPlayer", "Favorite: ${track.trackName}")
            },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter
    }

    /** Переключение воспроизведения (старт/пауза) */
    private fun togglePlayback() {
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        val resumePosition = if (!currentState.isPlaying && currentState.position > 0L) currentState.position else null
        viewModel.togglePlayback(resumePosition)
    }

    /** Обновление UI на основе текущего состояния */
    private fun updateUI(state: PlayerUiState) {
        if (state.error != null) {
            handlePlaybackError("Ошибка воспроизведения", state.error)
            viewModel.clearError()
            return
        }

        // Обновляем состояние адаптера — это вызовет bind() в PlayerViewHolder,
        // который автоматически обновит иконку кнопки воспроизведения
        adapter.notifyDataSetChangedWithState(
            isPlaying = state.playbackState.isPlaying,
            currentTimeMillis = state.playbackState.position,
            position = 0,
            formattedTime = state.formattedTime
        )

        // Запускаем/останавливаем опрос прогресса
        if (state.shouldPoll && state.playbackState.isPlaying) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    /** Запуск периодического опроса прогресса воспроизведения (каждую секунду) */
    private fun startPolling() {
        stopPolling()

        updateRunnable = Runnable {
            viewModel.updateCurrentPosition()

            val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
            if (currentState.isPlaying) {
                handler.postDelayed(updateRunnable!!, 1000)
            } else {
                stopPolling()
            }
        }
        handler.post(updateRunnable!!)
    }

    /** Останавливаем опрос прогресса */
    private fun stopPolling() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    /** Сохраняем состояние при повороте экрана */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        outState.putBoolean("isPlaying", currentState.isPlaying)
        outState.putLong("savedPosition", currentState.position)
    }

    /** Приостанавливаем воспроизведение при уходе в фон */
    override fun onPause() {
        super.onPause()
        val currentState = viewModel.uiState.value?.playbackState ?: PlaybackState(false, 0L)
        if (currentState.isPlaying) {
            viewModel.saveCurrentPosition()
            viewModel.stopPlayback()
        }
        stopPolling()
    }

    /** Очистка при остановке */
    override fun onStop() {
        super.onStop()
        stopPolling()
    }

    /** Полная очистка ресурсов при уничтожении */
    override fun onDestroyView() {
        super.onDestroyView()
        stopPolling()
        handler.removeCallbacksAndMessages(null)
    }

    /** Обновляем UI при возврате на экран */
    override fun onResume() {
        super.onResume()
        val currentState = viewModel.uiState.value ?: return
        updateUI(currentState)
        if (currentState.playbackState.isPlaying) startPolling()
    }

    /** Показать ошибку */
    private fun showError(message: String) {
        Log.e("AudioPlayerFragment", "Ошибка: $message")
    }

    /** Логируем и показываем ошибку воспроизведения */
    private fun handlePlaybackError(message: String, e: Throwable) {
        Log.e("AudioPlayerFragment", message, e)
        showError(message)
    }
}
