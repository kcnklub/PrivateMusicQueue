package com.jukebox.hero.Models

data class Party (
        var hostId: String? = null,
        var RoomCode: String? = null,
        var partyName: String? = null
){
    companion object {
        const val FIELD_NAME = "PartyName"
        const val FIELD_HOST_ID = "HostId"
        const val FIELD_ROOM_CODE = "RoomCode"
    }
}