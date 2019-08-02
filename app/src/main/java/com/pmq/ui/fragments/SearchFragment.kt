package com.pmq.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.pmq.hero.R
import com.pmq.ui.JukeBoxActivity
import com.pmq.ui.adapters.TrackAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.TracksPager
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {
    val api = SpotifyApi()
    var songArrayList: ArrayList<Track> = ArrayList()
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var partyId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = activity as JukeBoxActivity
        partyId = activity.partyID
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        linearLayoutManager = LinearLayoutManager(requireContext())
        val searchResultsList: RecyclerView = view.findViewById(R.id.search_list)
        api.setAccessToken(JukeBoxActivity.spotifyAuthToken)
        view.searchButton.setOnClickListener {

            val searchText: EditText = view.findViewById(R.id.searchText)

            val spotify = api.service
            spotify.searchTracks(searchText.text.toString(), object : SpotifyCallback<TracksPager>() {
                override fun failure(p0: SpotifyError?) {
                    Log.d("Album failure", p0.toString())
                }

                override fun success(t: TracksPager?, response: retrofit.client.Response?) {
                    if (t != null) {
                        if (t.tracks.total > 0) {
                            songArrayList.clear()
                            for (i in t.tracks.items) {
                                songArrayList.add(i)
                            }
                            searchResultsList.layoutManager = linearLayoutManager
                            searchResultsList.adapter = TrackAdapter(songArrayList, requireContext(), partyId)
                        }
                    }
                }
            })
        }
        return view
    }

    fun getTheId():String {
        return partyId
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                SearchFragment().apply {
                    arguments = Bundle().apply {
                        //putString(searchQuery, searchBar)
                    }
                }
        private const val TAG = "Search Fragment"
    }
}
