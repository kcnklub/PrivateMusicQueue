package com.jukebox.hero.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.gson.GsonBuilder

import com.jukebox.hero.R
import com.jukebox.hero.ui.MainActivity
import com.jukebox.hero.ui.MainActivity.Companion.CLIENT_ID
import com.jukebox.hero.ui.MainActivity.Companion.REDIRECT_URL
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PlayerFragment interface
 * to handle interaction events.
 * Use the [PlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PlayerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    var trackProgressBar : TrackProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        val activity = activity as MainActivity
        activity.spotifyAuthToken

        SpotifyAppRemote.setDebugMode(true)

        disconnected()
        connectAndAuthorize()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment

        val view  = inflater.inflate(R.layout.fragment_player, container, false)
        val seekBar = view.findViewById<SeekBar>(R.id.seek_to)
        seekBar.isEnabled = false
        seekBar.progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        seekBar.indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        trackProgressBar = TrackProgressBar(seekBar)
        return view
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        disconnected()
    }

    private fun disconnect(){
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        disconnected()
    }

    private fun disconnected(){
        // update the ui on disconnect
    }

    private fun connectAndAuthorize(){
        connect()
    }

    private fun connect(){
        SpotifyAppRemote.disconnect(spotifyAppRemote)

        val activity = activity as MainActivity

        SpotifyAppRemote.connect(
                activity.application,
                ConnectionParams
                        .Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URL)
                        .build(),
                object : Connector.ConnectionListener{
                    override fun onConnected(p0: SpotifyAppRemote?) {
                        spotifyAppRemote = p0!!
                        onConnected()
                    }

                    override fun onFailure(p0: Throwable?) {
                        Log.d(TAG, "Error $p0")
                        disconnect()
                    }
                }
        )
    }

    fun onConnected(){
        // ui update
    }

    class TrackProgressBar(val seekBar: SeekBar,
                           val handler: Handler = Handler()){

        companion object {
            const val LOOP_DURATION : Long = 500
        }

        val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                spotifyAppRemote!!.playerApi.seekTo(seekBar!!.progress.toLong())
                        .setErrorCallback{
                            Log.d(TAG, it.message)
                        }
            }
        }

        init {
            seekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        }

        private val seekRunnable : Runnable = object : Runnable {
            override fun run() {
                val progress = seekBar.progress
                seekBar.progress = (progress + LOOP_DURATION).toInt()
                handler.postDelayed(this, LOOP_DURATION)
            }
        }

        private fun setDuration(duration: Long){
            seekBar.max = duration.toInt()
        }

        private fun update(progress : Long){
            seekBar.progress = progress.toInt()
        }

        private fun pause(){
            handler.removeCallbacks(seekRunnable)
        }

        private fun unpause(){
            handler.removeCallbacks(seekRunnable)
            handler.postDelayed(seekRunnable, LOOP_DURATION)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PlayerFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        const val TAG = "Player Fragment"

        @JvmStatic
        var spotifyAppRemote : SpotifyAppRemote? = null

        var gson = GsonBuilder().setPrettyPrinting().create()
    }
}
