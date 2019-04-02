package com.jukebox.hero.ui.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.listview_song_item_row.view.*

class SongsAdapter(query : Query) : FirestoreAdapter<SongsAdapter.SongHolder>(query){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsAdapter.SongHolder {
        return SongsAdapter.SongHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.listview_song_item_row, parent, false))
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Song::class.java))
    }

    class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val songName = itemView.song_name
        private val albumArt = itemView.album_art
        private val artistName = itemView.artist

        fun bind(song: Song?){
            if(song == null){
                return
            }
            songName?.text = song.name
            Picasso.get().load(song.albumArt).resize(150, 150).into(albumArt)
            artistName.text = song.artist
            Log.d(TAG, song.name)

            albumArt.setOnClickListener{
                // do the onclick
            }
            artistName.setOnClickListener {
                // do the onclick
            }
            songName.setOnClickListener {
                // do the onclick
            }
        }
    }

    companion object {
        const val TAG = "SongAdapter"
    }
}