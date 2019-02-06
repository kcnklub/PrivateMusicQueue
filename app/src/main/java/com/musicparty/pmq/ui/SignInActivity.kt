package com.musicparty.pmq.ui

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.musicparty.pmq.Models.User
import com.musicparty.pmq.R
import com.musicparty.pmq.services.PmqUserService
import com.musicparty.pmq.util.SaveSharedPreference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_sign_in.*
import java.lang.Exception

class SignInActivity : AppCompatActivity() {

    private val TAG: String = "Sign In Activty"

    private var disposable : Disposable? = null
    private val pmqUserService by lazy {
        PmqUserService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val signInButton: Button = findViewById(R.id.signin_button)
        val registerButton: Button = findViewById(R.id.register_button)

        signInButton.setOnClickListener { attemptLogin() }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        if(SaveSharedPreference.getLoggedStatus(applicationContext)){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun attemptLogin(){
        val usernameView: AutoCompleteTextView = findViewById(R.id.username_signin)
        val passwordView: EditText = findViewById(R.id.password_signin)

        usernameView.error = null
        passwordView.error = null

        val email: String = usernameView.text.toString()
        val password: String = passwordView.text.toString()

        var cancel = false
        var focusView: View = usernameView

        if (TextUtils.isEmpty(password)) {
            passwordView.error = "@Strings/error_invalid_password"
            focusView = passwordView
            cancel = true
        }

        if (TextUtils.isEmpty(email)) {
            usernameView.error = "@Strings/error_required_field"
            focusView = usernameView
            cancel = true
        }

        if (cancel) {
            focusView.requestFocus()
        } else {
            Log.d(TAG, "LOGGING IN")
            // Log the user in here.
            disposable = pmqUserService.signIn(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result -> run {
                                if(result.message == "success"){
                                    //logged in
                                    loginComplete(result.userId)
                                } else {
                                    throw Exception(result.message)
                                }
                            }},
                            { error ->
                                run {
                                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
        }
    }

    private fun loginComplete(userId : Int){
        SaveSharedPreference.setLoggedIn(applicationContext, true, userId)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }


}