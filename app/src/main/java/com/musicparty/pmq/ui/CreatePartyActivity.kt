package com.musicparty.pmq.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.View
import android.widget.*
import com.musicparty.pmq.Models.PartyQueue
import com.musicparty.pmq.R
import com.musicparty.pmq.services.PmqPartyQueueService
import com.musicparty.pmq.util.SaveSharedPreference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_create_party.*

class CreatePartyActivity : AppCompatActivity() {

    private var disposable : Disposable? = null
    private val pmqPartyQueueService by lazy {
        PmqPartyQueueService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_party)
        setSupportActionBar(toolbar)

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
            if (isEmpty(partyPassPhrase)) {
                partyPassPhraseView.error = getString(R.string.error_required_field)
                focusView = partyPassPhraseView
                cancel = true
            }

            if (isEmpty(partyPassPhraseConfirm)) {
                partyPassPhraseConfirmView.error = getString(R.string.error_required_field)
                focusView = partyPassPhraseConfirmView
                cancel = true
            }

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
            disposable = pmqPartyQueueService.createParty(
                    PartyQueue.Body(SaveSharedPreference.getLoggedInUserId(applicationContext),
                            partyName.toString(),
                            partyPassPhrase.toString(),
                            isPrivate))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result -> kotlin.run {
                                if(result.message == "success"){
                                    finishCreation(result.partyQueueId)
                                } else {
                                    throw Exception(result.message)
                                }
                            }}, { error -> run {
                                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                                Log.d("CreatePartyActivity", error.toString())
                            }}
                    )
        }
    }

    private fun finishCreation(partyQueueId : Int){
        val intent = Intent(this, PartyViewActivity::class.java)
        intent.putExtra("partyQueueId", partyQueueId)
        startActivity(intent)
    }
}
