package com.practicum.playlistmaker.domain.usecase

import android.content.Context
import com.practicum.playlistmaker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ShareAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) : ShareAppUseCaseContract{

    override operator fun invoke(): String {
        return context.getString(R.string.share_text)
    }
}
