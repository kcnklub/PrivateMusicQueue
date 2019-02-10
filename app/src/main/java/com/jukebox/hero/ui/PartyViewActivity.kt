package com.jukebox.hero.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView
import android.widget.Toast
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.services.PmqSongService
import com.jukebox.hero.ui.Adapters.SongsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_party_view.*

class PartyViewActivity : AppCompatActivity() {

    private var disposable : Disposable? = null
    private val pmqSongService by lazy {
        PmqSongService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_view)
        setSupportActionBar(toolbar)
        val partyQueueId : Int = this.intent.extras["partyQueueId"] as Int
        disposable = pmqSongService.getSongsForParty(partyQueueId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result -> run{
                            if(!result.isEmpty()){
                                setUpListView(result)
                            } else {
                                throw Exception("Result is empty")
                            }
                        }},{ error -> run {
                            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                        }}
                )

    }

    private fun setUpListView(list: List<Song.Song>){
        val songs = findViewById<ListView>(R.id.song_list)
        val songAdapter = SongsAdapter(this, R.layout.listview_song_item_row, list)
        songs.adapter = songAdapter
    }

}
