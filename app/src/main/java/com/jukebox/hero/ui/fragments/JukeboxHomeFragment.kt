package com.jukebox.hero.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.JukeBoxActivity
import com.jukebox.hero.ui.adapters.SongsAdapter

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class JukeboxHomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    lateinit var query: Query
    lateinit var adapter : SongsAdapter

    private lateinit var firestore : FirebaseFirestore
    private lateinit var partyId : String
    lateinit var queue : ArrayList<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        partyId = (activity as JukeBoxActivity).partyID
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
        val searchResultsList: RecyclerView = view.findViewById(R.id.queue_list)
        query = firestore.collection("Parties")
                .document(partyId).collection("Queue")
                .orderBy(Song.FIELD_PLACE_IN_QUEUE, Query.Direction.ASCENDING)

        adapter = SongsAdapter(query)

        searchResultsList.layoutManager = linearLayoutManager
        searchResultsList.adapter = adapter


        return view
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JukeboxHomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                JukeboxHomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
