package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.main.ui.MainActivity
import com.practicum.playlistmaker.player.domain.model.PlaybackState
import com.practicum.playlistmaker.player.ui.adapter.PlayerTrackAdapter
import com.practicum.playlistmaker.player.ui.view.AudioPlayerViewModel
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Фрагмент аудиоплеера: воспроизведение трека, отображение прогресса, управление воспроизведением.
 */
class AudioPlayerFragment : Fragment() {

    private val viewModel: AudioPlayerViewModel by viewModel()

    // UI-компоненты
    private lateinit var recyclerViewAudioPlayer: RecyclerView
    private lateinit var adapter: PlayerTrackAdapter

    // Для отслеживания состояния воспроизведения
    private var lastKnownIsPlaying: Boolean = false
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

        // 1. Обработка нажатия экранной кнопки «Назад»
        val backButton: TextView = requireView().findViewById(R.id.back)
        backButton.setOnClickListener {
            handleBackPress()
        }

        // 2. Обработка аппаратной кнопки «Back»
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

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

            if (state.shouldPoll && state.playbackState.isPlaying) {
                viewModel.startProgressUpdates() // Запускаем обновление прогресса
            } else {
                viewModel.stopProgressUpdates() // Останавливаем
            }
        }

        viewModel.setupPlaybackCompletionListener()

        if (savedInstanceState == null) {
            viewModel.initPlayback(track.previewUrl)
        } else {
            // Восстановление состояния после поворота экрана
            val isPlaying = savedInstanceState.getBoolean("isPlaying")
            val savedPosition = savedInstanceState.getLong("savedPosition")
            viewModel.restorePlaybackState(isPlaying, savedPosition)
        }
    }

    /** Получаем трек из интента и преобразуем в доменную модель */
    private fun getTrackFromIntent(): Track {
        val parcelableTrack: ParcelableTrack = arguments?.getParcelable("track")
            ?: throw IllegalArgumentException("Track is required but not provided in arguments.")

        return viewModel.processTrack(parcelableTrack)
    }

    /** Инициализация RecyclerView с адаптером */
    private fun setupRecyclerView(track: Track) {
        recyclerViewAudioPlayer = requireView().findViewById(R.id.recyclerViewAudioPlayer)
        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(requireContext())

        adapter = PlayerTrackAdapter(
            tracks = listOf(track),
            onClickPlayButton = { _ -> togglePlayback() },
            onAddToPlaylist = { track ->
                Log.d("AudioPlayer", "Add to playlist: ${track.trackName}")
            },
            onFavorite = { track ->
                Log.d("AudioPlayer", "Favorite: ${track.trackName}")
            },
            formatDurationUseCase = viewModel.formatTrackDurationUseCase
        )
        recyclerViewAudioPlayer.adapter = adapter

        // Добавляем прослушиватель прокрутки
        recyclerViewAudioPlayer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val firstItem = layoutManager.findViewByPosition(firstVisibleItemPosition)

                // Условия для показа Toolbar:
                // 1. Первый элемент в зоне видимости (позиция 0 или -1)
                // 2. Прокрутка вверх (dy < -5 — порог чувствительности)
                // 3. Первый элемент частично или полностью виден
                if ((firstVisibleItemPosition <= 0) &&
                    dy < -5 &&
                    firstItem != null) {

                    // Проверяем, что первый элемент виден хотя бы на 50 %
                    val visibleHeight = firstItem.height - firstItem.top
                    if (visibleHeight > firstItem.height * 0.5f) {
                        showToolbarWithAutoHide()
                    }
                }
            }
        })
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

        // Обновляем ТОЛЬКО время — без перерисовки элемента
        adapter.updateCurrentTime(state.formattedTime)

        // Обновляем кнопку Play/Pause ТОЛЬКО если состояние изменилось
        val isPlaying = state.playbackState.isPlaying
        if (lastKnownIsPlaying != isPlaying) {
            adapter.notifyDataSetChangedWithState(
                isPlaying = isPlaying,
                currentTimeMillis = state.playbackState.position,
                position = 0,
                formattedTime = state.formattedTime
            )
        }
        lastKnownIsPlaying = isPlaying
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
    }

    /** Очистка при остановке */
    override fun onStop() {
        super.onStop()
    }

    /** Полная очистка ресурсов при уничтожении */
    override fun onDestroyView() {
        super.onDestroyView()
    }

    /** Обновляем UI при возврате на экран */
    override fun onResume() {
        super.onResume()
        val currentState = viewModel.uiState.value ?: return
        updateUI(currentState)
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

    private fun handleBackPress() {
        viewModel.resetPlaybackToStart()
        findNavController().popBackStack()
    }

    private fun showToolbarWithAutoHide() {
        val activity = requireActivity() as MainActivity
        val toolbar = activity.getToolbar()

        // 1. Назначаем Toolbar как ActionBar
        activity.setSupportActionBar(toolbar)

        // 2. Показываем Toolbar
        toolbar.visibility = View.VISIBLE
        activity.supportActionBar?.show()

        // 3. Логируем
        Log.d("AudioPlayerFragment", "Toolbar shown")

        // 4. Автоскрываем через 2 секунды
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000)
            if (isResumed) {
                toolbar.visibility = View.GONE
                activity.supportActionBar?.hide()
                Log.d("AudioPlayerFragment", "Toolbar hidden")
            }
        }
    }
}
