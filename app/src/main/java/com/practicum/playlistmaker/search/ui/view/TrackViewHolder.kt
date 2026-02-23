package com.practicum.playlistmaker.search.ui.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.core.contract.FormatTrackDurationUseCaseContract
import com.practicum.playlistmaker.core.models.Track

/**
 * ViewHolder для отображения трека в компактном режиме списка.
 * Показывает основную информацию: обложку, название трека, исполнителя и длительность.
 * Поддерживает отображение текущего времени воспроизведения при активном воспроизведении.
 *
 * @param itemView корневой View элемента RecyclerView
 * @param formatDurationUseCase use case для форматирования длительности трека
 */
class TrackViewHolder(
    itemView: View,
    private val formatDurationUseCase: FormatTrackDurationUseCaseContract
) : RecyclerView.ViewHolder(itemView) {

    // UI‑компоненты элемента
    private val artworkImageView: ImageView = itemView.findViewById(R.id.artwork_image)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)

    /** Текущий трек, привязанный к этому ViewHolder */
    private var track: Track? = null

    /**
     * Привязывает данные трека к UI‑элементам.
     * - заполняет текстовые поля (название, исполнитель, длительность);
     * - загружает обложку через Glide с закруглёнными углами.
     *
     * @param track данные трека для отображения
     */
    fun bind(track: Track) {
        this.track = track

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

        // Отображаем длительность трека или оставляем поле пустым, если длительность недоступна
        track.trackTimeMillis?.let { timeMillis ->
            trackTimeTextView.text = formatDurationUseCase.invoke(timeMillis)
        } ?: run {
            trackTimeTextView.text = ""
        }

        // Загружаем обложку альбома через Glide
        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder_45)
            .error(R.drawable.ic_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(2))
            .into(artworkImageView)
    }

    /**
     * Обновляет отображение времени трека на текущее время воспроизведения.
     * Используется, когда трек воспроизводится и нужно показать прогресс.
     * Берёт готовое форматированное время из состояния ViewModel.
     *
     * @param isPlaying флаг воспроизведения (играет/пауза) — не используется в методе,
     *   но оставлен для совместимости с интерфейсом
     * @param currentTimeMillis текущая позиция воспроизведения в миллисекундах — не используется,
     *   так как используется готовое форматированное время
     * @param formattedTime форматированное время воспроизведения
     */
    fun showPlayingState(isPlaying: Boolean, currentTimeMillis: Long, formattedTime: String) {
        trackTimeTextView.text = formattedTime
    }

    /**
     * Восстанавливает отображение исходной длительности трека.
     * Вызывается, когда трек перестаёт воспроизводиться или не является текущим.
     * Если длительность трека доступна, форматирует и отображает её; иначе — очищает поле.
     */
    fun hidePlayingState() {
        track?.trackTimeMillis?.let { timeMillis ->
            trackTimeTextView.text = formatDurationUseCase.invoke(timeMillis)
        } ?: run {
            trackTimeTextView.text = ""
        }
    }
}