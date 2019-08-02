package com.pmq.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import com.pmq.Models.User
import com.pmq.R
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        googleSignInButton.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException){
                Log.w(TAG, "Google Sign in failed", e)
            }
        }
    }

    private fun signIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct : GoogleSignInAccount){
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d(TAG, "signInWithCreditials:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_LONG).show()
                        updateUI(null)
                    }
                }
    }

    private fun updateUI(user: FirebaseUser?){
        if(user != null){

            val userDoc = fireStore.collection("Users").document(user.uid)
            userDoc.get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            Log.d(TAG, "User document already existed")
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        else {
                            fireStore.collection("Users").orderBy("UserCode", Direction.DESCENDING).limit(1)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        // get a new unique userCode
                                        var userCode = if (!querySnapshot.isEmpty) {
                                            querySnapshot.documents.first().data!!["UserCode"].toString()
                                        } else {
                                            "0000"
                                        }
                                        userCode = ((userCode.toInt()) + 1).toString().padStart(4, '0')

                                        val updateNewUser = User(user.displayName.toString(), user.uid, userCode, null)
                                        userDoc.set(updateNewUser)
                                                .addOnSuccessListener {
                                                    val intent = Intent(this, HomeActivity::class.java)
                                                    startActivity(intent)
                                                    Log.d(TAG, "New user created on sign in")
                                                }
                                                .addOnFailureListener { error ->
                                                    Log.e(TAG, "Error creating user document on sign in", error)
                                                }
                                    }
                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "!!! Error getting user document on sign in", it)
                    }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.googleSignInButton -> signIn()
        }
    }

    companion object {
        private const val TAG = "Sign In Activity"
        private const val GOOGLE_SIGN_IN = 1337
    }
}