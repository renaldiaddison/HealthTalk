package com.example.healthtalk.lib

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtalk.R
import com.example.healthtalk.databinding.FollowersCardBinding
import com.example.healthtalk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class FollowersRVAdapter(
    private val context: Context,
    private val followers: ArrayList<String>,
    private val recyclerViewInterface: RecyclerViewInterface,
) : RecyclerView.Adapter<FollowersRVAdapter.ViewHolder>() {
    class ViewHolder(
        val context: Context,
        val binding: FollowersCardBinding,
        val recyclerViewInterface: RecyclerViewInterface,
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
        return FollowersRVAdapter.ViewHolder(
            context,
            FollowersCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), recyclerViewInterface
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val follower: String = followers[position]

        val firebaseFirestore = FirebaseFirestore.getInstance()
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseFirestore.collection("users").document(follower).addSnapshotListener { it2, error ->
            val curr = it2?.toObject(User::class.java)

            if (curr != null) {
                Picasso.get().load(curr.profilePicture)
                    .into(holder.binding.profilePicture)

                holder.binding.usernameTV.text = curr.username
            }

        }

        firebaseAuth.currentUser?.let {
            firebaseFirestore.collection("users").document(it.uid)
                .addSnapshotListener { it2, error ->
                    val curr = it2?.toObject(User::class.java)

                    if (curr != null) {
                        if (curr.following.contains(follower)) {
                            holder.binding.btn.setText(R.string.following)
                        } else {
                            holder.binding.btn.setText(R.string.followers_follow)
                        }
                    }

                }
        }


        holder.binding.btn.setOnClickListener {
            if (holder.binding.btn.text.toString() == context.getString(R.string.following)) {
                firebaseFirestore.collection("users").document(follower).update(
                    "followers", FieldValue.arrayRemove(
                        firebaseAuth.currentUser!!.uid
                    )
                )
                firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                    .update(
                        "following", FieldValue.arrayRemove(
                            follower
                        )
                    )
            } else if (holder.binding.btn.text.toString() == context.getString(R.string.followers_follow)) {
                firebaseFirestore.collection("users").document(follower).update(
                    "followers", FieldValue.arrayUnion(
                        firebaseAuth.currentUser!!.uid
                    )
                )
                firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                    .update(
                        "following", FieldValue.arrayUnion(
                            follower
                        )
                    )
            }
        }

    }

    override fun getItemCount(): Int {
        return followers.size
    }
}
