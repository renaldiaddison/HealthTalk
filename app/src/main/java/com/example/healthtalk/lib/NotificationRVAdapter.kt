package com.example.healthtalk.lib

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthtalk.R
import com.example.healthtalk.databinding.CardNotificationBinding
import com.example.healthtalk.models.Notification
import com.example.healthtalk.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class NotificationRVAdapter(
    private val context: Context,
    private val notifications: ArrayList<Notification>,
) : RecyclerView.Adapter<NotificationRVAdapter.ViewHolder>() {

    class ViewHolder(
        val binding: CardNotificationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CardNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]

        val firebaseFirestore = FirebaseFirestore.getInstance()

        holder.binding.replyCommentTV.visibility = View.GONE
        notification.userId?.let { firebaseFirestore.collection("users").document(it) }
            ?.addSnapshotListener { snapshot, _ ->
                val curr = snapshot?.toObject(User::class.java)

                if (curr != null) {
                    Picasso.get().load(curr.profilePicture)
                        .into(holder.binding.profilePicture)

                    holder.binding.usernameTV.text = curr.username
                }

            }

        if (notification.type == "followYou") {
            holder.binding.notificationType.setText(R.string.followingYou)
        } else if (notification.type == "likeComment") {
            holder.binding.notificationType.setText(R.string.likeComment)
        } else if (notification.type == "likeVideo") {
            holder.binding.notificationType.setText(R.string.likeVid)
        } else if (notification.type == "upvoteThread") {
            holder.binding.notificationType.setText(R.string.upvoteThread)
        } else if (notification.type == "likeReply") {
            holder.binding.notificationType.setText(R.string.likeReply)
        } else if (notification.type?.contains("replyComment") == true) {
            val strs = notification.type?.split("-")?.toTypedArray()
            var stringNotif = ""
            if (strs != null) {
                for ((index, str) in strs.withIndex()) {
                    if (index != 0) {
                        stringNotif += str;
                    }
                }
            }
            holder.binding.notificationType.setText(R.string.replyComment)
            holder.binding.replyCommentTV.visibility = View.VISIBLE
            holder.binding.replyCommentTV.text = stringNotif
        } else {
            val strs = notification.type?.split("-")?.toTypedArray()
            var stringNotif = ""
            if (strs != null) {
                for ((index, str) in strs.withIndex()) {
                    if (index != 0) {
                        stringNotif += str;
                    }
                }
            }
            holder.binding.notificationType.setText(R.string.commentThread)
            holder.binding.replyCommentTV.visibility = View.VISIBLE
            holder.binding.replyCommentTV.text = stringNotif
        }

    }

    override fun getItemCount(): Int {
        return notifications.size
    }
}