package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jukebox.hero.Models.PartyQueue
import com.jukebox.hero.services.PmqPartyQueueService
import com.jukebox.hero.util.SaveSharedPreference
import com.jukebox.hero.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_create_party.*
import kotlin.random.Random

class CreatePartyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_party)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val createPartyButton = findViewById<Button>(R.id.create_party)
        createPartyButton.setOnClickListener{
            attemptCreateParty()
        }
    }

    private fun attemptCreateParty(){
        val partyNameView = findViewById<AutoCompleteTextView>(R.id.party_name)
        val partyPassPhraseView = findViewById<EditText>(R.id.party_passphrase)
        val partyPassPhraseConfirmView = findViewById<EditText>(R.id.confirm_party_passphrase)
        val checkBox = findViewById<CheckBox>(R.id.is_private_party)

        val partyName = partyNameView.text
        val partyPassPhrase = partyPassPhraseView.text
        val partyPassPhraseConfirm = partyPassPhraseConfirmView.text
        val isPrivate = checkBox.isChecked

        var cancel = false
        var focusView: View = partyNameView

        if (isEmpty(partyName)) {
            partyNameView.error = getString(R.string.error_required_field)
            focusView = partyNameView
            cancel = true
        }

        if(isPrivate){
            if ((partyPassPhrase.toString() != partyPassPhraseConfirm.toString())) {
                partyPassPhraseConfirmView.error = getString(R.string.passphrases_dont_match)
                focusView = partyPassPhraseConfirmView
                cancel = true
            }
        }

        if(cancel){
            focusView.requestFocus()
        } else {
            //create party.
            val uid = auth.currentUser!!.uid
            val party = HashMap<String, Any>()
            party["HostId"] = uid
            party["Users"] = List(1){ uid }
            party["PartyName"] = partyName.toString()
            party["isPrivate"] = isPrivate
            if(isPrivate){
                if(partyPassPhrase.isEmpty()){
                    party["RoomCode"] = createRoomCode()
                } else {
                    party["RoomCode"] = partyPassPhrase
                }
            }
            party["Queue"] = List(0){}

            val userDoc = firestore.collection("Users").document(uid)
            userDoc.get()
                    .addOnSuccessListener {
                        if(it != null){
                            val user = it.data!!
                            user["CurrentParty"] = uid
                            userDoc.update(user)
                                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapShot Successfully written!") }
                                    .addOnFailureListener{ e -> Log.w(TAG, "Error writting document", e)}
                        } else {
                            Log.d(TAG, "No such document")
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "get failed with ", it)
                    }
            firestore.collection("Parties").document(auth.currentUser!!.uid)
                    .set(party)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

            val intent = Intent(this, MainActivity::class.java)
            finishCreation(uid)
        }
    }

    private fun finishCreation(partyQueueId : String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("partyQueueId", partyQueueId)
        startActivity(intent)
    }

    fun createRoomCode():String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val codeArray = List(5) {chars[Random.nextInt(0, chars.length)]}
        return codeArray.joinToString("")
    }

    companion object {
        const val TAG = "Create Party Activity"
    }
}
