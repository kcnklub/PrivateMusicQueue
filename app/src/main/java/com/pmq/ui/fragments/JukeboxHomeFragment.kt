package com.pmq.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmq.Models.Song
import com.pmq.R
import com.pmq.ui.JukeBoxActivity
import com.pmq.ui.adapters.SongsAdapter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class JukeboxHomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var query: Query
    private lateinit var adapter : SongsAdapter
    private lateinit var searchResultsList : RecyclerView

    private lateinit var fireStore : FirebaseFirestore
    private lateinit var partyId : String
    private lateinit var queue : ArrayList<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireStore = FirebaseFirestore.getInstance()
        partyId = (activity as JukeBoxActivity).partyID
        partyID = partyId
        Log.d("TAG", partyId)
        queue = ArrayList()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_jukebox_home, container, false)
        linearLayoutManager = LinearLayoutManager(requireContext())
        searchResultsList= view!!.findViewById(R.id.queue_list)
        query = fireStore.collection("Parties")
                .document(partyId).collection("Queue")
                .orderBy(Song.FIELD_SCORE, Query.Direction.DESCENDING)
                .orderBy(Song.FIELD_QUEUE_TIME, Query.Direction.ASCENDING)
        (this.requireActivity() as JukeBoxActivity).jukeboxHomeFragment = this

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter = SongsAdapter(query, (activity as JukeBoxActivity).playerFragment, partyId)
        searchResultsList.layoutManager = linearLayoutManager
        searchResultsList.adapter = adapter
        adapter.startListening()
    }

    companion object {
        @JvmStatic
        var partyID = ""
        fun newInstance(param1: String, param2: String) =
                JukeboxHomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
