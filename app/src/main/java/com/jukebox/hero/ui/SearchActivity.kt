package com.jukebox.hero.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.jukebox.hero.R
import com.jukebox.hero.ui.adapters.TrackAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.TracksPager

class SearchActivity : AppCompatActivity() {

    val context : Context = this

    val api = SpotifyApi()
    var songArrayList: ArrayList<Track> = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager

    private val lastVisableItemPosition : Int
        get() = linearLayoutManager.findLastVisibleItemPosition()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        linearLayoutManager = LinearLayoutManager(this)
        val songlistview: RecyclerView = findViewById(R.id.search_list)
        songlistview.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        api.setAccessToken(JukeBoxActivity.spotifyAuthToken)
        val song2Button: Button = findViewById(R.id.searchButton)
        song2Button.setOnClickListener { view ->
            val searchtext: EditText = findViewById(R.id.searchText)

            val spotify = api.service

            spotify.searchTracks(searchtext.text.toString(), object : SpotifyCallback<TracksPager>() {
                override fun failure(p0: SpotifyError?) {
                    Log.d("Album failure", p0.toString())
                }

                override fun success(t: TracksPager?, response: retrofit.client.Response?) {
                    if (t != null) {
                        if (t.tracks.total > 0) {
                            songArrayList.clear()
                            for (i in t.tracks.items) {
                                songArrayList.add(i)
                            }
                            songlistview.layoutManager = linearLayoutManager
//                            songlistview.adapter = TrackAdapter(songArrayList, context)
                        }
                    }
                }
            })
        }
    }
}