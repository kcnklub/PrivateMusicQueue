package com.musicparty.pmq.services

import com.musicparty.pmq.Models.Song
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PmqSongService {

    @GET("getSongForParty")
    fun getSongsForParty(@Query("queueId") queueId : Int) : Observable<List<Song.Song>>

    companion object {
        fun create() : PmqSongService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://10.0.2.2:8080/")
                    .build()

            return retrofit.create(PmqSongService::class.java)
        }
    }
}