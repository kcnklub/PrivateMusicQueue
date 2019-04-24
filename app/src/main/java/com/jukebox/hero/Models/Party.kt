package com.jukebox.hero.Models

data class Party (
        var HostId: String? = null,
        var RoomCode: String? = null,
        var PartyName: String? = null
){
    companion object {
        const val FIELD_NAME = "PartyName"
        const val FIELD_HOST_ID = "HostId"
        const val FIELD_ROOM_CODE = "RoomCode"
    }
}