package com.codetech.speechtotext.models

import android.os.Parcel
import android.os.Parcelable

data class WordResult(
    val word: String,
    val phonetic: String?,
    val meanings: List<Meaning>,
)


data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>,
    val synonyms: List<String>,
    val antonyms: List<String>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createTypedArrayList(Definition.CREATOR) ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(partOfSpeech)
        parcel.writeTypedList(definitions)
        parcel.writeStringList(synonyms)
        parcel.writeStringList(antonyms)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meaning> {
        override fun createFromParcel(parcel: Parcel): Meaning {
            return Meaning(parcel)
        }

        override fun newArray(size: Int): Array<Meaning?> {
            return arrayOfNulls(size)
        }
    }
}

data class Definition(
    val definition: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(definition)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Definition> {
        override fun createFromParcel(parcel: Parcel): Definition {
            return Definition(parcel)
        }

        override fun newArray(size: Int): Array<Definition?> {
            return arrayOfNulls(size)
        }
    }
}
