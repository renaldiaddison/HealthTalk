package com.example.healthtalk.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityThreadDetailPageBinding
import com.example.healthtalk.helper.getRelativeString
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.lib.ThreadCommentRVAdapter
import com.example.healthtalk.models.Notification
import com.example.healthtalk.models.ThreadComment
import com.example.healthtalk.parcel.ThreadCommentID
import com.example.healthtalk.parcel.ThreadID
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ThreadDetailPage : AppCompatActivity(), RecyclerViewInterface {

    private lateinit var binding: ActivityThreadDetailPageBinding
    private lateinit var comments: ArrayList<ThreadComment>
    private lateinit var threadCommentRVAdapter: ThreadCommentRVAdapter
    private lateinit var db: FirebaseFirestore

    private lateinit var thread: ThreadID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        thread = intent.getParcelableExtra("thread")!!

        FirebaseAuth.getInstance().currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener {
                    val data = it.data ?: return@addOnSuccessListener
                    Picasso.get().load(data["profilePicture"] as String)
                        .into(binding.avCurrUser)
                }
        }

        db.collection("users").document(thread.uid!!).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                binding.tvUsername.text = data["username"] as String
                Picasso.get().load(data["profilePicture"] as String)
                    .into(binding.avUser)
            }

        db.collection("threads").document(thread.id).get()
            .addOnSuccessListener {
                val data = it.data
                val createdDate = (data!!["createdAt"] as Timestamp).toDate()
                val dateString = getRelativeString(createdDate)
                binding.tvCreatedAt.text = dateString
                binding.tvThreadBody.text = data["body"] as String
                val up = data["upvote"] as List<*>
                val down = data["downvote"] as List<*>
            }

        binding.btnPostComment.setOnClickListener {
            val body = binding.etCommentBody.text.toString()
            val threadId = thread.id

            val threadComment = ThreadComment(
                threadId = threadId,
                body = body,
                uid = FirebaseAuth.getInstance().currentUser?.uid,
                replyTo = thread.uid
            )

            db.collection("threads").document(thread.id)
                .collection("comments")
                .add(threadComment).addOnSuccessListener {
                    binding.etCommentBody.text.clear()

                    Toast.makeText(this, getText(R.string.comment_posted), Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                    Toast.makeText(this, getText(R.string.comment_add_failed), Toast.LENGTH_SHORT)
                        .show()
                }

            if (thread.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                val notification = Notification(
                    thread.uid,
                    FirebaseAuth.getInstance().currentUser?.uid,
                    "commentThread-$body"
                )
                db.collection("users").document(thread.uid)
                    .collection("notifications").add(notification)
            }
        }

        binding.rvThreadComment.layoutManager = LinearLayoutManager(this)
        binding.rvThreadComment.setHasFixedSize(true)

        comments = arrayListOf()

        threadCommentRVAdapter = ThreadCommentRVAdapter(comments, this)

        binding.rvThreadComment.adapter = threadCommentRVAdapter

        subscribeThreadComments(thread.id)
//        getThreadComments(thread.id)
    }

    private fun subscribeThreadComments(threadId: String) {
        db.collection("threads").document(threadId).collection("comments")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, getText(R.string.error_occured), Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    comments = arrayListOf()
                    for (document in querySnapshot.documents) {
                        var curr = document.toObject(ThreadComment::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> comments.add(it1) }
                    }
                    threadCommentRVAdapter.setComments(comments)

                    threadCommentRVAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun getThreadComments(threadId: String) {
        db.collection("threads").document(threadId).collection("comments")
            .get().addOnSuccessListener {
                for (document in it.documents) {
                    var curr = document.toObject(ThreadComment::class.java)
                    curr?.id = document.id.toString()
                    curr?.let { it1 -> comments.add(it1) }
                }
                threadCommentRVAdapter.setComments(comments)

                threadCommentRVAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(position: Int) {
        val curr = comments[position]
        val id = curr.id
        val threadId = thread.id
        val commUid = curr.uid
        val body = curr.body
        val upvote = curr.upvote
        val downvote = curr.downvote
        val replyTo = curr.replyTo

        val commentItem: ThreadCommentID =
            ThreadCommentID(id!!, threadId, commUid!!, body!!, replyTo, upvote, downvote)

        intent = Intent(this, CommentDetailPage::class.java)
        intent.putExtra("thread", thread)
        intent.putExtra("comment", commentItem)
        startActivity(intent)
    }
}