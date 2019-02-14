package com.jukebox.hero.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.jukebox.hero.R

class DisplayNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_name)
    }

    fun setDisplayName(v: View) {

        //TODO: SET CURRENTLY SIGNED IN USER'S DISPLAY NAME HERE

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
