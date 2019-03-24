package com.jukebox.hero.ui.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.renderscript.ScriptGroup
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.SearchActivity
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
        holder.songName?.text = item.name
        val albumArtUrl = item.album.images[0].url
        Picasso.get().load(albumArtUrl).resize(150, 150).into(holder.albumArt)
        holder.artistName.text = item.artists[0].name

        //Toast.makeText(context, ""+data[position].uri, Toast.LENGTH_SHORT).show()
        holder.albumArt.setOnClickListener{
            alertBox(holder.artistName.text.toString(), holder.songName.text.toString(), data[position].uri)
        }
        holder.artistName.setOnClickListener {
            alertBox(holder.artistName.text.toString(), holder.songName.text.toString(), data[position].uri)
        }
        holder.songName.setOnClickListener {
            alertBox(holder.artistName.text.toString(), holder.songName.text.toString(), data[position].uri)
        }
    }

    fun alertBox(aName: String, sName: String, uuri:String) {
        var builder: AlertDialog.Builder = AlertDialog.Builder(context)

        val aName = aName
        val sName = sName

        builder.setCancelable(true)
        builder.setTitle("Add Song")
        builder.setMessage("Add $sName by $aName to the queue?")

        builder.setNegativeButton("No") { _, _->
            Toast.makeText(context, "Song not added to queue", Toast.LENGTH_SHORT).show()
        }
        builder.setPositiveButton("Yes") { _, _->
            Toast.makeText(context, "Song added to queue", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    class SongHolder(view: View) : RecyclerView.ViewHolder(view){
        val songName = view.song_name
        val albumArt = view.album_art
        val artistName = view.artist
    }
}