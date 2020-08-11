package com.example.kotlinmessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinmessenger.messages.LatestMessagesActivity
import com.example.kotlinmessenger.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Main", "Sign in Success with $email")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to log in : ${it.message}")
                }
        }

        back_to_register_text_view.setOnClickListener {
            finish()
        }

    }
}