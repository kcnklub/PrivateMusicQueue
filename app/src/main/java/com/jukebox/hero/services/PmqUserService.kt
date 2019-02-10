package com.jukebox.hero.services

import com.jukebox.hero.Models.User
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface PmqUserService {

    @GET("signIn")
    fun signIn(@Query("email") email: String,
               @Query("hashPass") hashPass : String): Observable<User.Result>

    @POST("registerUser")
    fun registerUser(@Body user : User.User) : Observable<User.Result>

    companion object {
        fun create() : PmqUserService {
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://10.0.2.2:8080/")
                    .build()

            return retrofit.create(PmqUserService::class.java)
        }
    }
}