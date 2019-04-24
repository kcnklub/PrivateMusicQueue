package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query.Direction
import com.jukebox.hero.R
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.*

class SignInActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        googleSignInButton.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult>{
                    override fun onSuccess(result: LoginResult?) {
                        Log.d(TAG, "facebook:onsuccess:$result")
                        handleFacebookAccessToken(result?.accessToken!!)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "facebook:oncancel")
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d(TAG, "facebook:onerror")
                    }
                })

        firestore = FirebaseFirestore.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // check if user is signed in and update UI accordingly.
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
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken){
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
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

            val userDoc = firestore.collection("Users").document(user.uid)
            userDoc.get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            Log.d(TAG, "User document already existed")
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        else {
                            firestore.collection("Users").orderBy("UserCode", Direction.DESCENDING).limit(1)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        // get a new unique userCode
                                        var userCode = if (querySnapshot != null) {
                                            querySnapshot.documents.first().data!!["UserCode"].toString()
                                        } else {
                                            "0000"
                                        }
                                        userCode = ((userCode.toInt()) + 1).toString().padStart(4, '0')

                                        val u = HashMap<String, Any?>()
                                        u["DisplayName"] = user.displayName.toString()
                                        u["UserId"] = user.uid
                                        u["UserCode"] = userCode
                                        u["CurrentParty"] = null
                                        u["History"] = mutableListOf<String>()
                                        u["HostedParties"] = mutableListOf<String>()

                                        userDoc.set(u)
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



            // move to the main activity
            //val intent = Intent(this, JukeBoxActivity::class.java)



        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.googleSignInButton -> signIn()
        }
    }

    companion object {
        private const val TAG = "Sign In Activity"
        private const val GOOGLE_SIGN_IN = 1337
    }
}