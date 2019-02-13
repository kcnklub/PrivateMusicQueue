package com.jukebox.hero.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
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

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        val createPartyButton = findViewById<Button>(R.id.create_party)
//        createPartyButton.setOnClickListener {
//            val intent = Intent(this, CreatePartyActivity::class.java)
//            startActivity(intent)
//        }

//        navigation.setOnNavigationItemSelectedListener(
//                object : BottomNavigationView.OnNavigationItemSelectedListener{
//                    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
//                        var selectedFragment : Fragment? = null
//                        when(p0.itemId){
//                            R.id.action_item1 -> {
//                                selectedFragment = testOne.newInstance("test", "test")
//                            }
//                            R.id.action_item2 -> {
//                                selectedFragment = testOne.newInstance("test2", "test2")
//                            }
//                            R.id.action_item3 -> {
//                                selectedFragment = testOne.newInstance("test3", "test3")
//                            }
//                        }
//                        val transaction = supportFragmentManager.beginTransaction()
//                        transaction.replace(R.id.frame_layout, selectedFragment!!)
//                        transaction.commit()
//                        return true
//                    }
//                }
//        )

//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.frame_layout, testOne.newInstance("test", "test"))
//        transaction.commit()

        viewpager.adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        navigation.setupWithViewPager(viewpager)
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

    private fun setUpListView(list: List<PartyQueue.PartyQueue>){
        val parties = findViewById<ListView>(R.id.party_list)
        val partyQueueAdapter = PartyQueueAdapter(this, R.layout.listview_partyqueue_item_row, list)
        parties.adapter = partyQueueAdapter
    }
}