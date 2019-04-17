package com.jukebox.hero.ui.fragments

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatImageButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.GsonBuilder
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.JukeBoxActivity
import com.jukebox.hero.ui.JukeBoxActivity.Companion.CLIENT_ID
import com.jukebox.hero.ui.JukeBoxActivity.Companion.REDIRECT_URL
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Capabilities
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PlayerFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    var isOwner : Boolean = false

    var playPauseButton : AppCompatImageButton? = null
    var skipPreviousButton : AppCompatImageButton? = null
    var skipNextButton : AppCompatImageButton? = null
    var coverArtImageView : ImageView? = null

    var seekBar : SeekBar? = null
    var trackProgressBar : TrackProgressBar? = null

    var playerStateSubscription : Subscription<PlayerState>? = null
    var playerContextSubscription : Subscription<PlayerContext>? = null
    var capabilitiesSubscription : Subscription<Capabilities>? = null

    var playerContextEventCallBack = Subscription.EventCallback<PlayerContext> {
        Log.d(TAG, "idk something is happening here. ")
    }

    private val playerStateEventCallBack = Subscription.EventCallback<PlayerState> { playerState ->
        if(playerState.playbackSpeed > 0){
            trackProgressBar!!.unpause()
        } else {
            trackProgressBar!!.pause()
        }

        if(playerState.isPaused){
            playPauseButton!!.setImageResource(R.drawable.btn_play)
        } else {
            playPauseButton!!.setImageResource(R.drawable.btn_pause)
        }

        spotifyAppRemote!!.imagesApi.getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                .setResultCallback { bitmap ->
                    coverArtImageView!!.setImageBitmap(bitmap)
                }
        seekBar!!.max = playerState.track.duration.toInt()
        trackProgressBar!!.setDuration(playerState.track.duration)
        trackProgressBar!!.update(playerState.playbackPosition)

        seekBar!!.isEnabled = isOwner
        if(playerState.playbackPosition == playerState.track.duration){
            (requireActivity() as JukeBoxActivity).updateQueue()
            playNextSong()
            lastSongPos = playerState.track.duration / 2
        } else {
            lastSongPos = playerState.playbackPosition
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        isOwner = (this.requireContext() as JukeBoxActivity).isOwner!!
        if(isOwner){
            SpotifyAppRemote.setDebugMode(true)
            disconnected()
            connectAndAuthorize()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment

        val view  = inflater.inflate(R.layout.fragment_player, container, false)
        seekBar = view.findViewById(R.id.seek_to)
        seekBar!!.isEnabled = false
        seekBar!!.progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        seekBar!!.indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        coverArtImageView = view.findViewById(R.id.image)
        playPauseButton = view.findViewById(R.id.play_pause_button)
        playPauseButton!!.setOnClickListener{onPlayPauseClicked()}
        skipNextButton = view.findViewById(R.id.skip_next_button)
        skipNextButton!!.setOnClickListener{onSkipNext()}
        skipPreviousButton = view.findViewById(R.id.skip_prev_button)
        skipPreviousButton!!.setOnClickListener { onSkipPrevious() }
        trackProgressBar = TrackProgressBar(seekBar)

        isOwner = (this.requireContext() as JukeBoxActivity).isOwner!!
        if(!isOwner){
            playPauseButton!!.visibility = View.INVISIBLE
            skipNextButton!!.visibility = View.INVISIBLE
            skipPreviousButton!!.visibility = View.INVISIBLE
        }
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

        val activity = activity as JukeBoxActivity

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URL)
                .showAuthView(true)
                .build()
        val connectionListener = object : Connector.ConnectionListener {
            override fun onConnected(spotAppRemote: SpotifyAppRemote) {
                spotifyAppRemote = spotAppRemote
                Log.e("PlayerFragment","Connected to Spotify!")
                onConnected()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e("PlayerFragment",throwable.message, throwable)
                if(throwable is CouldNotFindSpotifyApp)
                    Log.e("PlayerFragment","Spotify App not installed!")
                else if(throwable is NotLoggedInException)
                    Log.e("PlayerFragment","Not Logged in with Spotify!")
                else
                    Log.e("PlayerFragment", "Failed to Connect to Spotify!")
            }
        }

        //if(spotifyAppRemote == null)
        //    SpotifyAppRemote.disconnect(spotifyAppRemote)
        SpotifyAppRemote.setDebugMode(true)
        SpotifyAppRemote.connect(context, connectionParams, connectionListener)
    }

    fun onConnected(){
        // ui update
        if (playerStateSubscription != null && !playerStateSubscription!!.isCanceled){
            playerStateSubscription!!.cancel()
            playerStateSubscription = null
        }

        playerContextSubscription = spotifyAppRemote!!.playerApi.subscribeToPlayerContext()
                .setEventCallback(playerContextEventCallBack)
                .setErrorCallback {
                    Log.d(TAG, "something went wrong.")
                } as Subscription<PlayerContext>

        playerStateSubscription = spotifyAppRemote!!.playerApi
                .subscribeToPlayerState()
                .setEventCallback(playerStateEventCallBack)
                .setLifecycleCallback(object : Subscription.LifecycleCallback{
                    override fun onStart() {
                        Log.d(TAG, "on start")
                    }

                    override fun onStop() {
                        Log.d(TAG, "on stop")
                    }
                })
                .setErrorCallback{
                    Log.d(TAG, "Error")
                } as Subscription<PlayerState>?
    }

    fun play(trackURI: String?){
        spotifyAppRemote!!.playerApi
                .play(trackURI)
                .setResultCallback { Log.d(TAG, "Play successful") }
                .setErrorCallback { Log.d(TAG, "something went wrong.") }
    }

    fun playNextSong(){
        val db = FirebaseFirestore.getInstance()
        val partyId = (requireActivity() as JukeBoxActivity).partyID
        db.collection("Parties").document(partyId)
                .collection("Queue")
                .orderBy(Song.FIELD_PLACE_IN_QUEUE, Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { document ->
                    val nextSong = document.first().toObject(Song::class.java)
                    play(nextSong.songURI)
                }
    }

    // player controls

    fun onSkipPrevious(){
        spotifyAppRemote!!.playerApi.skipPrevious()
                .setResultCallback { Log.d(TAG, "skip back") }
                .setErrorCallback { Log.d(TAG, "Error") }
    }

    fun onSkipNext(){
        (requireActivity() as JukeBoxActivity).updateQueue()
        playNextSong()
        lastSongPos = 10000
    }

    fun onPlayPauseClicked(){
        spotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState ->
            if(playerState.isPaused){
                spotifyAppRemote!!.playerApi
                        .resume()
                        .setResultCallback { Log.d(TAG, "resumed") }
                        .setErrorCallback { Log.d(TAG, "Error") }
            } else {
                spotifyAppRemote!!.playerApi
                        .pause()
                        .setResultCallback { Log.d(TAG, "paused") }
                        .setErrorCallback { Log.d(TAG, "Error") }
            }
        }
    }

    class TrackProgressBar(val seekBar: SeekBar?,
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
                            Log.d(TAG, "Errrrrrrooooooorrrrrrr")
                        }
            }
        }

        init {
            seekBar!!.setOnSeekBarChangeListener(seekBarChangeListener)
        }

        private val seekRunnable : Runnable = object : Runnable {
            override fun run() {
                val progress = seekBar!!.progress
                seekBar.progress = (progress + LOOP_DURATION).toInt()
                handler.postDelayed(this, LOOP_DURATION)
            }
        }

        fun setDuration(duration: Long){
            seekBar!!.max = duration.toInt()
        }

        fun update(progress : Long){
            seekBar!!.progress = progress.toInt()
            PlayerFragment.lastSongPos = 10000
        }

        fun pause(){
            handler.removeCallbacks(seekRunnable)
        }

        fun unpause(){
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

        @JvmStatic
        var lastSongPos : Long = 10000
    }
}
