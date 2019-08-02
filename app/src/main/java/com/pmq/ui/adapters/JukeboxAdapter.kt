package com.pmq.ui.adapters

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmq.Models.Party
import com.pmq.hero.R
import com.pmq.ui.HomeActivity
import com.pmq.ui.JukeBoxActivity
import kotlinx.android.synthetic.main.listview_jukebox_row.view.*

class JukeboxAdapter(query : Query, val context: Context) : FirestoreAdapter<JukeboxAdapter.JukeboxHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JukeboxHolder {
        return JukeboxHolder(LayoutInflater.from(parent.context).inflate(R.layout.listview_jukebox_row, parent, false), context)
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
                Toast.makeText(context, "this is going to take you do the party now?", Toast.LENGTH_LONG).show()
                FirebaseFirestore.getInstance().collection("Parties")
                        .whereEqualTo(Party.FIELD_ROOM_CODE, party.roomCode).get()
                        .addOnSuccessListener {
                            if(!it.isEmpty){
                                val party = it.documents.first().toObject(Party::class.java)

                                HomeActivity.addUserToParty(it.documents.first().id)
                                HomeActivity.setCurrentParty(FirebaseAuth.getInstance().currentUser!!.uid, it.documents.first().id)
                                if(FirebaseAuth.getInstance().currentUser!!.uid != party!!.hostId){
                                    HomeActivity.addPartyToHistory(FirebaseAuth.getInstance().currentUser!!.uid, it.documents.first().id)
                                }

                                val intent = Intent(context, JukeBoxActivity::class.java)
                                intent.putExtra("partyQueueId", it.documents.first().id)
                                intent.putExtra("OwnerId", party.hostId)
                                context.startActivity(intent)
                            }
                        }
            }
        }
    }
}