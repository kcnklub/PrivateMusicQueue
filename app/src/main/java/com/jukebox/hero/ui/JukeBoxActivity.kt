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
import com.google.firebase.firestore.WriteBatch
import com.jukebox.hero.Models.Song
import com.jukebox.hero.R
import com.jukebox.hero.ui.adapters.SimpleFragmentPagerAdapter
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse

import kotlinx.android.synthetic.main.activity_main.*

class JukeBoxActivity : AppCompatActivity(){

    lateinit var partyID : String
    lateinit var ownerID : String
    lateinit var currentUser : String
    var isOwner : Boolean? = false

    lateinit var db : FirebaseFirestore

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
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
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
            R.id.party_manager -> {
                val intent = Intent(this, HomeActivity::class.java)
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

    fun updateQueue(){
        db = FirebaseFirestore.getInstance()
        val query = db.collection("Parties")
                .document(partyID).collection("Queue")
                .orderBy(Song.FIELD_PLACE_IN_QUEUE, Query.Direction.ASCENDING)

        query.get().addOnSuccessListener {documents ->
            val batch : WriteBatch = db.batch()
            for(document in documents){
                val docRef = db.collection("Parties").document(partyID).collection("Queue").document(document.id)
                val songInQueue = document.toObject(Song::class.java)
                if(songInQueue.placeInQueue == 1){
                    batch.delete(docRef)
                } else {
                    batch.update(docRef, Song.FIELD_PLACE_IN_QUEUE, songInQueue.placeInQueue!!.minus(1))
                }
            }
            batch.commit().addOnCompleteListener{
                Log.d(TAG, "we made it.")
            }
        }
    }

    companion object {
        const val TAG = "Main Activity"
        const val SPOTIFY_REQUEST_CODE = 1337
        const val REDIRECT_URL = "https://google.com"
        const val CLIENT_ID = "a323c442192a47a6a3cd7e93a318f080"
        var spotifyAuthToken = ""
    }
}