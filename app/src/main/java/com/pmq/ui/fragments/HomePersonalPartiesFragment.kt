package com.pmq.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmq.Models.Party
import com.pmq.Models.User
import com.pmq.R
import com.pmq.ui.JukeBoxActivity
import com.pmq.ui.SettingsActivity
import com.pmq.ui.SignInActivity
import com.pmq.ui.adapters.JukeboxAdapter
import java.util.*
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [HomePersonalPartiesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [HomePersonalPartiesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class HomePersonalPartiesFragment : Fragment() {
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


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_personal_parties, container, false)


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etPartyName = view.findViewById(R.id.etPartyName)

        btnHostButton = view.findViewById(R.id.btnCreateParty)
        btnHostButton.setOnClickListener(View.OnClickListener {
            attemptCreateParty()
        })

        btnLeaveButton = view.findViewById(R.id.btnLeaveParty)
        btnLeaveButton.setOnClickListener(View.OnClickListener {
            onLeavePartyClicked()
        })

        tvJoinParty = view.findViewById(R.id.tvJoinParty)
        etRoomCode = view.findViewById(R.id.etRoomCode)

        btnJoinParty = view.findViewById(R.id.btnJoinByRoomCode)
        btnJoinParty.setOnClickListener(View.OnClickListener {
            onJoinPartyClicked()
        })

        tvNearbyParties = view.findViewById(R.id.tvHistory)

        divider1 = view.findViewById(R.id.divider)
        divider2 = view.findViewById(R.id.divider2)

        getUsersCurrentParty(auth.currentUser!!.uid)

        jukeboxListView = view.findViewById(R.id.user_jukeboxes)
        userJukeboxQuery = firestore.collection("Users").document(auth.currentUser!!.uid)
                .collection("userParties")
        jukeboxAdapter = JukeboxAdapter(userJukeboxQuery, requireContext())
        jukeboxListView.layoutManager = LinearLayoutManager(requireContext())
        jukeboxListView.adapter = jukeboxAdapter
        jukeboxAdapter.startListening()

        return view
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
                        Toast.makeText(requireContext(), "No parties with that information.", Toast.LENGTH_SHORT).show()
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
                            for (i in 1 until history.size) {
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
                                            val intent = Intent(requireContext(), JukeBoxActivity::class.java)
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
            Toast.makeText(requireContext(), "Party name cannot be empty", Toast.LENGTH_SHORT).show()
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
                                        FirebaseFirestore.getInstance().runTransaction{ transaction ->
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
                                        Toast.makeText(requireContext(), "You have created a party with this name already", Toast.LENGTH_LONG).show()
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
        val intent = Intent(requireContext(), JukeBoxActivity::class.java)
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.leave_party -> run {
                val intent = Intent(requireContext(), HomePersonalPartiesFragment::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(requireContext(), SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.log_out -> run {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomePersonalPartiesFragment.
         */

        const val TAG : String = "PersonalPartiesFragment"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomePersonalPartiesFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
