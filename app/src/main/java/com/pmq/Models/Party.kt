package com.pmq.Models

data class Party (
        var hostId: String? = null,
        var roomCode: String? = null,
        var partyName: String? = null,
        var publicIP: String? = null
){
    companion object {
        const val FIELD_NAME = "partyName"
        const val FIELD_HOST_ID = "hostId"
        const val FIELD_ROOM_CODE = "roomCode"
        const val FIELD_PUBLIC_IP = "publicIP"
    }
}