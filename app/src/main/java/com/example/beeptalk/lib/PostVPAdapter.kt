package com.example.beeptalk.lib

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.beeptalk.R
import com.example.beeptalk.databinding.VideoContainerBinding
import com.example.beeptalk.models.Notification
import com.example.beeptalk.models.Post
import com.example.beeptalk.pages.CreateThreadPage
import com.example.beeptalk.pages.PostCommentPage
import com.example.beeptalk.pages.ProfilePage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class PostVPAdapter(
    private val context: Context,
    private val posts: ArrayList<Post>,
) : RecyclerView.Adapter<PostVPAdapter.ViewHolder>() {
    class ViewHolder(
        val context: Context,
        val binding: VideoContainerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            context,
            VideoContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = posts[position]
        val db = FirebaseFirestore.getInstance()
        val mPrefs = context.getSharedPreferences("current_user", Context.MODE_PRIVATE);
        val userRef = post.userId?.let { db.collection("users").document(it) }
        val postRef = post.id?.let { it1 -> db.collection("posts").document(it1) }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


        // like btn
        if (post.likes.contains(currentUserId)) {
            holder.binding.like.setImageResource(R.drawable.ic_baseline_like_filled_24)
        }

        // favorite btn
        if (post.favorites.contains(currentUserId)) {
            holder.binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
        }

        // set username and caption
        userRef?.get()?.addOnSuccessListener { document ->
            val data = document.getString("username")
            holder.binding.usernameTV.text = data

            Picasso.get()
                .load(document.getString("profilePicture"))
                .into(holder.binding.profilePicture);
        }?.addOnFailureListener {
        }
        holder.binding.captionTV.text = post.caption


        // set video view
        val uri: Uri = Uri.parse(post.videoUrl)

        holder.binding.homeVideoView.setVideoURI(uri);
        holder.binding.pauseBar.visibility = View.GONE
        holder.binding.homeVideoView.setOnPreparedListener {
            holder.binding.progressBar.visibility = View.GONE
        }

        holder.binding.homeVideoView.setOnFocusChangeListener { view, b ->
            if(b) {
                holder.binding.homeVideoView.start()
            } else {
                holder.binding.homeVideoView.start()
            }
        }
        holder.binding.homeVideoView.setOnCompletionListener {
            holder.binding.homeVideoView.start()
        }
        holder.binding.homeVideoView.start()
        holder.binding.homeVideoView.setOnClickListener {
            if (holder.binding.homeVideoView.isPlaying) {
                holder.binding.pauseBar.visibility = View.VISIBLE
                holder.binding.homeVideoView.pause()
            } else {
                holder.binding.pauseBar.visibility = View.GONE
                holder.binding.homeVideoView.start()
            }
        }

        // show like and comment count
        holder.binding.likeCount.text = post.likes.size.toString()
        post.id?.let {
            db.collection("posts").document(it).collection("comments")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        holder.binding.commentCount.text = snapshot.size().toString()
                    } else {
                        holder.binding.commentCount.text = "0"
                    }
                }
        }
        holder.binding.favoriteCount.text = post.favorites.size.toString()

        // like listener
        holder.binding.like.setOnClickListener {
            if (post.likes.contains(currentUserId)) {
                holder.binding.like.setImageResource(R.drawable.ic_baseline_like_24)
                postRef?.update("likes", FieldValue.arrayRemove(currentUserId))
                    ?.addOnSuccessListener {
                    }?.addOnFailureListener {
                    }
                post.likes.remove(currentUserId)
            } else {
                holder.binding.like.setImageResource(R.drawable.ic_baseline_like_filled_24)
                postRef?.update("likes", FieldValue.arrayUnion(currentUserId))
                    ?.addOnSuccessListener {
                    }?.addOnFailureListener {
                    }
                if (currentUserId != null) {
                    post.likes.add(currentUserId)
                }
                if (post.userId != FirebaseAuth.getInstance().currentUser?.uid) {
                    val notification = Notification(post.userId, currentUserId, "likeVideo")
                    post.userId?.let { it1 ->
                        db.collection("users").document(it1).collection("notifications")
                            .add(notification)
                    }
                }
            }
            holder.binding.likeCount.text = post.likes.size.toString()
        }

        //comment listener
        holder.binding.comment.setOnClickListener {
            post.id?.let { it1 -> goToPostCommentPage(it1) }
        }

        // favorite listener
        holder.binding.favorite.setOnClickListener {
            if (post.favorites.contains(currentUserId)) {
                holder.binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_24)
                postRef?.update("favorites", FieldValue.arrayRemove(currentUserId))
                    ?.addOnSuccessListener {
                    }?.addOnFailureListener {
                    }
                post.favorites.remove(currentUserId)
            } else {
                holder.binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                postRef?.update("favorites", FieldValue.arrayUnion(currentUserId))
                    ?.addOnSuccessListener {
                    }?.addOnFailureListener {
                    }
                if (currentUserId != null) {
                    post.favorites.add(currentUserId)
                }
            }
            holder.binding.favoriteCount.text = post.favorites.size.toString()
        }

        // profile picture listener
        holder.binding.profilePicture.setOnClickListener {
            post.userId?.let { it1 -> goToProfilePage(it1) }
        }

        // stitch listener
        holder.binding.stitch.setOnClickListener {
            post.id?.let { it1 -> goToCreateThreadPage(it1) }
        }

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private fun goToProfilePage(userId: String) {
        val intent = Intent(context, ProfilePage::class.java)
        intent.putExtra("userId", userId)
        context.startActivity(intent)
    }

    private fun goToPostCommentPage(postId: String) {
        val intent = Intent(context, PostCommentPage::class.java)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

    private fun goToCreateThreadPage(postId: String) {
        val intent = Intent(context, CreateThreadPage::class.java)
        intent.putExtra("postId", postId)
        context.startActivity(intent)
    }

}