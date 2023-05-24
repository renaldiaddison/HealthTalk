package com.example.healthtalk.pages

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityVerifyOtpforgotPasswordPageBinding

class VerifyOTPForgotPasswordPage : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyOtpforgotPasswordPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpforgotPasswordPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        val emailSentTo = intent.getStringExtra("emailSentTo")
        val OTP = intent.getStringExtra("OTP")

        binding.emailSentToTV.text = emailSentTo

        setupOTPInputs()

        binding.verifyOTPBtn.setOnClickListener {
            if (binding.inputCode1.text.toString().trim().isEmpty() ||
                binding.inputCode2.text.toString().trim().isEmpty() ||
                binding.inputCode3.text.toString().trim().isEmpty() ||
                binding.inputCode4.text.toString().trim().isEmpty() ||
                binding.inputCode5.text.toString().trim().isEmpty() ||
                binding.inputCode6.text.toString().trim().isEmpty()
            ) {
                Toast.makeText(this, getText(R.string.code_cannot_empty), Toast.LENGTH_SHORT).show()
            } else {
                val code: String =
                    binding.inputCode1.text.toString() + binding.inputCode2.text.toString() + binding.inputCode3.text.toString() + binding.inputCode4.text.toString() + binding.inputCode5.text.toString() + binding.inputCode6.text.toString()
                if (code == OTP) {
                    if (emailSentTo != null) {
                        goToUpdatePasswordPage(emailSentTo)
                    }
                } else {
                    Toast.makeText(this, getText(R.string.enter_a_valid_code), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupOTPInputs() {
        binding.inputCode1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotEmpty()) {
                    binding.inputCode2.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.inputCode2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotEmpty()) {
                    binding.inputCode3.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.inputCode3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotEmpty()) {
                    binding.inputCode4.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.inputCode4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotEmpty()) {
                    binding.inputCode5.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        binding.inputCode5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isNotEmpty()) {
                    binding.inputCode6.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

    }

    private fun goToUpdatePasswordPage(email: String) {
        val intent = Intent(this, UpdatePasswordPage::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }
}