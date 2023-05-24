package com.example.healthtalk.pages

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordPage : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.changePasswordBtn.setOnClickListener {
            val password = binding.passwordET.text.toString()
            val confPassword = binding.confirmPasswordET.text.toString()

            if (password.isNotEmpty() && confPassword.isNotEmpty()) {
                if (password == confPassword) {
                    firebaseAuth.currentUser?.let { it1 ->
                        firebaseFirestore.collection("users").document(it1.uid)
                            .get().addOnCompleteListener { it ->
                                if (it.isSuccessful) {
                                    val document = it.result
                                    if (password != document["password"] as String) {
                                        firebaseAuth.currentUser?.updatePassword(password)
                                            ?.addOnSuccessListener {
                                                Toast.makeText(
                                                    this,
                                                    getText(R.string.password_update_success),
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                firebaseAuth.currentUser?.uid?.let { it2 ->
                                                    firebaseFirestore.collection("users").document(
                                                        it2
                                                    ).update("password", password)
                                                        .addOnSuccessListener {
                                                        }

                                                }
                                                finish()
                                            }?.addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    getText(R.string.password_update_failed),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }


                                    } else {
                                        Toast.makeText(
                                            this,
                                            getText(R.string.password_cannot_same),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}