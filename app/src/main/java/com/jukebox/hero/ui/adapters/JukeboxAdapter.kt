package com.jukebox.hero.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Party
import com.jukebox.hero.R
import kotlinx.android.synthetic.main.listview_jukebox_row.view.*

class JukeboxAdapter(query : Query, val context: Context) : FirestoreAdapter<JukeboxAdapter.JukeboxHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JukeboxHolder {
        return JukeboxHolder(LayoutInflater.from(parent.context).inflate(R.layout.listview_jukebox_row , parent, false), context)
    }

    override fun onBindViewHolder(holder: JukeboxHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Party::class.java), position)
    }

    class JukeboxHolder(itemView : View, val context: Context) : RecyclerView.ViewHolder(itemView){

        private val jukeboxName = itemView.jukebox_name
        private val jukeboxCode = itemView.jukebox_code

        private val jukebox = itemView.jukebox

        fun bind(party: Party?, position: Int){
            jukeboxName.text = party!!.partyName
            jukeboxCode.text = "Room Code : " + party.roomCode

            jukebox.setOnClickListener {
                Toast.makeText(context, "this is going to take you to the party now?", Toast.LENGTH_LONG).show()
            }
        }
    }
}