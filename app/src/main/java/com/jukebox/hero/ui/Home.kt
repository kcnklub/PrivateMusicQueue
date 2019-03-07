package com.jukebox.hero.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import com.jukebox.hero.R
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
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
class Home : Fragment() {

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
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        //val test : TextView = view!!.findViewById(R.id.test)
        //test.text = param1

        tvParty = view.findViewById(R.id.tvParty)

        btnHostButton = view.findViewById(R.id.btnCreateParty)
        btnHostButton.setOnClickListener(View.OnClickListener {
            onHostPartyClicked()
        })

        btnLeaveButton = view.findViewById(R.id.btnLeaveParty)
        btnLeaveButton.setOnClickListener(View.OnClickListener {
            onLeavePartyClicked()
        })

        tvRoomCode = view.findViewById(R.id.tvRoomCode)
        tvJoinParty = view.findViewById(R.id.tvJoinParty)
        etRoomCode = view.findViewById(R.id.etRoomCode)
        btnJoinParty = view.findViewById(R.id.btnJoinByRoomCode)
        tvNearbyParties = view.findViewById(R.id.tvNearbyParties)
        svParties = view.findViewById(R.id.svNearbyParties)

        return view
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
                                .addOnSuccessListener { Log.d(Home.TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(Home.TAG, "Error writing document", e) }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

        firestore.collection("Parties").document(auth.currentUser!!.uid)
                .set(party)
                    .addOnSuccessListener { Log.d(Home.TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(Home.TAG, "Error writing document", e) }
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
                                                partyDoc.delete()
                                                        .addOnSuccessListener { d3 ->
                                                            if (d3 != null) {
                                                                Log.d(TAG, "Successfully deleted Party")
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.d(TAG, "Delete failed with ", e)
                                                        }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment testOne.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                Home().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        private const val TAG = "testOne"
    }
}
