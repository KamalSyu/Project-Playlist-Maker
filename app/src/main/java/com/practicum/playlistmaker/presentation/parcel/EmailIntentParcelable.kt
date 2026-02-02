package com.practicum.playlistmaker.presentation.parcel


import android.os.Parcel
import android.os.Parcelable
import com.practicum.playlistmaker.domain.model.SupportEmailIntentData

// Адаптер: превращает доменную модель в Parcelable для Intent
class EmailIntentParcelable(
    val data: SupportEmailIntentData
) : Parcelable {

    // Конструктор для создания из Parcel (требуется Parcelable)
    private constructor(parcel: Parcel) : this(
        SupportEmailIntentData(
            email = parcel.readString() ?: "",
            subject = parcel.readString() ?: "",
            body = parcel.readString() ?: ""
        )
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(data.email)
        parcel.writeString(data.subject)
        parcel.writeString(data.body)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<EmailIntentParcelable> {
        override fun createFromParcel(parcel: Parcel): EmailIntentParcelable {
            return EmailIntentParcelable(parcel)
        }

        override fun newArray(size: Int): Array<EmailIntentParcelable?> {
            return arrayOfNulls(size)
        }
    }
}