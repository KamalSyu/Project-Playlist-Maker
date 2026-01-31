//package com.practicum.playlistmaker.data.dto
//data class SearchResponseDTO(
//    val resultCount: Int,
//    val results: List<TrackDTO>
//)
//package com.practicum.playlistmaker.data.dto
//data class TrackDTO(
//    var trackName: String,
//    var artistName: String,
//    var trackTimeMillis: Long,
//    var artworkUrl100: String,
//    var releaseDate: String? = null,
//    var collectionName: String? = null,
//    var primaryGenreName: String? = null,
//    var country: String? = null,
//    var previewUrl: String? = null
//)
//package com.practicum.playlistmaker.data.mapper
//fun SearchResponseDTO.toDomain(): SearchResponse {
//    return SearchResponse(
//        resultCount = this.resultCount,
//        results = this.results.map { it.toDomain() }
//    )
//}
//fun TrackDTO.toDomain(): Track {
//    return Track(
//        trackName = this.trackName,
//        artistName = this.artistName,
//        trackTimeMillis = this.trackTimeMillis,
//        artworkUrl100 = this.artworkUrl100,
//        releaseDate = this.releaseDate,
//        collectionName = this.collectionName,
//        primaryGenreName = this.primaryGenreName,
//        country = this.country,
//        previewUrl = this.previewUrl
//    )
//}
//package com.practicum.playlistmaker.data.network
//interface ItunesApi {
//    @GET("search")
//    suspend fun searchTracks(
//        @Query("term") term: String,
//        @Query("media") media: String = "music",
//        @Query("entity") entity: String = "song"
//    ): SearchResponseDTO
//}
//package com.practicum.playlistmaker.data.repository
//class HistoryRepositoryImpl(
//    private val sharedPreferences: SharedPreferences
//) : HistoryRepository {
//    override suspend fun addTrack(track: Track) {
//        val history = getHistory().toMutableList()
//        history.removeIf { it.trackId == track.trackId }
//        history.add(0, track)
//        if (history.size > 10) {
//            history.subList(10, history.size).clear()
//        }
//        sharedPreferences.edit()
//            .putString("history", Gson().toJson(history))
//            .apply()
//    }
//    override suspend fun getHistory(): List<Track> {
//        val json = sharedPreferences.getString("history", "[]")
//        return Gson().fromJson(json, Array<Track>::class.java).toList()
//    }
//    override suspend fun clearHistory() {
//        sharedPreferences.edit().clear().apply()
//    }
//}
//package com.practicum.playlistmaker.data.repository
//class ItunesRepositoryImpl @Inject constructor(
//    private val api: ItunesApi
//) : ItunesRepository {
//    override suspend fun search(query: String): SearchResponse {
//        val response: SearchResponseDTO = api.searchTracks(query)
//        return response.toDomain()
//    }
//}
//package com.practicum.playlistmaker.data.repository
//@Singleton
//class PlayerRepositoryImpl @Inject constructor(): PlayerRepository {
//    private var mediaPlayer: MediaPlayer? = null // Экземпляр MediaPlayer для работы с аудио
//    private var completionListener: (() -> Unit)? = null  // Слушатель завершения воспроизведения
//    override suspend fun play() {
//        mediaPlayer?.start() }
//    override suspend fun pause() {
//        mediaPlayer?.pause() }
//    override suspend fun prepare(url: String?) {
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(url)
//            prepare()
//            setOnCompletionListener {
//                completionListener?.invoke()
//            }
//        }
//    }
//    override suspend fun stop() {
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//    }
//    override suspend fun reset() {
//        withContext(Dispatchers.Main) {
//            mediaPlayer?.seekTo(0)
//            mediaPlayer?.stop() // если нужно полностью остановить
//            mediaPlayer = null // Гарантируем, что isPlaying() вернёт false
//        }
//    }
//    override fun setOnCompletionListener(listener: () -> Unit) {
//        completionListener = listener
//        mediaPlayer?.setOnCompletionListener { listener() }
//    }
//    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
//    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L
//}
//package com.practicum.playlistmaker.data.repository
//class SettingsRepositoryImpl(
//    private val sharedPreferences: SharedPreferences
//) : SettingsRepository {
//    override fun saveTheme(isDarkMode: Boolean) {
//        sharedPreferences.edit().putBoolean("dark_theme", isDarkMode).apply()
//    }
//    override fun isDarkThemeEnabled(): Boolean {
//        return sharedPreferences.getBoolean("dark_theme", false)
//    }
//}
//package com.practicum.playlistmaker.di
//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//    @Provides
//    @Singleton
//    fun provideContext(app: Application): Context = app
//    @Provides
//    @Singleton
//    fun provideSharedPreferences(context: Context): SharedPreferences {
//        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//    }
//    @Provides
//    @Singleton
//    fun provideRetrofit(): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl("https://itunes.apple.com/") // Обратите внимание: возможно, нужен "https://"
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//    @Provides
//    @Singleton
//    fun provideItunesApi(retrofit: Retrofit): ItunesApi {
//        return retrofit.create(ItunesApi::class.java)
//    }
//    @Provides
//    @Singleton
//    fun provideItunesRepository(api: ItunesApi): ItunesRepository {
//        return ItunesRepositoryImpl(api)
//    }
//    @Provides
//    @Singleton
//    fun provideHistoryRepository(sharedPreferences: SharedPreferences): HistoryRepository {
//        return HistoryRepositoryImpl(sharedPreferences)
//    }
//    @Provides
//    @Singleton
//    fun providePlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository = impl
//    @Provides
//    @Singleton
//    fun provideSettingsRepository(sharedPreferences: SharedPreferences): SettingsRepository {
//        return SettingsRepositoryImpl(sharedPreferences)
//    }
//    @Provides
//    @Singleton
//    fun provideUseCaseCreator(
//        itunesRepository: ItunesRepository,
//        historyRepository: HistoryRepository,
//        playerRepository: PlayerRepository,
//        settingsRepository: SettingsRepository
//    ): UseCaseCreator {
//        return UseCaseCreator(itunesRepository, historyRepository, playerRepository, settingsRepository)
//    }
//    @Provides
//    fun provideSearchTracksUseCase(useCaseCreator: UseCaseCreator): SearchTracksUseCase {
//        return useCaseCreator.createSearchTracksUseCase()
//    }
//    @Provides
//    fun provideAddTrackToHistoryUseCase(useCaseCreator: UseCaseCreator): AddTrackToHistoryUseCase {
//        return useCaseCreator.createAddTrackToHistoryUseCase()
//    }
//    @Provides
//    fun provideGetSearchHistoryUseCase(
//        useCaseCreator: UseCaseCreator
//    ): GetSearchHistoryUseCase {
//        return useCaseCreator.createGetSearchHistoryUseCase()
//    }
//    @Provides
//    fun provideClearSearchHistoryUseCase(useCaseCreator: UseCaseCreator): ClearSearchHistoryUseCase {
//        return useCaseCreator.createClearSearchHistoryUseCase()
//    }
//    @Provides
//    fun provideFilterTracksUseCase(useCaseCreator: UseCaseCreator): FilterTracksUseCase {
//        return useCaseCreator.createFilterTracksUseCase()
//    }
//}
//package com.practicum.playlistmaker.domain.model
//data class SearchResponse(
//    val resultCount: Int,
//    val results: List<Track>
//)
//package com.practicum.playlistmaker.domain.model
//data class Track(
//    val trackId: String,
//    var trackName: String,
//    var artistName: String,
//    var trackTimeMillis: Long?,
//    var artworkUrl100: String?,
//    var releaseDate: String?,
//    var collectionName: String?,
//    var primaryGenreName: String?,
//    var country: String?,
//    var previewUrl: String?
//)  {
//    constructor(
//        trackName: String,
//        artistName: String,
//        trackTimeMillis: Long? = null,
//        artworkUrl100: String? = null,
//        releaseDate: String? = null,
//        collectionName: String? = null,
//        primaryGenreName: String? = null,
//        country: String? = null,
//        previewUrl: String? = null
//    ) : this(
//        trackId = "$trackName:$artistName:${collectionName ?: ""}".lowercase(),
//        trackName = trackName,
//        artistName = artistName,
//        trackTimeMillis = trackTimeMillis,
//        artworkUrl100 = artworkUrl100,
//        releaseDate = releaseDate,
//        collectionName = collectionName,
//        primaryGenreName = primaryGenreName,
//        country = country,
//        previewUrl = previewUrl
//    )
//    fun getHighQualityArtworkUrl(): String? {
//        return artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
//    }
//}
//package com.practicum.playlistmaker.domain.repository
//interface HistoryRepository {
//    suspend fun addTrack(track: Track)
//    suspend fun getHistory(): List<Track>
//    suspend fun clearHistory()
//}
//package com.practicum.playlistmaker.domain.repository
//interface ItunesRepository {
//    suspend fun search(query: String): SearchResponse
//}
//package com.practicum.playlistmaker.domain.repository
//interface PlayerRepository {
//    suspend fun prepare(url: String?)
//    suspend fun play()
//    suspend fun pause()
//    suspend fun stop()
//    suspend fun reset()
//    fun isPlaying(): Boolean
//    fun getCurrentPosition(): Long
//    fun setOnCompletionListener(listener: () -> Unit)
//}
//package com.practicum.playlistmaker.domain.repository
//interface SettingsRepository {
//    fun saveTheme(isDarkMode: Boolean)
//    fun isDarkThemeEnabled(): Boolean
//}
//package com.practicum.playlistmaker.domain.usecase
//class AddTrackToHistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) {
//    suspend operator fun invoke(track: Track) {
//        historyRepository.addTrack(track)
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class ClearSearchHistoryUseCase @Inject constructor (private val historyRepository: HistoryRepository) {
//    suspend operator fun invoke() {
//        historyRepository.clearHistory()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class FilterTracksUseCase @Inject constructor() {
//    operator fun invoke(tracks: List<Track>, query: String): List<Track> {
//        if (query.isEmpty()) return emptyList()
//        val lowerQuery = query.lowercase()
//        return tracks.filter { track ->
//            track.trackName.lowercase().contains(lowerQuery) ||
//                    track.artistName.lowercase().contains(lowerQuery)
//        }
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class FormatTrackDurationUseCase @Inject constructor() {
//    operator fun invoke(durationMillis: Long): String {
//        val minutes = durationMillis / 60_000
//        val seconds = (durationMillis % 60_000) / 1_000
//        return String.format("%02d:%02d", minutes, seconds)
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class GetCurrentPositionUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
//    operator fun invoke(): Long {
//        return playerRepository.getCurrentPosition()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class GetSearchHistoryUseCase @Inject constructor (private val historyRepository: HistoryRepository) {
//    suspend operator fun invoke(): List<Track> {
//        return historyRepository.getHistory()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class GetThemeStateUseCase @Inject constructor (private val settingsRepository: SettingsRepository) {
//    operator fun invoke(): Boolean {
//        return settingsRepository.isDarkThemeEnabled()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class HandlePlaybackCompletionUseCase @Inject constructor(
//    private val playerRepository: PlayerRepository
//) {
//    suspend operator fun invoke() {
//        playerRepository.reset()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class PreparePlaybackUseCase @Inject constructor(private val playerRepository: PlayerRepository) {
//    suspend operator fun invoke(previewUrl: String?): Result<Unit> {
//        return try {
//            playerRepository.prepare(previewUrl)
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class SearchTracksUseCase @Inject constructor (private val itunesRepository: ItunesRepository) {
//    suspend operator fun invoke(query: String): Result<List<Track>> {
//        return try {
//            val response = itunesRepository.search(query)
//            if (response.results.isEmpty()) {
//                Result.success(emptyList()) // Явно возвращаем пустой список
//            } else {
//                Result.success(response.results)
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class SendSupportEmailUseCase @Inject constructor() {
//    operator fun invoke(): SupportEmailIntentData {
//        return SupportEmailIntentData(
//            email = "support@example.com",
//            subject = "Вопрос по приложению Playlist Maker",
//            body = "Здравствуйте! У меня возникла проблема..."
//        )
//    }
//}
//data class SupportEmailIntentData(
//    val email: String,
//    val subject: String,
//    val body: String
//)
//package com.practicum.playlistmaker.domain.usecase
//class ShareAppUseCase @Inject constructor(){
//    operator fun invoke(): String {
//        return "Скачайте приложение: https://example.com/playlistmaker"
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class StopPlaybackUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
//    suspend operator fun invoke() {
//        playerRepository.stop()
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class SwitchThemeUseCase @Inject constructor (private val settingsRepository: SettingsRepository) {
//
//    operator fun invoke(isDarkMode: Boolean) {
//        settingsRepository.saveTheme(isDarkMode)
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class TogglePlaybackUseCase @Inject constructor (private val playerRepository: PlayerRepository) {
//    suspend operator fun invoke(): Result<Boolean> {
//        return try {
//            if (!playerRepository.isPlaying()) {
//                playerRepository.play()
//                Result.success(true)
//            } else {
//                playerRepository.pause()
//                Result.success(false)
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}
//package com.practicum.playlistmaker.domain.usecase
//class UseCaseCreator @Inject constructor(
//    private val itunesRepository: ItunesRepository,
//    private val historyRepository: HistoryRepository,
//    private val playerRepository: PlayerRepository,
//    private val settingsRepository: SettingsRepository
//) {
//    fun createSearchTracksUseCase(): SearchTracksUseCase {
//        return SearchTracksUseCase(itunesRepository)
//    }
//    fun createAddTrackToHistoryUseCase(): AddTrackToHistoryUseCase {
//        return AddTrackToHistoryUseCase(historyRepository)
//    }
//    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase {
//        return GetSearchHistoryUseCase(historyRepository)
//    }
//    fun createClearSearchHistoryUseCase(): ClearSearchHistoryUseCase {
//        return ClearSearchHistoryUseCase(historyRepository)
//    }
//    fun createFilterTracksUseCase(): FilterTracksUseCase {
//        return FilterTracksUseCase()
//    }
//    fun createSwitchThemeUseCase(): SwitchThemeUseCase {
//        return SwitchThemeUseCase(settingsRepository)
//    }
//    fun createGetThemeStateUseCase(): GetThemeStateUseCase {
//        return GetThemeStateUseCase(settingsRepository)
//    }
//}
//package com.practicum.playlistmaker.presentation.util
//class DateFormatter {
//    private val inputFormat: SimpleDateFormat by lazy {
//        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
//            timeZone = TimeZone.getTimeZone("UTC")
//        }
//    }
//    private val outputFormat: SimpleDateFormat by lazy {
//        SimpleDateFormat("yyyy", Locale.US)
//    }
//    fun formatReleaseDate(releaseDateString: String?): String {
//        if (releaseDateString == null) return "-"
//        return try {
//            val date = inputFormat.parse(releaseDateString) ?: return "-"  // Если parse вернул null — сразу возвращаем "-"
//            outputFormat.format(date)                                     // Теперь date — не nullable
//        } catch (e: Exception) {
//            Log.w("DateFormatter", "Failed to parse release date: $releaseDateString", e)
//            "-"
//        }
//    }
//}
//package com.practicum.playlistmaker.presentation.adapter
//class TrackAdapter(
//    private var tracks: List<Track> = emptyList(),
//    private val viewType: Int,
//    private var onTrackClick: (Track) -> Unit = {},
//    private var onClickPlayButton: (Track) -> Unit = {},
//    private var onAddToPlaylist: (Track) -> Unit = {},
//    private var onFavorite: (Track) -> Unit = {}
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    var isPlaying: Boolean = false
//    var currentTimeMillis: Long = 0
//    var currentPosition: Int = -1
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        Log.d("TrackAdapter", "Current viewType: $this.viewType")  // <-- Добавить
//        return when (this.viewType) {
//            VIEW_TYPE_TRACK -> createTrackViewHolder(parent)
//            VIEW_TYPE_ALBUM -> createAlbumViewHolder(parent)
//            else -> throw IllegalArgumentException("Unsupported view type: $viewType")
//        }
//    }
//    private fun createTrackViewHolder(parent: ViewGroup): TrackViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_track, parent, false)
//        return TrackViewHolder(view)
//    }
//    private fun createAlbumViewHolder(parent: ViewGroup): AlbumViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_audioplayer, parent, false)
//        return AlbumViewHolder(view, onTrackClick, onClickPlayButton, onAddToPlaylist, onFavorite)
//    }
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val track = tracks[position]
//        when (holder) {
//            is TrackViewHolder -> bindTrackViewHolder(holder, track, position)
//            is AlbumViewHolder -> bindAlbumViewHolder(holder, track, position)
//        }
//        holder.itemView.setOnClickListener { onTrackClick(track) }
//        holder.itemView.tag = viewType
//    }
//    private fun bindTrackViewHolder(holder: TrackViewHolder, track: Track, position: Int) {
//        holder.bind(track)
//        if (position == currentPosition) {
//            holder.showPlayingState(isPlaying, currentTimeMillis)
//        } else {
//            holder.hidePlayingState()
//        }
//    }
//    fun setOnPlayButtonClickListener(listener: (Track) -> Unit) {
//        this.onClickPlayButton = listener
//    }
//    private fun bindAlbumViewHolder(holder: AlbumViewHolder, track: Track, position: Int) {
//        holder.bind(
//            track = track,
//            isPlaying = isPlaying,
//            currentTimeMillis = currentTimeMillis,
//            onTrackClick = onTrackClick,
//            onPlayButtonClick = {
//                onClickPlayButton(track) // Вызываем внешний слушатель
//            },
//            onAddToPlaylistClick = onAddToPlaylist,
//            onFavoriteClick = onFavorite
//        )
//    }
//    override fun getItemCount(): Int = tracks.size
//    fun updateList(newTracks: List<Track>) {
//        tracks = newTracks
//        notifyDataSetChanged()
//    }
//    fun setOnItemClickListener(listener: (Track) -> Unit) {
//        onTrackClick = listener
//    }
//    fun notifyDataSetChangedWithState(
//        isPlaying: Boolean,
//        currentTimeMillis: Long = 0,
//        position: Int = -1
//    ) {
//        this.isPlaying = isPlaying
//        this.currentTimeMillis = currentTimeMillis
//        this.currentPosition = position
//        if (position != -1 && position < itemCount) {
//            notifyItemChanged(position)
//        } else {
//            notifyDataSetChanged()
//        }
//    }
//}
//package com.practicum.playlistmaker.presentation.parcel
//@Parcelize
//data class ParcelableTrack(
//    val trackId: String,
//    val trackName: String,
//    val artistName: String,
//    val trackTimeMillis: Long?,
//    val artworkUrl100: String?,
//    val releaseDate: String?,
//    val collectionName: String?,
//    val primaryGenreName: String?,
//    val country: String?,
//    val previewUrl: String?
//) : Parcelable
//fun Track.toParcelable() = ParcelableTrack(
//    trackId = this.trackId,
//    trackName = this.trackName,
//    artistName = this.artistName,
//    trackTimeMillis = this.trackTimeMillis,
//    artworkUrl100 = this.artworkUrl100,
//    releaseDate = this.releaseDate,
//    collectionName = this.collectionName,
//    primaryGenreName = this.primaryGenreName,
//    country = this.country,
//    previewUrl = this.previewUrl
//)
//fun ParcelableTrack.toDomain() = Track(
//    trackId = this.trackId,
//    trackName = this.trackName,
//    artistName = this.artistName,
//    trackTimeMillis = this.trackTimeMillis,
//    artworkUrl100 = this.artworkUrl100,
//    releaseDate = this.releaseDate,
//    collectionName = this.collectionName,
//    primaryGenreName = this.primaryGenreName,
//    country = this.country,
//    previewUrl = this.previewUrl
//)
//package com.practicum.playlistmaker.presentation.ui
//@AndroidEntryPoint
//class AudioPlayerActivity : AppCompatActivity() {
//    @Inject
//    lateinit var preparePlaybackUseCase: PreparePlaybackUseCase
//    @Inject
//    lateinit var togglePlaybackUseCase: TogglePlaybackUseCase
//    @Inject
//    lateinit var stopPlaybackUseCase: StopPlaybackUseCase
//    @Inject
//    lateinit var getCurrentPositionUseCase: GetCurrentPositionUseCase
//    @Inject
//    lateinit var playerRepository: PlayerRepository  // Внедряем через Hilt!
//    private lateinit var recyclerViewAudioPlayer: RecyclerView
//    private lateinit var adapter: TrackAdapter
//    private val handler = Handler(Looper.getMainLooper())
//    private var updateRunnable: Runnable? = null
//    private var isPlaying = false
//    private var wasPausedInBackground = false
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_audioplayer)
//        val track = getTrackFromIntentOrSavedState(savedInstanceState)
//        isPlaying = savedInstanceState?.getBoolean("isPlaying") ?: false
//        setupRecyclerView(track)
//        if (savedInstanceState == null) {
//            prepareAndPlay(track)
//        } else {
//            updateUI()
//            if (isPlaying) startPolling() else stopPolling()
//        }
//        setupPlaybackListener()
//        setupBackButton()
//    }
//    private fun getTrackFromIntentOrSavedState(savedState: Bundle?): Track {
//        return savedState?.getParcelable("track")
//            ?: intent.getParcelableExtra("track")
//            ?: throw IllegalArgumentException("Track is required but not provided")
//    }
//    private fun setupBackButton() {
//        findViewById<TextView>(R.id.back).setOnClickListener { onBackPressed() }
//    }
//    private fun setupRecyclerView(track: Track) {
//        recyclerViewAudioPlayer = findViewById(R.id.recyclerViewAudioPlayer)
//        recyclerViewAudioPlayer.layoutManager = LinearLayoutManager(this)
//        adapter = TrackAdapter(
//            tracks = listOf(track),
//            viewType = VIEW_TYPE_ALBUM,
//            onTrackClick = {},
//            onClickPlayButton = { togglePlayback() },
//            onAddToPlaylist = {},
//            onFavorite = {}
//        )
//        recyclerViewAudioPlayer.adapter = adapter
//    }
//    private fun prepareAndPlay(track: Track) = lifecycleScope.launch {
//        try {
//            if (track.previewUrl.isNullOrBlank()) {
//                showError("Отрывок недоступен")
//                return@launch
//            }
//            preparePlaybackUseCase(track.previewUrl)
//            isPlaying = true
//            updateUI()
//            startPolling()
//        } catch (e: Exception) {
//            handlePlaybackError("Не удалось воспроизвести отрывок", e)
//        }
//    }
//    private fun togglePlayback() = lifecycleScope.launch {
//        try {
//            if (wasPausedInBackground) {
//                val track = requireNotNull(intent.getParcelableExtra<ParcelableTrack>("track")) { "Track missing in intent" }
//                preparePlaybackUseCase(track.previewUrl)
//                wasPausedInBackground = false
//            }
//            val result = togglePlaybackUseCase()
//            if (result.isSuccess) {
//                isPlaying = result.getOrThrow()
//                updateUI()
//                if (isPlaying) startPolling() else stopPolling()
//            }
//        } catch (e: Exception) {
//            handlePlaybackError("Ошибка при переключении воспроизведения", e)
//        }
//    }
//    private fun updateUI() = lifecycleScope.launch {
//        val currentPosition = getCurrentPositionUseCase()
//        adapter.notifyDataSetChangedWithState(isPlaying, currentPosition)
//    }
//    private fun startPolling() {
//        updateRunnable = Runnable {
//            if (isPlaying) {
//                updateUI()
//                handler.postDelayed(updateRunnable!!, 1000)
//            }
//        }
//        handler.post(updateRunnable!!)
//    }
//    private fun stopPolling() {
//        updateRunnable?.let { handler.removeCallbacks(it) }
//        updateRunnable = null
//    }
//    private fun setupPlaybackListener() = lifecycleScope.launch {
//        try {
//            playerRepository.setOnCompletionListener {
//                lifecycleScope.launch {
//                    isPlaying = false
//                    updateUI()
//                    stopPolling()
//                    playerRepository.reset()
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("AudioPlayerActivity", "Failed to set completion listener", e)
//        }
//    }
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelable("track", intent.getParcelableExtra("track"))
//        outState.putBoolean("isPlaying", isPlaying)
//    }
//    override fun onPause() {
//        super.onPause()
//        if (isPlaying) {
//            lifecycleScope.launch {
//                togglePlaybackUseCase()  // Приостанавливаем
//                wasPausedInBackground = true
//                stopPolling()
//            }
//        } else {
//            stopPolling()
//        }
//    }
//    override fun onStop() {
//        super.onStop()
//        lifecycleScope.launch { stopPlaybackUseCase() }
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        stopPolling()
//        lifecycleScope.launch {
//            playerRepository.setOnCompletionListener {}
//        }
//    }
//    override fun onResume() {
//        super.onResume()
//        if (isPlaying && !wasPausedInBackground) {
//            startPolling()
//        } else if (wasPausedInBackground) {
//            wasPausedInBackground = false  // Сбрасываем флаг
//        } else {
//            stopPolling()
//        }
//    }
//    override fun onBackPressed() {
//        stopPlaybackAndCleanup()
//        super.onBackPressed()
//    }
//    private fun stopPlaybackAndCleanup() = lifecycleScope.launch {
//        stopPlaybackUseCase()
//        isPlaying = false
//        updateUI()
//        stopPolling()
//    }
//    private fun showError(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//        Log.e("AudioPlayerActivity", "Ошибка: $message")
//    }
//    private fun handlePlaybackError(message: String, e: Exception) {
//        Log.e("AudioPlayerActivity", message, e)
//        showError(message)
//    }
//}
//package com.practicum.playlistmaker.presentation.ui
//@AndroidEntryPoint
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        findViewById<View>(R.id.setting).setOnClickListener {
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        }
//        findViewById<View>(R.id.search_button).setOnClickListener {
//            val intent = Intent(this, SearchActivity::class.java)
//            startActivity(intent)
//        }
//        findViewById<View>(R.id.mediateca).setOnClickListener {
//            val intent = Intent(this, MediatekaActivity::class.java)
//            startActivity(intent)
//        }
//    }
//}
//package com.practicum.playlistmaker.presentation.ui
//@AndroidEntryPoint
//class MediatekaActivity : AppCompatActivity() {
//    @Inject lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCase  // ← Внедряем через Hilt
//    private lateinit var adapter: TrackAdapter
//    private lateinit var recyclerView: RecyclerView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_mediateca)
//        recyclerView = findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = TrackAdapter(mutableListOf(), VIEW_TYPE_ALBUM, {}, {})
//        recyclerView.adapter = adapter
//        findViewById<View>(R.id.back).setOnClickListener { finish() }
//        loadHistory()
//    }
//    private fun loadHistory() {
//        lifecycleScope.launch {
//            try {
//                val history = getSearchHistoryUseCase()
//                adapter.updateList(history)
//            } catch (e: Exception) {
//            }
//        }
//    }
//}
//package com.practicum.playlistmaker.presentation.ui
//@AndroidEntryPoint
//class SearchActivity : AppCompatActivity() {
//    @Inject lateinit var searchTracksUseCase: SearchTracksUseCase
//    @Inject lateinit var addTrackToHistoryUseCase: AddTrackToHistoryUseCase
//    @Inject lateinit var getSearchHistoryUseCase: GetSearchHistoryUseCase
//    @Inject lateinit var clearSearchHistoryUseCase: ClearSearchHistoryUseCase
//    @Inject lateinit var filterTracksUseCase: FilterTracksUseCase
//    private lateinit var backTextView: TextView
//    private lateinit var searchEditText: EditText
//    private lateinit var resetButton: ImageView
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var noResultsLayout: LinearLayout
//    private lateinit var errorLayout: LinearLayout
//    private lateinit var updateButton: Button
//    private lateinit var hintMessage: TextView
//    private lateinit var historyRecyclerView: RecyclerView
//    private lateinit var clearHistoryButton: Button
//    private lateinit var historyRecyclerViewKit: LinearLayout
//    private lateinit var progressBar: ProgressBar
//    private lateinit var tracksAdapter: TrackAdapter
//    private lateinit var historyAdapter: TrackAdapter
//    private var filteredTracks: List<Track> = emptyList()
//    private var searchQuery: String = ""
//    private var lastSearchQuery: String? = null
//    private var isLastSearchFailed: Boolean = false
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search)
//        initViews()
//        setupClickListeners()
//        setupTextWatchers()
//        restoreState(savedInstanceState)
//        loadHistory()
//    }
//    private fun initViews() {
//        backTextView = findViewById(R.id.back)
//        searchEditText = findViewById(R.id.search_edit_text)
//        resetButton = findViewById(R.id.reset_button)
//        recyclerView = findViewById(R.id.recyclerView)
//        noResultsLayout = findViewById(R.id.no_results_layout)
//        errorLayout = findViewById(R.id.error_layout)
//        updateButton = findViewById(R.id.refresh_button)
//        hintMessage = findViewById(R.id.searchHint)
//        historyRecyclerView = findViewById(R.id.history_recycler_view)
//        clearHistoryButton = findViewById(R.id.clear_history_button)
//        historyRecyclerViewKit = findViewById(R.id.search_history_layout)
//        progressBar = findViewById(R.id.progressBar)
//        tracksAdapter = TrackAdapter(
//            tracks = emptyList(),
//            viewType = VIEW_TYPE_TRACK,
//            onTrackClick = { track -> onTrackClicked(track) },
//            onClickPlayButton = {}
//        )
//        recyclerView.adapter = tracksAdapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        historyAdapter = TrackAdapter(
//            tracks = emptyList(),
//            viewType = VIEW_TYPE_TRACK,
//            onTrackClick = { track -> openAudioPlayer(track) },
//            onClickPlayButton = {}
//        )
//        historyRecyclerView.adapter = historyAdapter
//        historyRecyclerView.layoutManager = LinearLayoutManager(this)
//    }
//    private fun setupClickListeners() {
//        backTextView.setOnClickListener {
//            finish() }
//        resetButton.setOnClickListener {
//            searchEditText.setText("")
//            updateTracksList(emptyList())
//            hideKeyboard()
//        }
//        updateButton.setOnClickListener {
//            if (isLastSearchFailed && lastSearchQuery != null) {
//                performSearch(lastSearchQuery!!)
//            }
//        }
//        clearHistoryButton.setOnClickListener {
//            lifecycleScope.launch {
//                clearSearchHistoryUseCase()
//                loadHistory()
//            }
//        }
//    }
//    private fun setupTextWatchers() {
//        var searchJob: Job? = null
//        searchEditText.doOnTextChanged { text, _, _, _ ->
//            val query = text?.toString()?.trim() ?: ""
//            searchQuery = query
//            resetButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.INVISIBLE
//            updateHintVisibility(query.isEmpty())
//            updateHistoryVisibility()
//            searchJob?.cancel()
//            if (query.isNotEmpty()) {
//                searchJob = lifecycleScope.launch {
//                    delay(2000) // 2 секунды
//                    performSearch(query)
//                }
//            } else {
//                // Если строка пуста — сразу очистить результаты
//                updateTracksList(emptyList())
//                showNoResults(true)
//            }
//        }
//        searchEditText.setOnFocusChangeListener { _, hasFocus ->
//            updateHintVisibility(hasFocus && searchEditText.text.isEmpty())
//            updateHistoryVisibility()
//        }
//    }
//    private fun restoreState(savedInstanceState: Bundle?) {
//        if (savedInstanceState != null) {
//            searchQuery = savedInstanceState.getString(SEARCH_QUERY_KEY, "")
//            searchEditText.setText(searchQuery)
//            if (searchQuery.isNotEmpty()) performSearch(searchQuery)
//        }
//    }
//    private fun loadHistory() {
//        if (!::getSearchHistoryUseCase.isInitialized) {
//            return
//        }
//        lifecycleScope.launch {
//            val history = getSearchHistoryUseCase()
//            historyAdapter.updateList(history)
//            updateHistoryVisibility()
//        }
//    }
//    private fun performSearch(query: String) {
//        if (query.isEmpty()) return
//        showLoading()
//        lastSearchQuery = query
//        lifecycleScope.launch {
//            val result = searchTracksUseCase(query)
//            if (result.isSuccess) {
//                isLastSearchFailed = false
//                errorLayout.visibility = View.GONE
//                filteredTracks = result.getOrThrow()
//                updateTracksList(filteredTracks)
//                showNoResults(filteredTracks.isEmpty())
//            } else {
//                isLastSearchFailed = true
//                showError()
//            }
//            hideLoading()
//        }
//    }
//    private fun filterAndUpdateTracks(query: String) {
//        filteredTracks = filterTracksUseCase(tracks = filteredTracks, query = query)
//        updateTracksList(filteredTracks)
//    }
//    private fun updateTracksList(tracks: List<Track>) {
//        tracksAdapter.updateList(tracks)
//        recyclerView.visibility = if (tracks.isNotEmpty()) View.VISIBLE else View.GONE
//        showNoResults(tracks.isEmpty() && searchQuery.isNotEmpty())
//    }
//    private fun onTrackClicked(track: Track) {
//        lifecycleScope.launch {
//            addTrackToHistoryUseCase(track)
//            loadHistory()
//            openAudioPlayer(track)
//        }
//    }
//    private fun openAudioPlayer(track: Track) {
//        val intent = Intent(this, AudioPlayerActivity::class.java)
//        val parcelableTrack = track.toParcelable()
//        intent.putExtra("track", parcelableTrack)
//        startActivity(intent)
//    }
//    private fun showLoading() {
//        progressBar.visibility = View.VISIBLE
//        recyclerView.visibility = View.INVISIBLE
//        noResultsLayout.visibility = View.INVISIBLE
//        errorLayout.visibility = View.INVISIBLE
//    }
//    private fun hideLoading() {
//        progressBar.visibility = View.INVISIBLE
//    }
//    private fun showError() {
//        errorLayout.visibility = View.VISIBLE
//        noResultsLayout.visibility = View.GONE
//    }
//    private fun showNoResults(show: Boolean) {
//        noResultsLayout.visibility = if (show && !isLastSearchFailed) View.VISIBLE else View.GONE
//        errorLayout.visibility = View.GONE
//    }
//    private fun updateHintVisibility(show: Boolean) {
//        hintMessage.visibility = if (show) View.VISIBLE else View.GONE
//    }
//    private fun updateHistoryVisibility() {
//        val isEmptyQuery = searchEditText.text.isEmpty()
//        val hasFocus = searchEditText.hasFocus()
//        val hasHistory = historyAdapter.itemCount > 0
//        historyRecyclerViewKit.visibility = if (isEmptyQuery && hasFocus && hasHistory) {
//            View.VISIBLE
//        } else {
//            View.GONE
//        }
//    }
//    private fun hideKeyboard() {
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
//    }
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString(SEARCH_QUERY_KEY, searchQuery)
//    }
//    override fun onResume() {
//        super.onResume()
//        loadHistory()
//        updateHistoryVisibility()
//    }
//}
//package com.practicum.playlistmaker.presentation.ui
//@AndroidEntryPoint
//class SettingsActivity : AppCompatActivity() {
//    @Inject lateinit var switchThemeUseCase: SwitchThemeUseCase
//    @Inject lateinit var getThemeStateUseCase: GetThemeStateUseCase
//    @Inject lateinit var shareAppUseCase: ShareAppUseCase
//    @Inject lateinit var sendSupportEmailUseCase: SendSupportEmailUseCase
//    private lateinit var themeSwitcher: SwitchMaterial
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_settings)
//        themeSwitcher = findViewById(R.id.switch_button)
//        themeSwitcher.isChecked = getThemeStateUseCase()
//        findViewById<View>(R.id.back).setOnClickListener { finish() }
//        findViewById<View>(R.id.btnShare).setOnClickListener { shareApp() }
//        findViewById<View>(R.id.supportButton).setOnClickListener { sendEmail() }
//        findViewById<View>(R.id.userAgreementButton).setOnClickListener { openUserAgreement() }
//        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
//            switchThemeUseCase(isChecked)
//        }
//    }
//    private fun shareApp() {
//        val shareText = shareAppUseCase()
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.type = "text/plain"
//        intent.putExtra(Intent.EXTRA_TEXT, shareText)
//        startActivity(Intent.createChooser(intent, getString(R.string.choose_app)))
//    }
//    private fun sendEmail() {
//        val data = sendSupportEmailUseCase()
//        val intent = Intent(Intent.ACTION_SENDTO)
//        intent.data = Uri.parse("mailto:")
//        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(data.email))
//        intent.putExtra(Intent.EXTRA_SUBJECT, data.subject)
//        intent.putExtra(Intent.EXTRA_TEXT, data.body)
//        startActivity(intent)
//    }
//    private fun openUserAgreement() {
//        val url = getString(R.string.url_oferta)
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        startActivity(intent)
//    }
//}
//package com.practicum.playlistmaker.presentation.util
//class Constants {
//    companion object {
//        const val PREFERENCES = "play_maker_preferences"
//        const val DARK_THEME_KEY = "dark_theme"
//        const val HISTORY_KEY = "search_history"
//        const val SEARCH_QUERY_KEY = "SEARCH_QUERY"
//        const val VIEW_TYPE_TRACK = 0
//        const val VIEW_TYPE_ALBUM = 1
//    }
//}
//package com.practicum.playlistmaker
//@HiltAndroidApp  // 1. Добавляем аннотацию для Hilt
//class App : Application() {
//    lateinit var sharedPreferences: SharedPreferences  // 2. Убираем private
//    override fun onCreate() {
//        super.onCreate()
//        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
//        val darkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)
//        setTheme(darkTheme)
//    }
//    fun switchTheme(darkThemeEnabled: Boolean) {
//        setTheme(darkThemeEnabled)
//        with(sharedPreferences.edit()) {
//            putBoolean(DARK_THEME_KEY, darkThemeEnabled)
//            apply()
//        }
//    }
//    private fun setTheme(darkTheme: Boolean) {
//        AppCompatDelegate.setDefaultNightMode(
//            if (darkTheme) {
//                AppCompatDelegate.MODE_NIGHT_YES
//            } else {
//                AppCompatDelegate.MODE_NIGHT_NO
//            }
//        )
//    }
//}