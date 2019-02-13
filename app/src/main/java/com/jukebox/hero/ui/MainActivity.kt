package com.jukebox.hero.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.jukebox.hero.Models.PartyQueue
import com.jukebox.hero.services.PmqPartyQueueService
import com.jukebox.hero.ui.adapters.PartyQueueAdapter
import com.jukebox.hero.util.SaveSharedPreference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import com.jukebox.hero.R
import com.jukebox.hero.ui.adapters.SimpleFragmentPagerAdapter
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import kaaes.spotify.webapi.android.SpotifyApi

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){

    var spotifyAuthToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // set up bottom took bar.
        viewpager.adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        navigation.setupWithViewPager(viewpager)

        navigation.getTabAt(0)?.setIcon(R.drawable.ic_view_agenda_black_24dp)
        navigation.getTabAt(0)?.text = ""

        navigation.getTabAt(1)?.setIcon(R.drawable.ic_playlist_add_black_24dp)
        navigation.getTabAt(1)?.text = ""

        navigation.getTabAt(2)?.setIcon(R.drawable.ic_perm_identity_black_24dp)
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
            R.id.action_settings -> true
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
    }
}