package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jukebox.hero.R
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

    private lateinit var tvPartyName: TextView
    private lateinit var tvRoomCode: TextView

    private lateinit var tvJoinParty: TextView
    private lateinit var etRoomCode: EditText
    private lateinit var btnJoinParty: Button
    private lateinit var tvNearbyParties: TextView
    private lateinit var svParties: ScrollView

    private lateinit var divider1: View
    private lateinit var divider2: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvParty = findViewById(R.id.tvParty)

        etPartyName = findViewById(R.id.etPartyName)

        btnHostButton = findViewById(R.id.btnCreateParty)
        btnHostButton.setOnClickListener(View.OnClickListener {
            attemptCreateParty()
        })

        btnLeaveButton = findViewById(R.id.btnLeaveParty)
        btnLeaveButton.setOnClickListener(View.OnClickListener {
            onLeavePartyClicked()
        })

        tvPartyName = findViewById(R.id.tvPartyName)
        tvRoomCode = findViewById(R.id.tvRoomCode)

        tvJoinParty = findViewById(R.id.tvJoinParty)
        etRoomCode = findViewById(R.id.etRoomCode)

        btnJoinParty = findViewById(R.id.btnJoinByRoomCode)
        btnJoinParty.setOnClickListener(View.OnClickListener {
            onJoinPartyClicked()
        })

        tvNearbyParties = findViewById(R.id.tvHistory)
        svParties = findViewById(R.id.svHistory)

        divider1 = findViewById(R.id.divider)
        divider2 = findViewById(R.id.divider2)

        getUsersCurrentParty(auth.currentUser!!.uid)
    }



    fun onJoinPartyClicked() {
        val roomCode: String = etRoomCode.text.toString()
        val uid = auth.currentUser!!.uid
        firestore.collection("Parties")
                .whereEqualTo("RoomCode", roomCode)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val data = result.documents.first().data!!
                        val hostId = data["HostId"].toString()
                        val name = data["PartyName"].toString()
                        val partyId = hostId + name.replace(" ", "")

                        addUserToParty(partyId, uid)

                        setCurrentParty(uid, partyId)

                        addPartyToHistory(uid, partyId)

                        getUsersCurrentParty(uid)

                        finishCreation(partyId, hostId)

                    } else {
                        Toast.makeText(this, "No parties with that information.", Toast.LENGTH_SHORT).show()
                    }

                }
    }

    fun addPartyToHistory(uid: String, partyid: String) {
        val userDoc = firestore.collection("Users").document(uid)
        userDoc.get()
                .addOnSuccessListener { doc ->
                    if (doc != null) {
                        val data = doc.data!!
                        if (data.containsKey("History")) {
                            val history = data["History"] as MutableList<Any>

                            history.add(0, partyid)
                            for (i in 1..history.size-1) {
                                if (history[i].toString() == partyid) {
                                    history.removeAt(i)
                                    break
                                }
                            }
                            data["History"] = history
                        } else {
                            val history = mutableListOf(partyid)
                            data["History"] = history
                        }

                        updateUserData(uid, data)
                    }
                }
    }

    fun updateUserData(uid: String, data: Map<String, Any>) {
        val userDoc = firestore.collection("Users").document(uid)
        userDoc.update(data)
                .addOnSuccessListener {
                    Log.d(TAG, "User object successfully updated.")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating user object.", exception)
                }
    }

    fun addUserToParty(party: String, uid: String) {
        val partyDoc = firestore.collection("Parties").document(party)
        partyDoc.get()
            .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val data = doc.data!!
                        val users = data["Users"] as MutableList<Any>
                        users.add(uid)
                        data["Users"] = users

                        partyDoc.set(data)
                                .addOnSuccessListener { partyDoc ->
                                    if (partyDoc != null) {
                                        Log.d(TAG,"Added user " + uid + " to party " + party)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error adding user to party: ", exception)
                                }
                    }
                }

    }

    fun setCurrentParty(uid: String, party: String) {
        firestore.collection("Users").document(uid)
                .update(hashMapOf("CurrentParty" to party as Any))
                .addOnSuccessListener { userDoc ->
                    if (userDoc != null) {
                        Log.d(TAG, "Updated current party to " + party + " for user " + uid)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating current party for user", exception)
                }
    }

    fun getUsersCurrentParty(uid : String): HashMap<String, Any>? {
        var party : HashMap<String, Any>? = null
        val userDoc = firestore.collection("Users").document(uid)

        userDoc.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d(TAG, "Successfully pulled user with id " + uid)
                        val currentParty = if (document.data!!.keys.contains("CurrentParty")) {
                            document.data!!["CurrentParty"] as String?
                        } else {
                            null
                        }

                        if (currentParty != null) {
                            val partyDoc = firestore.collection("Parties").document(currentParty.toString())
                            partyDoc.get()
                                    .addOnSuccessListener { doc2 ->
                                        if (doc2.exists()) {
                                            Log.d(TAG, "Successfully pulled party with id " + currentParty.toString())
                                            party = doc2.data!! as HashMap<String, Any>
                                            swapToPartyingElements(party!!)
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

        var cancel = false
        var focusView: View = etPartyName

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

                        val userDoc = firestore.collection("Users").document(uid)
                        userDoc.get()
                                .addOnSuccessListener {
                                    if(it.exists()){
                                        val user = it.data!!
                                        if (!(user["HostedParties"] as List<String>).contains(partyid)) {
                                            user["CurrentParty"] = uid + partyid

                                            val hostedParties = user["HostedParties"] as MutableList<String>
                                            hostedParties.add(partyName.toString())
                                            user["HostedParties"] = hostedParties

                                            userDoc.update(user)
                                                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapShot Successfully written!") }
                                                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                                            val partyDoc = firestore.collection("Parties").document(auth.currentUser!!.uid + partyName.toString().replace(" ", ""))
                                            partyDoc.set(party)
                                                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                                            finishCreation(uid + partyid, uid)
                                        } else {
                                            Toast.makeText(this, "You already have an active party with that name.", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Log.d(TAG, "No such document")
                                    }
                                }
                                .addOnFailureListener {
                                    Log.d(TAG, "get failed with ", it)
                                }
                    }
                    .addOnFailureListener { exc ->
                        Log.e(TAG, "Error getting room code list", exc)
                    }
        }
    }

    fun createRoomCode():String {
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

    fun onLeavePartyClicked() {
        leaveParty()
        swapToNotPartyingElements()
    }



    fun swapToPartyingElements(party: HashMap<String, Any>) {
        tvParty.visibility = View.VISIBLE
        btnHostButton.visibility = View.GONE
        tvParty.text = getText(R.string.home_partying)

        etPartyName.visibility = View.GONE

        tvPartyName.visibility = View.VISIBLE
        tvPartyName.text = getString(R.string.party_name_display, party["PartyName"].toString())

        tvRoomCode.visibility = View.VISIBLE
        tvRoomCode.text = getString(R.string.party_room_code_display, party["RoomCode"].toString())

        btnLeaveButton.visibility = View.VISIBLE

        tvJoinParty.visibility = View.GONE
        etRoomCode.visibility = View.GONE
        btnJoinParty.visibility = View.GONE
        tvNearbyParties.visibility = View.GONE
        svParties.visibility = View.GONE

        divider1.visibility = View.GONE
        divider2.visibility = View.GONE
    }

    fun leaveParty() {
        val uid = auth.currentUser!!.uid
        val userDoc = firestore.collection("Users").document(uid)
        userDoc.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.data)
                        val user = document.data!!
                        val currentParty = user["CurrentParty"].toString()

                        userDoc.update(hashMapOf("CurrentParty" to null) as MutableMap<String, Any>)

                        if (currentParty.isNotEmpty()) {
                            val partyDoc = firestore.collection("Parties").document(currentParty)
                            partyDoc.get()
                                    .addOnSuccessListener { doc ->
                                        if (doc != null) {
                                            Log.d(TAG, "DocumentSnapshot data: " + doc.data)
                                            val party = doc.data!!
                                            val users = party["Users"] as MutableList<Any>
                                            users.remove(uid)

                                            //if (users.isEmpty() || uid == currentParty)
                                                //deleteDocument(partyDoc)

                                            //else {
                                                party["Users"] = users
                                                partyDoc.set(party)
                                            //}

                                        } else {
                                            Log.d(TAG, "No such document")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d(TAG, "get failed with ", exception)
                                    }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
        }
    }

    fun swapToNotPartyingElements() {
        tvParty.visibility = View.VISIBLE
        tvParty.text = getText(R.string.home_not_partying)

        etPartyName.visibility = View.VISIBLE
        btnHostButton.visibility = View.VISIBLE

        tvPartyName.visibility = View.GONE
        tvRoomCode.visibility = View.GONE

        btnLeaveButton.visibility = View.GONE

        tvJoinParty.visibility = View.VISIBLE
        etRoomCode.visibility = View.VISIBLE
        btnJoinParty.visibility = View.VISIBLE
        tvNearbyParties.visibility = View.VISIBLE
        svParties.visibility = View.VISIBLE

        divider1.visibility = View.VISIBLE
        divider2.visibility = View.VISIBLE
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
            R.id.main_activity -> {
                val intent = Intent(this, JukeBoxActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "Home Activity"
    }
}
