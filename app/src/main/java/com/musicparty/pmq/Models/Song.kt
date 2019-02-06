package com.musicparty.pmq.Models

object Song {
    data class Result(val songId : Int,
                      val songURI : String,
                      val positionInQueue : Int,
                      val queueId : Int,
                      val requesterUsername : String,
                      val message : String)
    data class Body(val songURI : String,
                    val positionInQueue : Int,
                    val queueId : Int,
                    val requesterUsername : String)
    data class Song(val songId : Int,
                    val songURI : String,
                    val positionInQueue : Int,
                    val queueId : Int,
                    val requesterUsername : String)

}