package com.pmq.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.pmq.hero.R
import kotlinx.android.synthetic.main.activity_display_name.*


class DisplayNameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_name)

        auth = FirebaseAuth.getInstance()

        etDisplayName.setText(auth.currentUser!!.displayName.toString())
    }

    fun setDisplayName(v: View) {

        val name = etDisplayName.text.toString()
        val changeRequest = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()
        auth.currentUser!!.updateProfile(changeRequest)

        val intent = Intent(this, JukeBoxActivity::class.java)
        startActivity(intent)
    }
}
