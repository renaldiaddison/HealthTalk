package com.example.beeptalk.pages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.beeptalk.R
import com.example.beeptalk.databinding.ActivitySingleVideoPageBinding
import com.example.beeptalk.models.Notification
import com.example.beeptalk.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class SingleVideoPage : AppCompatActivity() {

    private lateinit var binding: ActivitySingleVideoPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleVideoPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        val postId = intent.getStringExtra("postId")

        if (postId != null) {
            firebaseFirestore.collection("posts").document(postId)
                .addSnapshotListener { it2, error ->
                    val post = it2?.toObject(Post::class.java)
                    post?.id = it2?.id.toString()

                    if (post != null) {
                        val userRef =
                            post.userId?.let { firebaseFirestore.collection("users").document(it) }
                        val postRef = post.id?.let { it1 ->
                            firebaseFirestore.collection("posts").document(it1)
                        }
                        val currentUserId = firebaseAuth.currentUser?.uid

                        // like btn
                        if (post.likes.contains(currentUserId)) {
                            binding.like.setImageResource(R.drawable.ic_baseline_like_filled_24)
                        }

                        // favorite btn
                        if (post.favorites.contains(currentUserId)) {
                            binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                        }

                        // set username and caption
                        userRef?.get()?.addOnSuccessListener { document ->
                            val data = document.getString("username")
                            binding.usernameTV.text = data

                            Picasso.get()
                                .load(document.getString("profilePicture"))
                                .into(binding.profilePicture);
                        }?.addOnFailureListener {
                        }
                        binding.captionTV.text = post.caption

                        // set video view
                        val uri: Uri = Uri.parse(post.videoUrl)

                        binding.homeVideoView.setVideoURI(uri);
                        binding.pauseBar.visibility = View.GONE
                        binding.homeVideoView.setOnPreparedListener {
                            binding.progressBar.visibility = View.GONE
                        }
                        binding.homeVideoView.setOnCompletionListener {
                            binding.homeVideoView.start()
                        }
                        binding.homeVideoView.start()
                        binding.homeVideoView.setOnClickListener {
                            if (binding.homeVideoView.isPlaying) {
                                binding.pauseBar.visibility = View.VISIBLE
                                binding.homeVideoView.pause()
                            } else {
                                binding.pauseBar.visibility = View.GONE
                                binding.homeVideoView.start()
                            }
                        }

                        // show like and comment count
                        binding.likeCount.text = post.likes.size.toString()
                        post.id?.let {
                            firebaseFirestore.collection("posts").document(it)
                                .collection("comments")
                                .addSnapshotListener { snapshot, _ ->
                                    if (snapshot != null) {
                                        binding.commentCount.text =
                                            snapshot.size().toString()
                                    } else {
                                        binding.commentCount.text = "0"
                                    }
                                }
                        }
                        binding.favoriteCount.text = post.favorites.size.toString()

                        // like listener
                        binding.like.setOnClickListener {
                            if (post.likes.contains(currentUserId)) {
                                binding.like.setImageResource(R.drawable.ic_baseline_like_24)
                                postRef?.update("likes", FieldValue.arrayRemove(currentUserId))
                                    ?.addOnSuccessListener {
                                    }?.addOnFailureListener {
                                    }
                                post.likes.remove(currentUserId)
                            } else {
                                binding.like.setImageResource(R.drawable.ic_baseline_like_filled_24)
                                postRef?.update("likes", FieldValue.arrayUnion(currentUserId))
                                    ?.addOnSuccessListener {
                                    }?.addOnFailureListener {
                                    }
                                if (currentUserId != null) {
                                    post.likes.add(currentUserId)
                                }
                                if (post.userId != FirebaseAuth.getInstance().currentUser?.uid) {
                                    val notification =
                                        Notification(post.userId, currentUserId, "likeVideo")
                                    post.userId?.let { it1 ->
                                        firebaseFirestore.collection("users").document(it1)
                                            .collection("notifications")
                                            .add(notification)
                                    }
                                }
                            }
                            binding.likeCount.text = post.likes.size.toString()
                        }

                        //comment listener
                        binding.comment.setOnClickListener {
                            goToPostCommentPage(post.id!!)
                        }

                        // favorite listener
                        binding.favorite.setOnClickListener {
                            if (post.favorites.contains(currentUserId)) {
                                binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_24)
                                postRef?.update("favorites", FieldValue.arrayRemove(currentUserId))
                                    ?.addOnSuccessListener {
                                    }?.addOnFailureListener {
                                    }
                                post.favorites.remove(currentUserId)
                            } else {
                                binding.favorite.setImageResource(R.drawable.ic_baseline_bookmark_added_24)
                                postRef?.update("favorites", FieldValue.arrayUnion(currentUserId))
                                    ?.addOnSuccessListener {
                                    }?.addOnFailureListener {
                                    }
                                if (currentUserId != null) {
                                    post.favorites.add(currentUserId)
                                }
                            }
                            binding.favoriteCount.text = post.favorites.size.toString()
                        }

                        // profile picture listener
                        binding.profilePicture.setOnClickListener {
                            post.userId?.let { it1 -> goToProfilePage(it1) }
                        }

                        // stitch listener
                        binding.stitch.setOnClickListener {
                            post.id?.let { it1 -> goToCreateThreadPage(it1) }
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

    private fun goToCreateThreadPage(postId: String) {
        val intent = Intent(this, CreateThreadPage::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

    private fun goToPostCommentPage(postId: String) {
        val intent = Intent(this, PostCommentPage::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }
}