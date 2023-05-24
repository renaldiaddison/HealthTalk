package com.example.healthtalk.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityEditCommentPageBinding
import com.example.healthtalk.helper.getRelativeString
import com.example.healthtalk.models.PostComment
import com.example.healthtalk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditCommentPage : AppCompatActivity() {

    private lateinit var binding: ActivityEditCommentPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCommentPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val postId = intent.getStringExtra("postId")
        val commentId = intent.getStringExtra("commentId")

        if (postId != null) {
            if (commentId != null) {

                binding.edit.setOnClickListener {

                    val body = binding.commentBodyTV.text.toString()

                    val updates = HashMap<String, Any>()
                    updates["body"] = body

                    if (body.isNotEmpty()) {
                        firebaseFirestore.collection("posts").document(postId)
                            .collection("comments")
                            .document(commentId).update(updates).addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    getText(R.string.comment_updated),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    getText(R.string.comment_update_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            getText(R.string.fields_cannot_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }

                firebaseFirestore.collection("posts").document(postId).collection("comments")
                    .document(commentId).addSnapshotListener { querySnapshot, _ ->
                        if (querySnapshot != null) {
                            val curr = querySnapshot.toObject(PostComment::class.java)
                            curr?.id = querySnapshot.id

                            if (curr != null) {
                                binding.createdDateTV.text = getRelativeString(curr.createdAt)
                                binding.commentBodyTV.setText(curr.body)

                                curr.userId?.let {
                                    firebaseFirestore.collection("users").document(it)
                                }
                                    ?.addSnapshotListener { value, _ ->
                                        if (value != null) {
                                            val user = value.toObject(User::class.java)

                                            if (user != null) {
                                                binding.usernameTV.text = user.username
                                                Picasso.get()
                                                    .load(user.profilePicture)
                                                    .into(binding.profilePicture)

                                                binding.profilePicture.setOnClickListener {
                                                    goToProfilePage(curr.userId)
                                                }

                                                binding.usernameTV.setOnClickListener {
                                                    goToProfilePage(curr.userId)
                                                }
                                            }

                                        }

                                    }
                            }
                        }
                    }
            }
        }

    }


    private fun goToProfilePage(userId: String) {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }
}