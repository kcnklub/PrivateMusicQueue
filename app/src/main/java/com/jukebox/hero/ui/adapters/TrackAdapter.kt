package com.jukebox.hero.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.squareup.picasso.Picasso
import kaaes.spotify.webapi.android.models.Track
import kotlinx.android.synthetic.main.listview_song_item_row.view.*

class TrackAdapter(private var data: List<Track>, val context: Context, private val partyQueueId: String) : RecyclerView.Adapter<TrackAdapter.SongHolder>(){

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

        holder.albumArt.setOnClickListener{
            alertBox(holder.artistName.text.toString(),
                    holder.songName.text.toString(),
                    data[position].uri,
                    data[position].album.images[0].url)
        }
        holder.artistName.setOnClickListener {
            alertBox(holder.artistName.text.toString(),
                    holder.songName.text.toString(),
                    data[position].uri,
                    data[position].album.images[0].url)
        }
        holder.songName.setOnClickListener {
            alertBox(holder.artistName.text.toString(),
                    holder.songName.text.toString(),
                    data[position].uri,
                    data[position].album.images[0].url)
        }
    }

    private fun alertBox(artistName: String, songName: String, songURI: String, albumArtURL: String ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)


        builder.setCancelable(true)
        builder.setTitle("Add Song")
        builder.setMessage("Add $songName by $artistName to the queue?")

        builder.setNegativeButton("No") { _, _->
            Toast.makeText(context, "Song not added to queue", Toast.LENGTH_SHORT).show()
        }
        builder.setPositiveButton("Yes") { _, _->
            var count = 0
            val db = FirebaseFirestore.getInstance()
            val partyRef = db.collection("Parties").document(partyQueueId)
            val songRef = partyRef.collection("Queue").document()
            db.collection("Parties").document(partyQueueId)
                    .collection("Queue")
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents){
                            count += 1
                            Log.d(TAG, "${document.data}")
                        }
                        FirebaseFirestore.getInstance().runTransaction{ p1 ->
                            Log.d(TAG, count.toString())
                            val song = Song(songName, artistName, albumArtURL, songURI, count + 1, 0)
                            p1.set(songRef, song)
                            null
                        }.addOnSuccessListener {
                            Toast.makeText(context, "Song added to queue", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Log.d(TAG, it.message)
                            it.printStackTrace()
                            Toast.makeText(context, "Song was not added to queue ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }

        }
        builder.show()
    }

    class SongHolder(view: View) : RecyclerView.ViewHolder(view){
        val songName = view.song_name
        val albumArt = view.album_art
        val artistName = view.artist
    }

    companion object {
        const val TAG = "TrackAdapter"
    }
}