package com.practicum.playlistmaker.player.ui.view

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.utils.DateFormatter

/**
 * ViewHolder для отображения трека в режиме детального просмотра (аудиоплеер).
 * Отображает полную информацию о треке: обложку, название, исполнителя, длительность,
 * дату релиза, жанр, страну, а также кнопки управления воспроизведением и действиями.
 *
 * @param itemView корневой View элемента RecyclerView
 * @param onClickListener обработчик клика по всему элементу трека
 * @param onPlayButtonClick обработчик нажатия кнопки воспроизведения/паузы
 * @param onAddToPlaylistClick обработчик добавления трека в плейлист
 * @param onFavoriteClick обработчик отметки трека как избранного
 * @param formatDurationUseCase use case для форматирования длительности трека
 */
class AlbumViewHolder(
    itemView: View,
    private val onClickListener: (Track) -> Unit,
    private val onPlayButtonClick: (Track) -> Unit,
    private val onAddToPlaylistClick: (Track) -> Unit,
    private val onFavoriteClick: (Track) -> Unit,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.ViewHolder(itemView) {

    // UI‑компоненты элемента
    private val albumImageView: ImageView = itemView.findViewById(R.id.album)
    private val textTrackName: TextView = itemView.findViewById(R.id.textTrackName)
    private val textArtistName: TextView = itemView.findViewById(R.id.textArtistName)
    private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    private val trackTimeMillisTextView: TextView = itemView.findViewById(R.id.trackTimeMillis)
    private val collectionNameTextView: TextView = itemView.findViewById(R.id.collectionName)
    private val releaseDateTextView: TextView = itemView.findViewById(R.id.releaseDate)
    private val primaryGenreNameTextView: TextView = itemView.findViewById(R.id.primaryGenreName)
    private val countryTextView: TextView = itemView.findViewById(R.id.country)
    private val playButton: ImageButton = itemView.findViewById(R.id.ic_play_button)
    private val plusButton: Button = itemView.findViewById(R.id.ic_button_plus)
    private val likeButton: Button = itemView.findViewById(R.id.ic_button_like)

    /** Утилита для форматирования даты релиза */
    private val dateFormatter = DateFormatter()

    /** Текущий трек, привязанный к этому ViewHolder */
    private var currentTrack: Track? = null

    /**
     * Привязывает данные трека к UI‑элементам и обновляет состояние кнопки воспроизведения.
     * Загружает обложку через Glide с закруглёнными углами.
     *
     * @param track данные трека для отображения
     * @param isPlaying флаг, указывающий, воспроизводится ли трек сейчас
     * @param currentTimeMillis текущая позиция воспроизведения в миллисекундах
     * @param formattedTime форматированное время воспроизведения (например, «03:45»)
     */
    fun bind(
        track: Track,
        isPlaying: Boolean,
        currentTimeMillis: Long = 0,
        formattedTime: String = "00:00"
    ) {
        currentTrack = track

        // Заполняем текстовые поля данными трека
        textTrackName.text = track.trackName
        textArtistName.text = track.artistName
        releaseDateTextView.text = dateFormatter.formatReleaseDate(track.releaseDate)
        collectionNameTextView.text = track.collectionName
        primaryGenreNameTextView.text = track.primaryGenreName
        countryTextView.text = track.country

        // Отображаем текущее время воспроизведения и общую длительность трека
        timeTextView.text = formattedTime
        trackTimeMillisTextView.text = track.trackTimeMillis?.let {
            formatDurationUseCase.invoke(it)
        } ?: ""

        // Загружаем обложку альбома через Glide
        val cornerRadiusPx = (8 * itemView.resources.displayMetrics.density).toInt()
        if (track.artworkUrl100 != null) {
            Glide.with(itemView.context)
                .load(track.getHighQualityArtworkUrl())
                .placeholder(R.drawable.ic_placeholder_312)
                .error(R.drawable.ic_placeholder_312)
                .centerCrop()
                .transform(RoundedCorners(cornerRadiusPx))
                .into(albumImageView)
        } else {
            albumImageView.setImageResource(R.drawable.ic_placeholder_312)
        }

        updatePlayButtonState(isPlaying)
        setupClickListeners(track)
    }

    /**
     * Настраивает обработчики кликов для всех интерактивных элементов ViewHolder.
     * Обработчики делегируют события наружу через лямбды, переданные в конструктор.
     *
     * @param track текущий трек для передачи в обработчики
     */
    private fun setupClickListeners(track: Track) {
        itemView.setOnClickListener { onClickListener(track) }
        playButton.setOnClickListener { onPlayButtonClick(track) }
        plusButton.setOnClickListener { onAddToPlaylistClick(track) }
        likeButton.setOnClickListener { onFavoriteClick(track) }
    }

    /**
     * Обновляет иконку кнопки воспроизведения в зависимости от состояния воспроизведения.
     * Показывает иконку паузы, если трек играет, или иконку воспроизведения, если на паузе.
     * Логирует изменение состояния для отладки.
     *
     * @param isPlaying флаг воспроизведения (играет/пауза)
     */
    fun updatePlayButtonState(isPlaying: Boolean) {
        Log.d("AlbumViewHolder", "updatePlayButtonState: isPlaying=$isPlaying")
        playButton.setImageResource(
            if (isPlaying) R.drawable.ic_pause_button
            else R.drawable.ic_play_button
        )
    }
    fun updateCurrentTime(formattedTime: String) {
        timeTextView.text = formattedTime
    }

}