package com.example.beeptalk.pages

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.beeptalk.R
import com.example.beeptalk.databinding.ActivityForgotPasswordPageBinding
import com.example.beeptalk.helper.generateOTP
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class ForgotPasswordPage : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordPageBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.sendEmailBtn.setOnClickListener {
            val email = binding.emailET.text.toString()
            if (email.isNotEmpty()) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    firebaseAuth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val signInMethods = task.result.signInMethods

                                if (signInMethods != null) {
                                    if (signInMethods.isEmpty()) {
                                        Toast.makeText(
                                            this,
                                            getText(R.string.email_not_exists),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (signInMethods.size == 1 && signInMethods.contains("google.com")) {
                                        Toast.makeText(
                                            this,
                                            getText(R.string.only_logged_with_google),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        if (signInMethods.size > 1 || !signInMethods.contains("google.com")) {
                                            val policy = ThreadPolicy.Builder()
                                                .permitAll().build()
                                            StrictMode.setThreadPolicy(policy)
                                            sendEmail(email)

                                            binding.emailET.text.clear()
                                        }
                                    }


                                } else {
                                    Toast.makeText(
                                        this,
                                        getText(R.string.email_not_exists),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    getText(R.string.error_check_email),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, getText(R.string.input_valid_email), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, getText(R.string.fields_cannot_empty), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun sendEmail(to: String) {
        val otp = generateOTP()
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("renacierr@gmail.com", "sorxkuozzpwaqoon")
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress("addisonrenaldi@gmail.com"))
            message.setRecipients(Message.RecipientType.TO, to)
            message.subject = getString(R.string.beep_talk_forgot_password)
            message.setText(getString(R.string.forgot_password_otp, otp))

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        } finally {
            goToVerifyOTPPage(to, otp)
        }
    }

    private fun goToLoginPage() {
        val intent = Intent(this, LoginPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToVerifyOTPPage(emailSentTO: String, OTP: String) {
        val intent = Intent(this, VerifyOTPForgotPasswordPage::class.java)
        intent.putExtra("emailSentTo", emailSentTO)
        intent.putExtra("OTP", OTP)
        startActivity(intent)
        finish()
    }

}