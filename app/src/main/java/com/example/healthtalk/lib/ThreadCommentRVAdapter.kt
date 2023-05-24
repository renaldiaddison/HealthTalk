package com.example.healthtalk.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtalk.R
import com.example.healthtalk.databinding.CardCommentThreadBinding
import com.example.healthtalk.models.Notification
import com.example.healthtalk.models.ThreadComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ThreadCommentRVAdapter(
    private var comments: ArrayList<ThreadComment>,
    private val recyclerViewInterface: RecyclerViewInterface,
) : RecyclerView.Adapter<ThreadCommentRVAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: CardCommentThreadBinding,
        val recyclerViewInterface: RecyclerViewInterface
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    recyclerViewInterface.onItemClick(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardCommentThreadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), recyclerViewInterface
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        val db = FirebaseFirestore.getInstance()

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        holder.binding.btnDownvote.setImageResource(R.drawable.ic_downvote)
        holder.binding.btnUpvote.setImageResource(R.drawable.ic_upvote)

        if (comment.upvote.contains(currentUserId)) {
            holder.binding.btnUpvote.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_up_24)
        }
        if (comment.downvote.contains(currentUserId)) {
            holder.binding.btnDownvote.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_down_24)
        }

        db.collection("users").document(comment.uid!!).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                holder.binding.tvUsername.text = data["username"] as String
                Picasso.get().load(data["profilePicture"] as String)
                    .into(holder.binding.avUser)
            }

        db.collection("users").document(comment.replyTo).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                holder.binding.tvReply.text = data["username"] as String
            }

        db.collection("threads").document(comment.threadId!!)
            .collection("comments").document(comment.id!!).addSnapshotListener { snapshot, _ ->
                val currComment = snapshot?.toObject(ThreadComment::class.java)
                if (currComment != null) {
                    holder.binding.tvCommentBody.text = currComment.body
                    holder.binding.tvTotalVotes.text =
                        (currComment.upvote.size - currComment.downvote.size).toString()
                }

            }

        holder.binding.btnUpvote.setOnClickListener {
            if (comment.upvote.contains(currentUserId)) return@setOnClickListener
            holder.binding.tvTotalVotes.text = comment.getTotalVotes().toString()
            db.collection("threads").document(comment.threadId)
                .collection("comments").document(comment.id!!)
                .update("upvote", FieldValue.arrayUnion(currentUserId))
            db.collection("threads").document(comment.threadId)
                .collection("comments").document(comment.id!!)
                .update("downvote", FieldValue.arrayRemove(currentUserId))

            if (comment.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                val notification = Notification(comment.uid, currentUserId, "likeComment")
                db.collection("users").document(comment.uid).collection("notifications")
                    .add(notification)
            }
            holder.binding.btnDownvote.setImageResource(R.drawable.ic_downvote)
        }

        holder.binding.btnDownvote.setOnClickListener {
            if (comment.downvote.contains(currentUserId)) return@setOnClickListener
            holder.binding.tvTotalVotes.text = comment.getTotalVotes().toString()
            db.collection("threads").document(comment.threadId)
                .collection("comments").document(comment.id!!)
                .update("downvote", FieldValue.arrayUnion(currentUserId))
            db.collection("threads").document(comment.threadId)
                .collection("comments").document(comment.id!!)
                .update("upvote", FieldValue.arrayRemove(currentUserId))

            holder.binding.btnUpvote.setImageResource(R.drawable.ic_upvote)
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    fun setComments(comments: ArrayList<ThreadComment>) {
        this.comments = comments
    }
}