package com.example.beeptalk.pages

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beeptalk.R
import com.example.beeptalk.databinding.ActivityCommentDetailPageBinding
import com.example.beeptalk.lib.RecyclerViewInterface
import com.example.beeptalk.lib.ThreadCommentReplyRVadapter
import com.example.beeptalk.models.Notification
import com.example.beeptalk.models.ThreadCommentReply
import com.example.beeptalk.parcel.ThreadCommentID
import com.example.beeptalk.parcel.ThreadID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CommentDetailPage : AppCompatActivity(), RecyclerViewInterface {

    private lateinit var binding: ActivityCommentDetailPageBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var comments: ArrayList<ThreadCommentReply>
    private lateinit var threadCommentReplyRVAdapter: ThreadCommentReplyRVadapter

    private lateinit var currThread: ThreadID
    private lateinit var currComm: ThreadCommentID

    private var uid: String = "default"
    private var uname: String = "default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currThread = intent.getParcelableExtra("thread")!!
        currComm = intent.getParcelableExtra("comment")!!

        binding.apply {
            commentBodyTv.text = currComm.body
        }

        db = FirebaseFirestore.getInstance()

        FirebaseAuth.getInstance().currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener {
                    val data = it.data ?: return@addOnSuccessListener
                    Picasso.get().load(data["profilePicture"] as String)
                        .into(binding.avCurrUser)
                }
        }

        db.collection("users").document(currComm.uid!!).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                binding.unameTv.text = data["username"] as String
                Picasso.get().load(data["profilePicture"] as String)
                    .into(binding.avUser)
            }

        db.collection("threads").document(currComm.threadId!!)
            .collection("comments").document(currComm.id!!).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                binding.commentBodyTv.text = data["body"] as String
                val up = data["upvote"] as List<*>
                val down = data["downvote"] as List<*>
            }

        binding.btnPostComment.setOnClickListener {
            val body = binding.commentBodyEt.text.toString()
            val threadId = currThread.id
            val commId = currComm.id

            val threadCommentReply = ThreadCommentReply(
                threadId = threadId,
                commentId = commId,
                body = body,
                uid = uid,
                replyTo = currComm.uid
            )

            db.collection("threads").document(threadId)
                .collection("comments").document(commId)
                .collection("comments")
                .add(threadCommentReply).addOnSuccessListener {
                    binding.commentBodyEt.text.clear()

                    Toast.makeText(this, getText(R.string.comment_posted), Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, getText(R.string.comment_post_failed), Toast.LENGTH_SHORT).show()
                }

            if (currComm.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                val notification = Notification(
                    currComm.uid,
                    FirebaseAuth.getInstance().currentUser?.uid, "replyComment-$body"
                )

                db.collection("users").document(currComm.uid).collection("notifications")
                    .add(notification)
            }
        }

        binding.rvThreadCommentReply.layoutManager = LinearLayoutManager(this)
        binding.rvThreadCommentReply.setHasFixedSize(true)

        comments = arrayListOf()

        threadCommentReplyRVAdapter = ThreadCommentReplyRVadapter(comments, this)

        binding.rvThreadCommentReply.adapter = threadCommentReplyRVAdapter

        subscribeThreadCommentReplies(currThread.id, currComm.id)
    }

    private fun subscribeThreadCommentReplies(threadId: String, commentId: String) {
        db.collection("threads").document(threadId)
            .collection("comments").document(commentId)
            .collection("comments")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                querySnapshot?.let {
                    comments = arrayListOf()
                    for (document in querySnapshot.documents) {
                        var curr = document.toObject(ThreadCommentReply::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> comments.add(it1) }
                    }
                    threadCommentReplyRVAdapter.setComments(comments)

                    threadCommentReplyRVAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun getThreadComments(threadId: String, commentId: String) {
        db.collection("threads").document(threadId).collection("comments").document(commentId)
            .collection("comments")
            .get().addOnSuccessListener {
                for (document in it.documents) {
                    var curr = document.toObject(ThreadCommentReply::class.java)
                    curr?.id = document.id.toString()
                    curr?.let { it1 -> comments.add(it1) }
                }
                threadCommentReplyRVAdapter.setComments(comments)

                threadCommentReplyRVAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(position: Int) {
    }
}