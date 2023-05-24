package com.example.healthtalk.lib

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtalk.R
import com.example.healthtalk.databinding.PostCommentCardBinding
import com.example.healthtalk.databinding.PostCommentReplyCardBinding
import com.example.healthtalk.helper.getRelativeString
import com.example.healthtalk.models.PostCommentReply
import com.example.healthtalk.models.User
import com.example.healthtalk.pages.ProfilePage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PostCommentReplyRVAdapter(
    private var context: Context,
    private var parentBinding: PostCommentCardBinding,
    private var postId: String,
    private var postCommentReplies: ArrayList<PostCommentReply>,
) : RecyclerView.Adapter<PostCommentReplyRVAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: PostCommentReplyCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            PostCommentReplyCardBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return postCommentReplies.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postCommentReply = postCommentReplies[position]
        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        parentBinding.viewAllRepliesBtn.visibility = View.GONE
        holder.binding.likeReplyBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        holder.binding.dislikeReplyBtn.setImageResource(R.drawable.ic_outline_thumb_down_24)
        holder.binding.createdDateTV.text = getRelativeString(postCommentReply.createdAt)
        holder.binding.replyBodyTV.text = postCommentReply.body

        postCommentReply.userId?.let { firebaseFirestore.collection("users").document(it) }
            ?.addSnapshotListener { value, _ ->
                if (value != null) {
                    val user = value.toObject(User::class.java)

                    if (user != null) {
                        holder.binding.usernameTV.text = user.username
                        Picasso.get()
                            .load(user.profilePicture)
                            .into(holder.binding.profilePicture);

                        holder.binding.profilePicture.setOnClickListener {
                            goToProfilePage(postCommentReply.userId)
                        }

                        holder.binding.usernameTV.setOnClickListener {
                            goToProfilePage(postCommentReply.userId)
                        }
                    }

                }

            }

        if (postCommentReply.likes.contains(firebaseAuth.currentUser?.uid)) {
            holder.binding.likeReplyBtn.setImageResource(R.drawable.ic_baseline_like_filled_24)
        }

        if (postCommentReply.dislikes.contains(firebaseAuth.currentUser?.uid)) {
            holder.binding.dislikeReplyBtn.setImageResource(R.drawable.ic_baseline_thumb_down_24)
        }

        holder.binding.likeReplyBtn.setOnClickListener {
            if (!postCommentReply.likes.contains(firebaseAuth.currentUser?.uid)) {
                postCommentReply.commentId?.let { it2 ->
                    postCommentReply.id?.let { it1 ->
                        firebaseFirestore.collection("posts").document(postId)
                            .collection("comments")
                            .document(
                                it2
                            ).collection(
                                "reply"
                            ).document(it1)
                            .update("likes", FieldValue.arrayUnion(firebaseAuth.currentUser?.uid))
                            .addOnSuccessListener {
                                holder.binding.likeReplyBtn.setImageResource(R.drawable.ic_baseline_like_filled_24)
                                if (postCommentReply.dislikes.contains(firebaseAuth.currentUser?.uid)) {
                                        firebaseFirestore.collection("posts").document(postId)
                                            .collection("comments").document(
                                                postCommentReply.commentId
                                            ).collection("reply").document(postCommentReply.id!!)
                                            .update(
                                                "dislikes",
                                                FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                                            ).addOnSuccessListener {
                                                holder.binding.dislikeReplyBtn.setImageResource(
                                                    R.drawable.ic_outline_thumb_down_24
                                                )
                                            }
                                }
                            }
                    }
                }

            } else {
                holder.binding.likeReplyBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                postCommentReply.commentId?.let { it1 ->
                    postCommentReply.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(postId)
                            .collection("comments")
                            .document(
                                it1
                            ).collection("reply").document(it2).update(
                                "likes",
                                FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                            )
                    }
                }
            }
        }

        holder.binding.dislikeReplyBtn.setOnClickListener {
            if (!postCommentReply.dislikes.contains(firebaseAuth.currentUser?.uid)) {
                postCommentReply.commentId?.let { it1 ->
                    postCommentReply.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(postId)
                            .collection("comments")
                            .document(
                                it1
                            ).collection("reply").document(it2).update(
                                "dislikes",
                                FieldValue.arrayUnion(firebaseAuth.currentUser?.uid)
                            ).addOnSuccessListener {
                                holder.binding.dislikeReplyBtn.setImageResource(R.drawable.ic_baseline_thumb_down_24)

                                if (postCommentReply.likes.contains(firebaseAuth.currentUser?.uid)) {
                                    postCommentReply.commentId.let { it1 ->
                                        postCommentReply.id?.let { it2 ->
                                            firebaseFirestore.collection("posts").document(postId)
                                                .collection("comments").document(
                                                    it1
                                                ).collection("reply").document(it2).update(
                                                    "likes",
                                                    FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                                                ).addOnSuccessListener {
                                                    holder.binding.likeReplyBtn.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                                                }
                                        }
                                    }
                                }
                            }
                    }
                }


            } else {
                holder.binding.dislikeReplyBtn.setImageResource(R.drawable.ic_outline_thumb_down_24)
                postCommentReply.commentId?.let { it1 ->
                    postCommentReply.id?.let { it2 ->
                        firebaseFirestore.collection("posts").document(postId).collection("comments")
                            .document(
                                it1
                            ).collection("reply").document(it2).update(
                                "dislikes",
                                FieldValue.arrayRemove(firebaseAuth.currentUser?.uid)
                            )
                    }
                }
            }
        }
    }

    private fun goToProfilePage(userId: String) {
        val intent = Intent(context, ProfilePage::class.java)
        intent.putExtra("userId", userId)
        context.startActivity(intent)
    }

}