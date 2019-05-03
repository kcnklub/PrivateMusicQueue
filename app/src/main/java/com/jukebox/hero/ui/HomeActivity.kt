package com.jukebox.hero.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jukebox.hero.Models.Party
import com.jukebox.hero.Models.User
import com.jukebox.hero.R
import com.jukebox.hero.ui.adapters.JukeboxAdapter
import java.util.*
import kotlin.random.Random

/**
 * Home activity where users will be when not in a party
 * They will be able to join a party from here and host a party.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvParty: TextView
    private lateinit var etPartyName: EditText
    private lateinit var btnHostButton : Button
    private lateinit var btnLeaveButton : Button

//    private lateinit var tvPartyName: TextView
    private lateinit var tvRoomCode: TextView

    private lateinit var tvJoinParty: TextView
    private lateinit var etRoomCode: EditText
    private lateinit var btnJoinParty: Button
    private lateinit var tvNearbyParties: TextView
    private lateinit var svParties: ScrollView

    private lateinit var divider1: View
    private lateinit var divider2: View

    private lateinit var jukeboxListView : RecyclerView
    private lateinit var userJukeboxQuery : Query
    private lateinit var jukeboxAdapter: JukeboxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etPartyName = findViewById(R.id.etPartyName)

        btnHostButton = findViewById(R.id.btnCreateParty)
        btnHostButton.setOnClickListener(View.OnClickListener {
            attemptCreateParty()
        })

        btnLeaveButton = findViewById(R.id.btnLeaveParty)
        btnLeaveButton.setOnClickListener(View.OnClickListener {
            onLeavePartyClicked()
        })

        tvJoinParty = findViewById(R.id.tvJoinParty)
        etRoomCode = findViewById(R.id.etRoomCode)

        btnJoinParty = findViewById(R.id.btnJoinByRoomCode)
        btnJoinParty.setOnClickListener(View.OnClickListener {
            onJoinPartyClicked()
        })

        tvNearbyParties = findViewById(R.id.tvHistory)

        divider1 = findViewById(R.id.divider)
        divider2 = findViewById(R.id.divider2)

        getUsersCurrentParty(auth.currentUser!!.uid)

        jukeboxListView = findViewById(R.id.user_jukeboxes)
        userJukeboxQuery = firestore.collection("Users").document(auth.currentUser!!.uid)
                .collection("userParties")
        jukeboxAdapter = JukeboxAdapter(userJukeboxQuery, this)
        jukeboxListView.layoutManager = LinearLayoutManager(this)
        jukeboxListView.adapter = jukeboxAdapter
        jukeboxAdapter.startListening()
    }



    fun onJoinPartyClicked() {
        val roomCode: String = etRoomCode.text.toString()
        val uid = auth.currentUser!!.uid
        firestore.collection("Parties")
                .whereEqualTo(Party.FIELD_ROOM_CODE, roomCode)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val partyId = result.documents.first().id
                        val party = result.documents.first().toObject(Party::class.java)
                        val data = result.documents.first().data!!
                        val hostId = data["HostId"].toString()
                        val name = data["PartyName"].toString()
//                        val partyId = hostId + name.replace(" ", "")

                        addUserToParty(partyId)

                        setCurrentParty(uid, partyId)

                        addPartyToHistory(uid, partyId)

                        getUsersCurrentParty(uid)

                        finishCreation(partyId, hostId)

                    } else {
                        Toast.makeText(this, "No parties with that information.", Toast.LENGTH_SHORT).show()
                    }

                }
    }

    private fun addPartyToHistory(uid: String, partyId: String) {
        val userDoc = firestore.collection("Users").document(uid)
        userDoc.get()
                .addOnSuccessListener { doc ->
                    if (doc != null) {
                        val data = doc.data!!
                        if (data.containsKey("History")) {
                            val history = data["History"] as MutableList<Any>

                            history.add(0, partyId)
                            for (i in 1..history.size-1) {
                                if (history[i].toString() == partyId) {
                                    history.removeAt(i)
                                    break
                                }
                            }
                            data["History"] = history
                        } else {
                            val history = mutableListOf(partyId)
                            data["History"] = history
                        }

                        updateUserData(uid, data)
                    }
                }
    }

    private fun updateUserData(uid: String, data: Map<String, Any>) {
        val userDoc = firestore.collection("Users").document(uid)
        userDoc.update(data)
                .addOnSuccessListener {
                    Log.d(TAG, "User object successfully updated.")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating user object.", exception)
                }
    }

    private fun addUserToParty(party: String) {
        val partyMembers = firestore.collection("Parties").document(party).collection("members").document()
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val member = User(null, FirebaseAuth.getInstance().currentUser!!.uid, null)
            transaction.set(partyMembers, member)
            null
        }
    }

    private fun setCurrentParty(uid: String, party: String) {
        firestore.collection("Users").document(uid)
                .update(hashMapOf("currentParty" to party as Any))
                .addOnSuccessListener { userDoc ->
                    if (userDoc != null) {
                        Log.d(TAG, "Updated current party to " + party + " for user " + uid)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating current party for user", exception)
                }
    }

    private fun getUsersCurrentParty(uid : String): Party? {
        var party : Party? = null
        val userDoc = firestore.collection("Users").document(uid)

        userDoc.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d(TAG, "Successfully pulled user with id " + uid)
                        val currentParty = if (document.data!!.keys.contains("currentParty")) {
                            document.data!!["currentParty"] as String?
                        } else {
                            null
                        }

                        if (currentParty != null) {
                            val partyDoc = firestore.collection("Parties").document(currentParty.toString())
                            partyDoc.get()
                                    .addOnSuccessListener { doc2 ->
                                        if (doc2.exists()) {
                                            Log.d(TAG, "Successfully pulled party with id " + currentParty.toString())
                                            party = doc2.toObject(Party::class.java)
//                                            swapToPartyingElements(party!!)
                                            val intent = Intent(this, JukeBoxActivity::class.java)
                                            intent.putExtra("partyQueueId", doc2.id)
                                            intent.putExtra("OwnerId", party!!.hostId as String)
                                            startActivity(intent)
                                        }
                                    }
                        } else {
                            Log.d(TAG, "currentParty was null")
                            swapToNotPartyingElements()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error checking if user has a party: ", exception)
                }



        return party

    }

    private fun attemptCreateParty(){

        val partyName = etPartyName.text

        if (TextUtils.isEmpty(partyName)) {
            Toast.makeText(this, "Party name cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            //create party.
            val uid = auth.currentUser!!.uid
            val party = HashMap<String, Any>()
            party["HostId"] = uid
            party["Users"] = List(1){ uid }
            party["PartyName"] = partyName.toString()
            party["Queue"] = List(0){}
            val partyid = partyName.toString().replace(" ", "")
            val newParty = Party(uid, createRoomCode(), partyName.toString())

            firestore.collection("Parties")
                    .get()
                    .addOnSuccessListener { query ->
                        val roomCodes = mutableListOf<String>()
                        for (doc in query.documents) {
                            roomCodes.add(doc.data!!["RoomCode"].toString())
                        }
                        do {
                            party["RoomCode"] = createRoomCode()
                        } while (roomCodes.contains(party["RoomCode"].toString()))

                        val userParties = firestore.collection("Users").document(uid).collection("myParties")
                        userParties.get()
                                .addOnSuccessListener {documents ->
                                    var nameUsed = false
                                    for(document in documents){
                                        val oldParty = document.toObject(Party::class.java)
                                        if(oldParty.partyName == partyName.toString()){
                                            nameUsed = true
                                        }
                                    }

                                    if(!nameUsed){
                                        // this is a new name for a party so lets get this bread and make it.
                                        FirebaseFirestore.getInstance().runTransaction{transaction ->
                                            transaction.set(FirebaseFirestore.getInstance()
                                                    .collection("Users").document(uid)
                                                    .collection("userParties").document(),
                                                    newParty)
                                            null
                                        }.addOnSuccessListener {
                                                FirebaseFirestore.getInstance()
                                                        .collection("Users").document(uid)
                                                        .collection("userParties").whereEqualTo(Party.FIELD_NAME, partyName.toString()).get().addOnSuccessListener { docs ->
                                                            FirebaseFirestore.getInstance().runTransaction {
                                                                it.set(FirebaseFirestore.getInstance().collection("Parties").document(docs.documents.first().id), newParty)
                                                                null
                                                            }.addOnSuccessListener {
                                                                finishCreation(docs.documents.first().id, uid)
                                                            }
                                                        }
                                        }
                                    } else {
                                        Toast.makeText(this, "You have created a party with this name already", Toast.LENGTH_LONG).show()
                                    }
                                }
                    }
                    .addOnFailureListener { exc ->
                        Log.e(TAG, "Error getting room code list", exc)
                    }
        }
    }

    private fun createRoomCode():String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val codeArray = List(5) {chars[Random.nextInt(0, chars.length)]}
        return codeArray.joinToString("")
    }

    private fun finishCreation(partyQueueId : String, ownerId : String){
        val intent = Intent(this, JukeBoxActivity::class.java)
        intent.putExtra("partyQueueId", partyQueueId)
        intent.putExtra("OwnerId", ownerId)
        startActivity(intent)
    }

    private fun onLeavePartyClicked() {
//        leaveParty()
        swapToNotPartyingElements()
    }

    private fun swapToNotPartyingElements() {
//        tvParty.visibility = View.VISIBLE
//        tvParty.text = getText(R.string.home_not_partying)

        etPartyName.visibility = View.VISIBLE
        btnHostButton.visibility = View.VISIBLE

//        tvPartyName.visibility = View.GONE
//        tvRoomCode.visibility = View.GONE

        btnLeaveButton.visibility = View.GONE

        tvJoinParty.visibility = View.VISIBLE
        etRoomCode.visibility = View.VISIBLE
        btnJoinParty.visibility = View.VISIBLE
        tvNearbyParties.visibility = View.VISIBLE
//        svParties.visibility = View.VISIBLE

        divider1.visibility = View.VISIBLE
        divider2.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val TAG = "Home Activity"

        fun leaveParty(context: Context) {
            val auth = FirebaseAuth.getInstance()
            val fireStore = FirebaseFirestore.getInstance()
            val uid = auth.currentUser!!.uid
            // update the current party attr. on the user.
            val userDoc = fireStore.collection("Users").document(uid)
            userDoc.get()
                    .addOnSuccessListener { document ->
                        val user = document.toObject(User::class.java)
                        if (user!!.currentParty != null) {
                            val oldPartyId = user.currentParty.toString()
                            fireStore.runTransaction { transaction ->
                                transaction.update(userDoc, User.FIELD_CURRENT_PARTY, null)
                                null
                            }.addOnSuccessListener {
                                //remove the user from the member collection of the party.
                                fireStore.collection("Parties").document(oldPartyId)
                                        .collection("members").whereEqualTo(User.FIELD_USER_ID, uid)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if(!documents.isEmpty){
                                                for(doc in documents){
                                                    doc.reference.delete()
                                                }
                                            }
                                            val intent = Intent(context, HomeActivity::class.java)
                                            context.startActivity(intent)
                                        }
                            }
                        }
                    }
        }
    }
}
