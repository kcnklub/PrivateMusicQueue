package com.jukebox.hero.ui.adapters

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.renderscript.ScriptGroup
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.squareup.picasso.Picasso
import kaaes.spotify.webapi.android.models.Track
import kotlinx.android.synthetic.main.listview_song_item_row.view.*
import java.io.InputStream
import java.net.URL

class TrackAdapter(private var data: List<Track>, val context: Context) : RecyclerView.Adapter<TrackAdapter.SongHolder>(){

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        return SongHolder(LayoutInflater.from(context).inflate(R.layout.listview_song_item_row, parent, false))
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val item = data[position]
        holder.songURI?.text = item.name
        val albumArtUrl = item.album.images[0].url
        Picasso.get().load(albumArtUrl).resize(150, 150).into(holder.albumArt)
        holder.artistName.text = item.artists[0].name
    }

    class SongHolder(view: View) : RecyclerView.ViewHolder(view){
        val songURI = view.song_name
        val albumArt = view.album_art
        val artistName = view.artist
    }
}