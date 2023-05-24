package com.example.beeptalk.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beeptalk.databinding.FragmentNotificationBinding
import com.example.beeptalk.lib.NotificationRVAdapter
import com.example.beeptalk.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var notifications: ArrayList<Notification>

    private lateinit var notificationRVAdapter: NotificationRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(layoutInflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding.notificationRV.layoutManager = LinearLayoutManager(context)
        binding.notificationRV.setHasFixedSize(true)

        notifications = arrayListOf()

        notificationRVAdapter = context?.let { NotificationRVAdapter(it, notifications) }!!

        binding.notificationRV.adapter = notificationRVAdapter

        FirebaseAuth.getInstance().currentUser?.let { getNotifications(it.uid) }

        return binding.root
    }

    private fun getNotifications(uid: String) {
        db.collection("users").document(uid).collection("notifications")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notifications.clear()
                    for (document in snapshot.documents) {
                        val curr = document.toObject(Notification::class.java)
                        if (curr != null) {
                            if(curr.userId != uid) {
                                curr.let { it1 -> notifications.add(it1) }
                            }
                        }


                    }
                }
                notificationRVAdapter.notifyDataSetChanged()

                if (notifications.isEmpty()) {
                    binding.noRecentNotificationTV.visibility = View.VISIBLE
                } else {
                    binding.noRecentNotificationTV.visibility = View.GONE
                }
            }
    }

}