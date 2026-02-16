package com.practicum.playlistmaker.sharing.data.parcel

import android.os.Parcel
import android.os.Parcelable
import com.practicum.playlistmaker.sharing.domain.model.SupportEmailIntentData

class EmailIntentParcelable(
    val data: SupportEmailIntentData
) : Parcelable {

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