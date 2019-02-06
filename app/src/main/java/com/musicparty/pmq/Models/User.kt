package com.musicparty.pmq.Models


object User {
    data class Result(val userId: Int,
                      val username : String,
                      val userEmail : String,
                      val userHashPass : String,
                      val message : String)

    data class User(val username: String,
                    val userEmail: String,
                    val userHashPass: String)
}