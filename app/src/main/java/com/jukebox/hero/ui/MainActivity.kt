package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.jukebox.hero.Models.PartyQueue
import com.jukebox.hero.services.PmqPartyQueueService
import com.jukebox.hero.ui.Adapters.PartyQueueAdapter
import com.jukebox.hero.util.SaveSharedPreference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var disposable : Disposable? = null
    private val pmqPartyQueueService by lazy {
        PmqPartyQueueService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val createPartyButton = findViewById<Button>(R.id.create_party)
        createPartyButton.setOnClickListener {
            val intent = Intent(this, CreatePartyActivity::class.java)
            startActivity(intent)
        }

        val userId = SaveSharedPreference.getLoggedInUserId(this)
        disposable = pmqPartyQueueService.getAllUserParties(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {result -> run {
                            if(!result.isEmpty()){
                                setUpListView(result)
                            } else {
                                throw Exception("result is empty")
                            }
                        }}, {error -> run {
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                }}
                )
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
                SaveSharedPreference.setLoggedIn(applicationContext, false, 0)
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpListView(list: List<PartyQueue.PartyQueue>){
        val parties = findViewById<ListView>(R.id.party_list)
        val partyQueueAdapter = PartyQueueAdapter(this, R.layout.listview_partyqueue_item_row, list)
        parties.adapter = partyQueueAdapter
    }
}