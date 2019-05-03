package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.adapters.SimpleFragmentPagerAdapter
import com.jukebox.hero.ui.fragments.JukeboxHomeFragment
import com.jukebox.hero.ui.fragments.PlayerFragment
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kotlinx.android.synthetic.main.activity_main.*

class JukeBoxActivity : AppCompatActivity(){

    lateinit var partyID : String
    private lateinit var ownerID : String
    private lateinit var currentUser : String
    var isOwner : Boolean? = false

    private lateinit var db : FirebaseFirestore

    lateinit var playerFragment: PlayerFragment
    lateinit var jukeboxHomeFragment : JukeboxHomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        partyID = intent.getStringExtra("partyQueueId")
        ownerID = intent.getStringExtra("OwnerId")
        currentUser = FirebaseAuth.getInstance().currentUser!!.uid

        if(ownerID == currentUser){
            isOwner = true
        }
        Log.d(TAG, "$currentUser is the owner $isOwner")
        Log.d(TAG, "We are in party $partyID")

        // set up bottom took bar.
        viewpager.adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        navigation.setupWithViewPager(viewpager)

        navigation.getTabAt(0)?.setIcon(R.drawable.ic_view_agenda_white_24dp)
        navigation.getTabAt(0)?.text = ""

        navigation.getTabAt(1)?.setIcon(R.drawable.ic_play_arrow_white_24dp)
        navigation.getTabAt(1)?.text = ""

        navigation.getTabAt(2)?.setIcon(R.drawable.ic_search_white_24dp)
        navigation.getTabAt(2)?.text = ""

        jukeboxHomeFragment = (viewpager.adapter as SimpleFragmentPagerAdapter).getItem(0) as JukeboxHomeFragment
        playerFragment = (viewpager.adapter as SimpleFragmentPagerAdapter).getItem(1) as PlayerFragment

        // spotify shit
        val builder = AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URL)
        builder.setScopes(arrayOf("user-read-private", "streaming"))
        val request = builder.build()
        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.jukebox_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.leave_party -> {
                HomeActivity.leaveParty(this)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.log_out -> run {
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPOTIFY_REQUEST_CODE){
            val response = AuthenticationClient.getResponse(resultCode, data)
            if(response.type == AuthenticationResponse.Type.TOKEN){
                spotifyAuthToken = response.accessToken
                Log.d(TAG, "!!!!!!!!!!AUTH TOKEN: $spotifyAuthToken")
            }
        }
    }

    companion object {
        const val TAG = "Main Activity"
        const val SPOTIFY_REQUEST_CODE = 1337
        const val REDIRECT_URL = "https://google.com"
        const val CLIENT_ID = "a323c442192a47a6a3cd7e93a318f080"
        var spotifyAuthToken = ""

        fun updateQueue(partyId: String){
            FirebaseFirestore.getInstance().collection("Parties").document(partyId)
                    .collection("Queue")
                    .orderBy(Song.FIELD_SCORE, Query.Direction.DESCENDING)
                    .orderBy(Song.FIELD_QUEUE_TIME, Query.Direction.ASCENDING)
                    .limit(1)
                    .get().addOnSuccessListener {
                        if(!it.isEmpty){
                            it.documents.first().reference.delete()
                        }
                    }
        }
    }
}