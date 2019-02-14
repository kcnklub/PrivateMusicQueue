package com.jukebox.hero.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.jukebox.hero.Models.User
import com.jukebox.hero.R
import com.jukebox.hero.services.PmqUserService
import com.jukebox.hero.util.SaveSharedPreference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_register.*
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private var disposable : Disposable? = null
    private val pmqUserService by lazy {
        PmqUserService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setSupportActionBar(toolbar)

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener {
            attemptRegister()
        }
    }

    private fun attemptRegister(){
        val usernameView: EditText = findViewById(R.id.username_register)
        val emailView: EditText = findViewById(R.id.email_register)
        val passwordView: EditText = findViewById(R.id.password_register)
        val passwordConfirmView: EditText = findViewById(R.id.password_confirm)

        usernameView.error = null
        emailView.error = null
        passwordView.error = null
        passwordConfirmView.error = null

        val username: String = usernameView.text.toString()
        val email: String = emailView.text.toString()
        val password: String = passwordView.text.toString()
        val passwordConfirm: String = passwordConfirmView.text.toString()

        var cancel = false
        var focusView: View = usernameView

        if (isEmpty(username)) {
            usernameView.error = getString(R.string.error_required_field)
            focusView = usernameView
            cancel = true
        }

        if (isEmpty(email)) {
            usernameView.error = getString(R.string.error_required_field)
            focusView = emailView
            cancel = true
        }

        if (isEmpty(password)) {
            usernameView.error = getString(R.string.error_required_field)
            focusView = passwordView
            cancel = true
        }

        if (isEmpty(passwordConfirm)) {
            usernameView.error = getString(R.string.error_required_field)
            focusView = passwordConfirmView
            cancel = true
        }

        if (password != passwordConfirm) {
            passwordConfirmView.error = getString(R.string.passwords_dont_match)
            focusView = passwordConfirmView
            cancel = true
        }

        if(cancel){
            focusView.requestFocus()
        } else {
            disposable = pmqUserService.registerUser( User.User(email, username, password))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result -> kotlin.run {
                                if(result.message == "success"){
                                    //logged in
                                    registerComplete(result.userId)
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

    private fun registerComplete(userId : Int){
        SaveSharedPreference.setLoggedIn(applicationContext, true, userId)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}
