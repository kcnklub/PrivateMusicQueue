package com.pmq.Models


data class User (
        var displayName: String? = null,
        var userId: String? = null,
        var userCode: String? = null,
        var currentParty: String? = null
){
    companion object {
        const val FIELD_CURRENT_PARTY = "currentParty"
        const val FIELD_USER_ID = "userId"
    }
}