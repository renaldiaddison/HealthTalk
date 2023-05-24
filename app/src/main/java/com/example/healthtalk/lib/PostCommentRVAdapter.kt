package com.example.healthtalk.lib

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityPostCommentPageBinding
import com.example.healthtalk.databinding.PostCommentCardBinding
import com.example.healthtalk.helper.getRelativeString
import com.example.healthtalk.models.PostComment
import com.example.healthtalk.models.PostCommentReply
import com.example.healthtalk.models.User
import com.example.healthtalk.pages.EditCommentPage
import com.example.healthtalk.pages.ProfilePage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso


class PostCommentRVAdapter(
    private var context: Context,
    private var parentBinding: ActivityPostCommentPageBinding,
    private var postComments: ArrayList<PostComment>,
) : RecyclerView.Adapter<PostCommentRVAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: PostCommentCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            PostCommentCardBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postComment = postComments[position]
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        holder.binding.viewAllRepliesBtn.visibility = View.GONE
        holder.binding.likeCommentBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        holder.binding.dislikeCommentBtn.setImageResource(R.drawable.ic_outline_thumb_down_24)
        holder.binding.edit.visibility = View.GONE
        holder.binding.delete.visibility = View.GONE

        val postCommentReplies: ArrayList<PostCommentReply> = arrayListOf()

        val postCommentReplyRVAdapter = postComment.postId?.let { it1 ->
            PostCommentReplyRVAdapter(
                context, holder.binding,
                it1, postCommentReplies
            )
        }
        holder.binding.repliesRV.layoutManager = LinearLayoutManager(context)
        holder.binding.repliesRV.setHasFixedSize(true)
        holder.binding.repliesRV.adapter = postCommentReplyRVAdapter

        postComment.postId?.let { it1 ->
            postComment.id?.let { it2 ->
                if (postCommentReplyRVAdapter != null) {
                    getAllReplies(
                        it1,
                        it2, postCommentReplies, postCommentReplyRVAdapter, holder.binding
                    )
                }
            }
        }

        if (postComment.userId == firebaseAuth.currentUser?.uid) {
            holder.binding.edit.visibility = View.VISIBLE
            holder.binding.delete.visibility = View.VISIBLE

            holder.binding.edit.setOnClickListener {
                postComment.id?.let { it1 -> postComment.postId?.let { it2 ->
                    goToEditCommentPage(
                        it2, it1)
                } }
            }

            holder.binding.delete.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(context.getString(R.string.delete_comment))
                builder.setMessage(context.getString(R.string.are_your_sure_delete_comment))

                builder.setPositiveButton(context.getText(R.string.yes)) { _, _ ->
                    postComment.postId?.let { it1 ->
                        postComment.id?.let { it2 ->
                            firebaseFirestore.collection("posts").document(
                                it1
                            ).collection("comments").document(it2).delete().addOnSuccessListener {
                                Toast.makeText(context, context.getText(R.string.comment_deleted), Toast.LENGTH_SHORT)
                                    .show()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    context.getText(R.string.comment_failed_deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }
                builder.setNegativeButton(context.getText(R.string.no)) { _, _ ->

                }
                val dialog = builder.create()
                dialog.show()
            }

        }

        holder.binding.repliesRV.visibility = View.GONE
        holder.binding.createdDateTV.text = getRelativeString(postComment.createdAt)
        holder.binding.commentBodyTV.text = postComment.body

        postComment.userId?.let { firebaseFirestore.collection("users").document(it) }
            ?.addSnapshotListener { value, _ ->
                if (value != null) {
                    val user = value.toObject(User::class.java)

                    if (user != null) {
                        holder.binding.usernameTV.text = user.username
                        Picasso.get()
                            .load(user.profilePicture)
                            .into(holder.binding.profilePicture);

                        holder.binding.reply.setOnClickListener {
                            parentBinding.usernameReplyToTV.text = user.username
                            parentBinding.replyingToLayout.visibility = View.VISIBLE
                            parentBinding.commentAsLayout.visibility = View.GONE

                            val intent = Intent("reply-credentials")
                            intent.putExtra("userId", user.uid)
                            intent.putExtra("commentId", postComment.id)
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                        }

                        holder.binding.profilePicture.setOnClickListener {
                            goToProfilePage(postComment.userId)
                        }

                        holder.binding.usernameTV.setOnClickListener {
                            goToProfilePage(postComment.userId)
                        }
                    }

                }

            }
        if (postComment.likes.contains(firebaseAuth.currentUser?.uid)) {
            holder.binding.likeCommentBtn.setImageResource(R.drawable.ic_baseline_like_filled_24)
        }

        if (postComment.dislikes.contains(firebaseAuth.currentUser?.uid)) {
            holder.binding.dislikeCommentBtn.setImageResource(R.drawable.ic_baseline_thumb_down_24)
        }

        holder.binding.likeCommentBtn.setOnClickListener {
            postComment.postId?.let { it1 ->
                postComment.id?.let { it2 ->
                    if (postCommentReplyRVAdapter != null) {
                        getAllReplies(
                            it1,
                            it2, postCommentReplies, postCommentReplyRVAdapter, holder.binding
                        )
                    }
                }
            }
            if (!postComment.likes.contains(firebaseAuth.currentUser?.uid)) {
                postComment.postId?.let { it1 ->
                    postComment.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(it1).collection("comments")
                            .document(
                                it2
                            ).update("likes", FieldValue.arrayUnion(firebaseAuth.currentUser?.uid))
                            .addOnSuccessListener {
                                holder.binding.likeCommentBtn.setImageResource(R.drawable.ic_baseline_like_filled_24)
                                if (postComment.dislikes.contains(firebaseAuth.currentUser?.uid)) {
                                    postComment.postId.let { it1 ->
                                        postComment.id?.let { it2 ->
                                            firebaseFirestore.collection("posts").document(it1)
                                                .collection("comments").document(
                                                    it2
                                                ).update(
                                                    "dislikes",
                                                    FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                                                ).addOnSuccessListener {
                                                    holder.binding.dislikeCommentBtn.setImageResource(
                                                        R.drawable.ic_outline_thumb_down_24
                                                    )
                                                }
                                        }
                                    }
                                }
                            }
                    }
                }

            } else {
                holder.binding.likeCommentBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                postComment.postId?.let { it1 ->
                    postComment.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(it1).collection("comments")
                            .document(
                                it2
                            ).update("likes", FieldValue.arrayRemove(firebaseAuth.currentUser?.uid))
                    }
                }
            }
        }

        holder.binding.dislikeCommentBtn.setOnClickListener {
            holder.binding.viewAllRepliesBtn.visibility = View.VISIBLE
            if (!postComment.dislikes.contains(firebaseAuth.currentUser?.uid)) {

                postComment.postId?.let { it1 ->
                    postComment.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(it1).collection("comments")
                            .document(
                                it2
                            ).update(
                                "dislikes",
                                FieldValue.arrayUnion(firebaseAuth.currentUser?.uid)
                            ).addOnSuccessListener {
                                holder.binding.dislikeCommentBtn.setImageResource(R.drawable.ic_baseline_thumb_down_24)

                                if (postComment.likes.contains(firebaseAuth.currentUser?.uid)) {
                                    postComment.postId.let { it1 ->
                                        postComment.id?.let { it2 ->
                                            firebaseFirestore.collection("posts").document(it1)
                                                .collection("comments").document(
                                                    it2
                                                ).update(
                                                    "likes",
                                                    FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                                                ).addOnSuccessListener {
                                                    holder.binding.likeCommentBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                                                }
                                        }
                                    }
                                }
                            }
                    }
                }


            } else {
                holder.binding.dislikeCommentBtn.setImageResource(R.drawable.ic_outline_thumb_down_24)
                postComment.postId?.let { it1 ->
                    postComment.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(it1).collection("comments")
                            .document(
                                it2
                            ).update(
                                "dislikes",
                                FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                            )
                    }
                }
            }
        }

        holder.binding.viewAllRepliesBtn.setOnClickListener {
            holder.binding.repliesRV.visibility = View.VISIBLE
            holder.binding.viewAllRepliesBtn.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return postComments.size
    }

    private fun goToProfilePage(userId: String) {
        val intent = Intent(context, ProfilePage::class.java)
        intent.putExtra("userId", userId)
        context.startActivity(intent)
    }

    private fun goToEditCommentPage(postId: String, commentId: String) {
        val intent = Intent(context, EditCommentPage::class.java)
        intent.putExtra("commentId", commentId)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

    private fun getAllReplies(
        postId: String,
        commentId: String,
        postCommentReplies: ArrayList<PostCommentReply>,
        postCommentReplyRVAdapter: PostCommentReplyRVAdapter,
        binding: PostCommentCardBinding
    ) {
        FirebaseFirestore.getInstance().collection("posts").document(postId).collection("comments")
            .document(commentId).collection("reply")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                querySnapshot?.let {
                    if (querySnapshot.documents.isEmpty()) {
                        binding.viewAllRepliesBtn.visibility = View.GONE
                    } else {
                        binding.viewAllRepliesBtn.visibility = View.VISIBLE
                    }
                }

                querySnapshot?.let {
                    postCommentReplies.clear()
                    for (document in querySnapshot.documents) {
                        val curr = document.toObject(PostCommentReply::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> postCommentReplies.add(it1) }
                    }
                    postCommentReplyRVAdapter.notifyDataSetChanged()
                }
            }
    }

}