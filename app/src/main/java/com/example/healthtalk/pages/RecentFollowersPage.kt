package com.example.healthtalk.pages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityRecentFollowersPageBinding
import com.example.healthtalk.lib.FollowersRVAdapter
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RecentFollowersPage : AppCompatActivity(), RecyclerViewInterface {

    private lateinit var binding: ActivityRecentFollowersPageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage

    private var followers: ArrayList<String> = arrayListOf()
    private lateinit var followerRVAdapter: FollowersRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentFollowersPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        followers = arrayListOf()
        followerRVAdapter = FollowersRVAdapter(this, followers, this)
        binding.recentFollowersRV.adapter = followerRVAdapter
        binding.recentFollowersRV.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recentFollowersRV.setHasFixedSize(true)

        getFollowers();


    }

    private fun getFollowers() {
        firebaseAuth.currentUser?.let { user ->
            firebaseFirestore.collection("users").document(user.uid)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(this, getText(R.string.error_occured), Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }

                    querySnapshot?.let {
                        followers.clear()
                        val curr = querySnapshot.toObject(User::class.java)
                        if (curr != null) {
                            followers.addAll(curr.followers)
                        }

                        followerRVAdapter.notifyDataSetChanged()

                        if (followers.isNotEmpty()) {
                            binding.noRecentFollowersTV.visibility = View.GONE
                        } else {
                            binding.noRecentFollowersTV.visibility = View.VISIBLE
                        }
                    }
                }
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra("userId", followers[position])
        startActivity(intent)
    }
}