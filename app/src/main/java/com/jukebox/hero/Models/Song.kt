package com.jukebox.hero.Models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Song(
        var name: String? = null,
        var artist: String? = null,
        var albumArt: String? = null,
        var songURI: String? = null,
        var placeInQueue : Int? = null
){
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_ARTIST = "artist"
        const val FIELD_ALBUM_ART = "albumArt"
        const val FIELD_SONG_URI = "songURI"
        const val FIELD_PLACE_IN_QUEUE = "placeInQueue"
    }
}