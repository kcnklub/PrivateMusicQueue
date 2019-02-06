package com.musicparty.pmq.Models

object PartyQueue {
    data class Result(val partyQueueId : Int,
                      val ownerId : Int,
                      val partyName : String,
                      val partyPassPhrase : String,
                      val isPartyPrivate : Boolean,
                      val message : String)

    data class PartyQueue(val partyQueueId : Int,
                          val ownerId : Int,
                          val partyName : String,
                          val partyPassPhrase : String,
                          val isPartyPrivate : Boolean)

    data class Body(val ownerId: Int,
                    val partyName: String,
                    val partyPassPhrase: String,
                    val isPartyPrivate: Boolean)
}