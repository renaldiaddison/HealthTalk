package com.example.healthtalk.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityEditProfilePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditProfilePage : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    private var currentUserName = "";
    private var btnDisabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val user = firebaseAuth.currentUser

        user?.uid?.let { it ->
            firebaseFirestore.collection("users").document(it).addSnapshotListener { value, error ->
                val data = value?.data
                if (data != null) {
                    binding.nameET.setText(data["name"] as String)
                    binding.usernameET.setText(data["username"] as String)
                    Picasso.get().load(data["profilePicture"] as String)
                        .into(binding.profilePicture)
                    binding.bioET.setText(data["bio"] as String)

                    currentUserName = data["username"] as String

                    btnDisabled = false
                }
            }
        }

        binding.profilePicture.setOnClickListener {
            pickImg()
        }

        binding.saveBtn.setOnClickListener {
            if (!btnDisabled) {
                val name = binding.nameET.text.toString()
                val username = binding.usernameET.text.toString()
                val bio = binding.bioET.text.toString()

                val updates = HashMap<String, Any>()
                updates["name"] = name
                updates["username"] = username
                updates["bio"] = bio

                if (name.isNotEmpty() && username.isNotEmpty() && bio.isNotEmpty()) {
                    firebaseFirestore.collection("users")
                        .whereEqualTo("username", username).get().addOnSuccessListener { res ->
                            if (res.isEmpty || username == currentUserName) {
                                user?.uid?.let { it1 ->
                                    firebaseFirestore.collection("users").document(
                                        it1
                                    ).update(updates).addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            getText(R.string.profile_updated),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                                }

                            } else {
                                Toast.makeText(
                                    this,
                                    getText(R.string.username_taken),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }


                } else {
                    Toast.makeText(this, getText(R.string.fields_cannot_empty), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }


    }

    private fun pickImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        pickImgFromGallery.launch(intent)
    }

    private var pickImgFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                firebaseAuth.currentUser?.let {
                    uploadImage(it.uid, result.data!!.data!!) { imageUrl ->
                        firebaseFirestore.collection("users").document(it.uid)
                            .update("profilePicture", imageUrl).addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    getText(R.string.profile_picture_updated),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                    }
                }

            }
        }

    private fun uploadImage(
        userId: String,
        filePath: Uri,
        callback: (imageUrl: String) -> Unit
    ) {
        val storageRef = firebaseStorage.getReference("$userId/profilePicture/")
        storageRef.putFile(filePath).addOnSuccessListener { task ->
            task.storage.downloadUrl.addOnSuccessListener { imageUrl ->
                callback.invoke(imageUrl.toString())
            }
        }.addOnFailureListener {
            return@addOnFailureListener
        }
    }
}