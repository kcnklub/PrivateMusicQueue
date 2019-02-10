package com.jukebox.hero.services

import com.jukebox.hero.Models.PartyQueue
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PmqPartyQueueService {

    @POST("createParty")
    fun createParty(@Body body : PartyQueue.Body): Observable<PartyQueue.Result>

    @GET("partiesByUserId")
    fun getAllUserParties(@Query("userId") userId : Int):
            Observable<List<PartyQueue.PartyQueue>>

    companion object {
        fun create() : PmqPartyQueueService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://10.0.2.2:8080/")
                    .build()

            return retrofit.create(PmqPartyQueueService::class.java)
        }
    }
}