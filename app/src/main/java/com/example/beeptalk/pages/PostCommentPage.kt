package com.example.beeptalk.pages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beeptalk.R
import com.example.beeptalk.databinding.ActivityPostCommentPageBinding
import com.example.beeptalk.lib.PostCommentRVAdapter
import com.example.beeptalk.models.PostComment
import com.example.beeptalk.models.PostCommentReply
import com.example.beeptalk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class PostCommentPage : AppCompatActivity() {

    private lateinit var binding: ActivityPostCommentPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var postCommentRVAdapter: PostCommentRVAdapter
    private lateinit var postComments: ArrayList<PostComment>

    private lateinit var postId: String
    private lateinit var commentReplyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCommentPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        postComments = arrayListOf()
        postId = intent.getStringExtra("postId").toString()

        firebaseAuth.currentUser?.let {
            firebaseFirestore.collection("users").document(it.uid).addSnapshotListener { value, _ ->
                if (value != null) {
                    val user = value.toObject(User::class.java)
                    if (user != null) {
                        Picasso.get()
                            .load(user.profilePicture)
                            .into(binding.profilePicture)

                        binding.currentUserUsername.text = user.username
                    };
                }
            }
        }

        postCommentRVAdapter = PostCommentRVAdapter(this, binding, postComments)
        binding.commentsRV.layoutManager = LinearLayoutManager(this)
        binding.commentsRV.setHasFixedSize(true)
        binding.commentsRV.adapter = postCommentRVAdapter

        binding.replyingToLayout.visibility = View.GONE

        binding.cancelReplyBtn.setOnClickListener {
            binding.replyingToLayout.visibility = View.GONE
            binding.usernameReplyToTV.text = ""
            binding.commentAsLayout.visibility = View.VISIBLE
        }

        binding.commentBtn.setOnClickListener {
            val usernameReply = binding.usernameReplyToTV.text.toString()
            val body = binding.commentET.text.toString()
            if (usernameReply.isEmpty()) {
                if (body.isNotEmpty()) {
                    val threadComment = PostComment(
                        postId = postId,
                        body = body,
                        userId = firebaseAuth.currentUser?.uid
                    )

                    firebaseFirestore.collection("posts").document(postId)
                        .collection("comments")
                        .add(threadComment).addOnSuccessListener {
                            binding.commentET.text.clear()

                            Toast.makeText(
                                this,
                                getText(R.string.comment_added),
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                getText(R.string.comment_add_failed),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                } else {
                    Toast.makeText(this, getText(R.string.fields_cannot_empty), Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                if (body.isNotEmpty()) {
                    val threadComment = PostCommentReply(
                        commentId = commentReplyId,
                        body = binding.commentET.text.toString(),
                        userId = firebaseAuth.currentUser?.uid
                    )

                    firebaseFirestore.collection("posts").document(postId)
                        .collection("comments").document(commentReplyId).collection("reply")
                        .add(threadComment).addOnSuccessListener {
                            binding.commentET.text.clear()

                            Toast.makeText(this, getText(R.string.reply_added), Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(this, getText(R.string.reply_add_failed), Toast.LENGTH_SHORT)
                                .show()
                        }
                } else {
                    Toast.makeText(this, getText(R.string.fields_cannot_empty), Toast.LENGTH_SHORT).show()
                }
            }


        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter("reply-credentials")
        );

        getAllComments(postId)
        postComments.sortedWith(compareBy { it.createdAt })
        postCommentRVAdapter.notifyDataSetChanged()
    }

    private fun getAllComments(postId: String) {
        firebaseFirestore.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                querySnapshot?.let {
                    postComments.clear()
                    for (document in querySnapshot.documents) {
                        val curr = document.toObject(PostComment::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> postComments.add(it1) }
                    }
                    postCommentRVAdapter.notifyDataSetChanged()
                }
            }
    }

    private var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            commentReplyId = intent.getStringExtra("commentId").toString()
        }
    }

}