package com.example.beeptalk.lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.beeptalk.R
import com.example.beeptalk.databinding.CardThreadBinding
import com.example.beeptalk.helper.getRelativeString
import com.example.beeptalk.models.Notification
import com.example.beeptalk.models.Post
import com.example.beeptalk.models.Thread
import com.example.beeptalk.pages.SingleVideoPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ThreadRVAdapter(
    private val context: Context,
    private val threads: ArrayList<Thread>,
    private val recyclerViewInterface: RecyclerViewInterface,
    private val recyclerViewEditInterface: RecyclerViewEditInterface,
) : RecyclerView.Adapter<ThreadRVAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: CardThreadBinding,
        val recyclerViewInterface: RecyclerViewInterface,
        val recyclerViewEditInterface: RecyclerViewEditInterface
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
            CardThreadBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), recyclerViewInterface, recyclerViewEditInterface
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val thread: Thread = threads[position]

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        holder.binding.thumbnailPostLinearLayout.visibility = View.GONE
        if (thread.stitch == null) {
            holder.binding.thumbnailPostLinearLayout.visibility = View.GONE
        } else {
            holder.binding.thumbnailPostLinearLayout.visibility = View.VISIBLE
            holder.binding.thumbnailPost.setOnClickListener {
                goToSingleVideoPage(thread.stitch)
            }
            db.collection("posts").document(thread.stitch).addSnapshotListener { snapshot, _ ->
                val post = snapshot?.toObject(Post::class.java)
                if (post != null) {
                    val videoUrl = post.videoUrl
                    Glide.with(context)
                        .asBitmap()
                        .load(videoUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                            ) {
                                holder.binding.thumbnailPost.setImageBitmap(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }
            }
        }

        db.collection("users").document(thread.uid!!).get()
            .addOnSuccessListener {
                val data = it.data ?: return@addOnSuccessListener
                holder.binding.tvUsername.text = data["username"] as String
                Picasso.get().load(data["profilePicture"] as String)
                    .into(holder.binding.avUser)
            }

        holder.binding.btnDownvote.setImageResource(R.drawable.ic_downvote)
        holder.binding.btnUpvote.setImageResource(R.drawable.ic_upvote)

        if (thread.upvote.contains(currentUser)) {
            holder.binding.btnUpvote.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_up_24)
        }
        if (thread.downvote.contains(currentUser)) {
            holder.binding.btnDownvote.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_down_24)
        }

        db.collection("threads").document(thread.id!!).addSnapshotListener { snapshot, _ ->
            val currThread = snapshot?.toObject(Thread::class.java)
            if (currThread != null) {

                holder.binding.tvCreatedAt.text = getRelativeString(currThread.createdAt)
                holder.binding.tvThreadBody.text = currThread.body
                holder.binding.tvTotalVotes.text =
                    (currThread.upvote.size - currThread.downvote.size).toString()
            }
        }

        if (thread.uid == currentUser) {
            holder.binding.btnEdit.visibility = View.VISIBLE
            holder.binding.btnEdit.setOnClickListener {
                if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    recyclerViewEditInterface.onItemEdit(holder.bindingAdapterPosition)
                }
            }
        } else holder.binding.btnEdit.visibility = View.GONE

        holder.binding.btnUpvote.setOnClickListener {
            if (thread.upvote.contains(currentUser)) return@setOnClickListener
            db.collection("threads").document(thread.id!!)
                .update("upvote", FieldValue.arrayUnion(currentUser))
            db.collection("threads").document(thread.id!!)
                .update("downvote", FieldValue.arrayRemove(currentUser))

            if (thread.uid != FirebaseAuth.getInstance().currentUser?.uid) {
                val notification = Notification(thread.uid, currentUser, "upvoteThread")
                db.collection("users").document(thread.uid).collection("notifications")
                    .add(notification)
            }
            holder.binding.btnDownvote.setImageResource(R.drawable.ic_downvote)
        }

        holder.binding.btnDownvote.setOnClickListener {
            if (thread.downvote.contains(currentUser)) return@setOnClickListener
            db.collection("threads").document(thread.id!!)
                .update("downvote", FieldValue.arrayUnion(currentUser))
            db.collection("threads").document(thread.id!!)
                .update("upvote", FieldValue.arrayRemove(currentUser))

            holder.binding.btnUpvote.setImageResource(R.drawable.ic_upvote)
        }
    }

    private fun goToSingleVideoPage(postId: String) {
        val intent = Intent(context, SingleVideoPage::class.java)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return threads.size
    }
}