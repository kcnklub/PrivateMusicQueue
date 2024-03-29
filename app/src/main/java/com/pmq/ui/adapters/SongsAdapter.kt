package com.pmq.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmq.Models.Song
import com.pmq.R
import com.pmq.ui.fragments.JukeboxHomeFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.listview_song_item_row.view.*

class SongsAdapter(val query : Query, private val listener : OnSongChangeListener, val partyId: String) : FirestoreAdapter<SongsAdapter.SongHolder>(query){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        return SongHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.listview_song_item_row, parent, false))
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Song::class.java), position)
    }

    interface OnSongChangeListener{
        fun onChange(song: Song?)
    }

    override fun onDataChanged() {
        super.onDataChanged()
        query.get().addOnSuccessListener {
            if(it.documents.isNotEmpty()) {
                val newSong = it.documents.first().toObject(Song::class.java)
                listener.onChange(newSong)
            }
        }
    }

    class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val songName = itemView.song_name
        private val albumArt = itemView.album_art
        private val artistName = itemView.artist
        private val score = itemView.score
        private val upvote = itemView.upvote
        private val downvote = itemView.downvote
        private var state = 0

        fun bind(song: Song?, position: Int){
            upvote.visibility = View.VISIBLE
            downvote.visibility = View.VISIBLE
            score.visibility = View.VISIBLE
            if(position == 0){
                itemView.setBackgroundColor(Color.parseColor("#66FFF9"))
                songName.setTextColor(Color.BLACK)
                artistName.setTextColor(Color.BLACK)
                itemView.upvote.visibility = View.GONE
                itemView.downvote.visibility = View.GONE
                itemView.score.visibility = View.GONE
            }
            if(song == null) {
                return
            }
            score.text = song.score.toString()
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
            upvote.setOnClickListener {
                when (state) {
                    0 -> {
                        state++
                        score.text = (score.text.toString().toInt()+1).toString()
                        updateScore(song, 1)
                    }
                    1 -> {
                        state--
                        score.text = (score.text.toString().toInt()-1).toString()
                        updateScore(song, -1)
                    }
                    else -> {
                        state+=2
                        score.text = (score.text.toString().toInt()+2).toString()
                        updateScore(song, 2)
                    }
                }

            }
            downvote.setOnClickListener {
                when (state) {
                    0 -> {
                        state--
                        score.text = (score.text.toString().toInt()-1).toString()
                        updateScore(song, -1)
                    }
                    -1 -> {
                        state++
                        score.text = (score.text.toString().toInt()+1).toString()
                        updateScore(song, 1)
                    }
                    else -> {
                        state-=2
                        score.text = (score.text.toString().toInt()-2).toString()
                        updateScore(song, -2)
                    }
                }

            }
        }

        fun updateScore(song: Song, value : Int) {
            val partyId = JukeboxHomeFragment.partyID
            val db = FirebaseFirestore.getInstance()
            val queue = db.collection("Parties").document(partyId).collection("Queue")
            queue.get().addOnSuccessListener { documents ->
                for(doc in documents) {
                    if(doc.data["songURI"] == song.songURI) {
                        Log.d(TAG,"${doc.data["name"]}")
                        val data = doc.data
                        data["score"] = song.score!!+value
                        queue.document(doc.id).update(data)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "SongAdapter"
    }
}