package com.jukebox.hero.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.model.Document

import com.jukebox.hero.R
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [testOne.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [testOne.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvParty: TextView
    private lateinit var btnHostButton : Button
    private lateinit var btnLeaveButton : Button
    private lateinit var tvRoomCode: TextView
    private lateinit var tvJoinParty: TextView
    private lateinit var etRoomCode: EditText
    private lateinit var btnJoinParty: Button
    private lateinit var tvNearbyParties: TextView
    private lateinit var svParties: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inflate the layout for this fragment
//        Log.d(TAG, "onCreateView")
//        val view = inflater.inflate(R.layout.activity_home, container, false)
        //val test : TextView = view!!.findViewById(R.id.test)
        //test.text = param1

        tvParty = findViewById(R.id.tvParty)

        btnHostButton = findViewById(R.id.btnCreateParty)
        btnHostButton.setOnClickListener(View.OnClickListener {
            onHostPartyClicked()
        })

        btnLeaveButton = findViewById(R.id.btnLeaveParty)
        btnLeaveButton.setOnClickListener(View.OnClickListener {
            onLeavePartyClicked()
        })

        tvRoomCode = findViewById(R.id.tvRoomCode)
        tvJoinParty = findViewById(R.id.tvJoinParty)
        etRoomCode = findViewById(R.id.etRoomCode)

        btnJoinParty = findViewById(R.id.btnJoinByRoomCode)
        btnJoinParty.setOnClickListener(View.OnClickListener {
            onJoinPartyClicked()
        })

        tvNearbyParties = findViewById(R.id.tvNearbyParties)
        svParties = findViewById(R.id.svNearbyParties)

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

                        addUserToParty(data["HostId"].toString(), uid)

                        setCurrentParty(uid, data["HostId"].toString())

                        getUsersCurrentParty(uid)

                    } else {
                        Toast.makeText(this, "No parties with that room code.", Toast.LENGTH_SHORT).show()
                    }

                }
    }

    fun addUserToParty(party: String, uid: String) {
        val partyDoc = firestore.collection("Parties").document(party)
        partyDoc.get()
            .addOnSuccessListener { doc ->
                    if (doc != null) {
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
        var currentParty:String? = null
        val user = firestore.collection("Users").document(uid)

        user.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "Successfully pulled user with id " + uid)
                        currentParty = document.data!!["CurrentParty"] as String?

                        if (currentParty != null) {
                            val partyDoc = firestore.collection("Parties").document(currentParty.toString())
                            partyDoc.get()
                                    .addOnSuccessListener { doc2 ->
                                        if (doc2 != null) {
                                            Log.d(TAG, "Successfully pulled party with id " + currentParty.toString())
                                            party = doc2.data!! as HashMap<String, Any>
                                            swapToPartyingElements(party!!)
                                        }
                                    }
                        } else {
                            Log.d(TAG, "currentParty was null")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error checking if user has a party: ", exception)
                }



        return party

    }


    fun onHostPartyClicked() {
        val party = createParty()
        swapToPartyingElements(party)
    }

    fun onLeavePartyClicked() {
        leaveParty()
        swapToNotPartyingElements()
    }

    fun createParty(): HashMap<String, Any> {
        val uid = auth.currentUser!!.uid
        val party = HashMap<String, Any>()
        party.put("HostId", uid)
        party.put("Users", List(1) { uid })
        party.put("RoomCode", createRoomCode())

        val userDoc = firestore.collection("Users").document(uid)
        userDoc.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.data)
                        val user = document.data!!
                        user["CurrentParty"] = uid
                        userDoc.set(user)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

        firestore.collection("Parties").document(auth.currentUser!!.uid)
                .set(party)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        return party
    }

    fun createRoomCode():String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val codeArray = List(5) {chars[Random.nextInt(0, chars.length)]}
        return codeArray.joinToString("")
    }

    fun swapToPartyingElements(party: HashMap<String, Any>) {
        btnHostButton.visibility = View.GONE
        tvParty.text = getText(R.string.home_partying)
        tvRoomCode.visibility = View.VISIBLE
        tvRoomCode.text = getString(R.string.room_code_display, party["RoomCode"].toString())
        btnLeaveButton.visibility = View.VISIBLE

        tvJoinParty.visibility = View.GONE
        etRoomCode.visibility = View.GONE
        btnJoinParty.visibility = View.GONE
        tvNearbyParties.visibility = View.GONE
        svParties.visibility = View.GONE
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

                        userDoc.set(hashMapOf("CurrentParty" to null))

                        if (currentParty.isNotEmpty()) {
                            val partyDoc = firestore.collection("Parties").document(currentParty)
                            partyDoc.get()
                                    .addOnSuccessListener { doc ->
                                        if (doc != null) {
                                            Log.d(TAG, "DocumentSnapshot data: " + doc.data)
                                            val party = doc.data!!
                                            val users = party["Users"] as MutableList<Any>
                                            users.remove(uid)

                                            if (users.isEmpty() || uid == currentParty)
                                                deleteDocument(partyDoc)

                                            else {
                                                party["Users"] = users
                                                partyDoc.set(party)
                                            }

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

    fun deleteDocument(doc: DocumentReference) {
        doc.delete()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "Successfully deleted Party")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "Delete failed with ", e)
                }
    }

    fun swapToNotPartyingElements() {
        btnHostButton.visibility = View.VISIBLE
        tvParty.text = getText(R.string.home_not_partying)
        tvRoomCode.visibility = View.GONE

        btnLeaveButton.visibility = View.GONE

        tvJoinParty.visibility = View.VISIBLE
        etRoomCode.visibility = View.VISIBLE
        btnJoinParty.visibility = View.VISIBLE
        tvNearbyParties.visibility = View.VISIBLE
        svParties.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "Home Activity"
    }
}
