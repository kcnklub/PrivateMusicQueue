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
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.JukeBoxActivity
import com.jukebox.hero.ui.JukeBoxActivity.Companion.CLIENT_ID
import com.jukebox.hero.ui.JukeBoxActivity.Companion.REDIRECT_URL
import com.jukebox.hero.ui.adapters.SongsAdapter
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp
import com.spotify.android.appremote.api.error.NotLoggedInException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.PlayerContext
import com.spotify.protocol.types.PlayerState
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PlayerFragment : Fragment(), SongsAdapter.OnSongChangeListener  {
    private var param1: String? = null
    private var param2: String? = null

    private var remainingTimeService : Thread? = null

    private var isOwner : Boolean = false

    private var playPauseButton : AppCompatImageButton? = null
    private var skipPreviousButton : AppCompatImageButton? = null
    private var skipNextButton : AppCompatImageButton? = null
    private var coverArtImageView : ImageView? = null

    private var seekBar : SeekBar? = null
    private var trackProgressBar : TrackProgressBar? = null

    private var playerStateSubscription : Subscription<PlayerState>? = null
    private var playerContextSubscription : Subscription<PlayerContext>? = null

    private var playerContextEventCallBack = Subscription.EventCallback<PlayerContext> {
        Log.d(TAG, "idk something is happening here. ")
    }

    private val playerStateEventCallBack = Subscription.EventCallback<PlayerState> { playerState ->

        if(playerState.playbackSpeed > 0){
            trackProgressBar!!.unPause()
        } else {
            trackProgressBar!!.pause()
        }

        if(playerState.isPaused){
            playPauseButton!!.setImageResource(R.drawable.btn_play)
            Log.d(TAG, "!!!! track is paused, we need to not change song !!!!")
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
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        isOwner = (this.requireContext() as JukeBoxActivity).isOwner!!
        (this.requireActivity() as JukeBoxActivity).playerFragment = this
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
            seekBar!!.visibility = View.INVISIBLE
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

        /* create a runnable we can pass to remainingTimeService that checks remaining time
        *  until it under 1333ms and then plays the next song. The runnable waits 1000ms after
        *  playing a new song before restarting the probes so it doesn't skip songs.
         */
        /*
        val runnable = Runnable {
            while (true) {
                // if we haven't played the next song yet, pull the current state
                val state = spotifyAppRemote!!.playerApi.playerState.await().data
                if(state != null) {
                    val remaining = state.track.duration - state.playbackPosition

                    // if the remaining time is under 1333ms, play the next song
                    if (remaining < 1333) {
                        (requireActivity() as JukeBoxActivity).updateQueue()
                        playNextSong()
                        Thread.sleep(1000) // sleep the thread for a moment so we don't skip songs
                    }

                    Thread.sleep(25) // interval between state probes
                }
            }
        }
        remainingTimeService = Thread(runnable)
        remainingTimeService!!.start()
        */
    }

    fun play(trackURI: String?){
        spotifyAppRemote!!.playerApi
                .play(trackURI)
                .setResultCallback {
                    Log.d(TAG, "Play successful")
                }
                .setErrorCallback { Log.d(TAG, "something went wrong.") }
    }

    fun playNextSong(){
        val db = FirebaseFirestore.getInstance()
        val partyId = (requireActivity() as JukeBoxActivity).partyID
        db.collection("Parties").document(partyId)
                .collection("Queue")
                .orderBy(Song.FIELD_SCORE, Query.Direction.DESCENDING)
                .orderBy(Song.FIELD_QUEUE_TIME, Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { document ->
                    if(!document.isEmpty) {
                        val nextSong = document.first().toObject(Song::class.java)
                        play(nextSong.songURI)
                    } else {
                        Toast.makeText(requireContext(), "There is no song in queue!", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    // player controls

    private fun onSkipPrevious(){
        spotifyAppRemote!!.playerApi.skipPrevious()
                .setResultCallback { Log.d(TAG, "skip back") }
                .setErrorCallback { Log.d(TAG, "Error") }
    }

    private fun onSkipNext(){
        playNextSong()
        lastSongPos = 10000
    }

    private fun onPlayPauseClicked(){
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

    override fun onChange(song: Song?) {
        if(!this.isOwner && song != null){
            Picasso.get().load(song.albumArt).into(coverArtImageView)
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
                            Log.d(TAG, "Something is Wrong")
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

        fun unPause(){
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

        @JvmStatic
        var lastSongPos : Long = 10000
    }
}
