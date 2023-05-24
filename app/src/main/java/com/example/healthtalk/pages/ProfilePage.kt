package com.example.healthtalk.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityProfilePageBinding
import com.example.healthtalk.lib.PostRVAdapter
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.lib.TabVPAdapter
import com.example.healthtalk.models.Notification
import com.example.healthtalk.models.Post
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfilePage : AppCompatActivity(), RecyclerViewInterface {

    private lateinit var binding: ActivityProfilePageBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var posts: ArrayList<Post>
    private lateinit var postRVAdapter: PostRVAdapter
    private var isThisUser: Boolean = true

    private var followers: ArrayList<String> = arrayListOf()
    private var btnDisabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        posts = arrayListOf()


        postRVAdapter = PostRVAdapter(this, posts, this)
        binding.postRV.adapter = postRVAdapter
        binding.postRV.layoutManager =
            GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false)
        binding.postRV.setHasFixedSize(true)

        val userId = intent.getStringExtra("userId")

        if (userId != null) {
            firebaseFirestore.collection("users").document(userId)
                .addSnapshotListener { value, error ->
                    val data = value?.data
                    if (data != null) {
                        followers = data["followers"] as ArrayList<String>

                        binding.nameTV.text = data["name"] as String
                        binding.usernameTV.text = "@" + data["username"] as String
                        Picasso.get().load(data["profilePicture"] as String)
                            .into(binding.profilePicture)
                        binding.bio.text = data["bio"] as String
                        binding.followersCount.text =
                            (data["followers"] as ArrayList<String>).size.toString()
                        binding.followingCount.text =
                            (data["following"] as ArrayList<String>).size.toString()

                        if (userId != firebaseAuth.currentUser?.uid) {
                            isThisUser = false
                            binding.profileMenu.visibility = View.GONE
                            binding.tabViewPager.visibility = View.GONE
                            binding.tabLayout.visibility = View.GONE
                            if (firebaseAuth.currentUser?.uid?.let { it1 ->
                                    (data["followers"] as ArrayList<String>).contains(
                                        it1
                                    )
                                } == true) {
                                binding.button.setText(R.string.following)
                            } else if (firebaseAuth.currentUser?.uid?.let { it1 ->
                                    (data["followers"] as ArrayList<String>).contains(
                                        it1
                                    )
                                } == false) {
                                binding.button.setText(R.string.follow)
                            }
                        } else {
                            binding.postRV.visibility = View.GONE
                        }

                    }

                    btnDisabled = false
                }
            getPosts(userId)

            binding.button.setOnClickListener {
                if (!btnDisabled) {
                    if (isThisUser) {
                        goToEditProfilePage()
                    } else {
                        if (firebaseAuth.currentUser?.let { it1 -> followers.contains(it1.uid) } == true) {
                            binding.button.setText(R.string.follow)
                            firebaseFirestore.collection("users").document(userId).update(
                                "followers", FieldValue.arrayRemove(
                                    firebaseAuth.currentUser!!.uid
                                )
                            )
                            firebaseFirestore.collection("users")
                                .document(firebaseAuth.currentUser!!.uid).update(
                                    "following", FieldValue.arrayRemove(
                                        userId
                                    )
                                )
                        } else {
                            binding.button.setText(R.string.following)
                            firebaseFirestore.collection("users").document(userId).update(
                                "followers", FieldValue.arrayUnion(
                                    firebaseAuth.currentUser!!.uid
                                )
                            )
                            firebaseFirestore.collection("users")
                                .document(firebaseAuth.currentUser!!.uid).update(
                                    "following", FieldValue.arrayUnion(
                                        userId
                                    )
                                )

                            val notification =
                                Notification(userId, firebaseAuth.currentUser!!.uid, "followYou")
                            firebaseFirestore.collection("users").document(userId)
                                .collection("notifications")
                                .add(notification)
                        }
                    }
                }
            }
        }
        binding.profilePicture.setOnClickListener {
            pickImg()
        }


        binding.tabViewPager.adapter = userId?.let { TabVPAdapter(this, it) }

        TabLayoutMediator(binding.tabLayout, binding.tabViewPager) { tab, index ->

            tab.setIcon(
                when (index) {
                    0 -> {
                        R.drawable.ic_baseline_tab_posted_video_24
                    }
                    1 -> {
                        R.drawable.ic_baseline_tab_like_24
                    }
                    2 -> {
                        R.drawable.ic_baseline_tab_bookmarks_24
                    }
                    else -> {
                        R.drawable.ic_baseline_tab_posted_video_24
                    }
                }
            )
        }.attach()

        binding.profileMenu.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.prof_changePassword -> {
                        goToChangePassword()
                        true
                    }
                    R.id.prof_recFol -> {
                        goToRecentFollowersPage()
                        true
                    }
                    R.id.prof_logOut -> {
                        val gso =
                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        FirebaseAuth.getInstance()
                            .signOut()
                        val loginIntent = Intent(applicationContext, LoginPage::class.java)
                        loginIntent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // clear previous task (optional)
                        startActivity(loginIntent)
                        val googleSignInClient = GoogleSignIn.getClient(this, gso)
                        googleSignInClient.signOut().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, getText(R.string.log_out_success), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            popup.inflate(R.menu.prof_menu)
            popup.show()

        }
    }

    private fun getPosts(userId: String) {
        firebaseFirestore.collection("posts").whereEqualTo("userId", userId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                querySnapshot?.let {
                    posts.clear()
                    for (document in querySnapshot.documents) {
                        val curr = document.toObject(Post::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> posts.add(it1) }
                    }

                    postRVAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun pickImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        pickImgFromGallery.launch(intent)
    }

    private var pickImgFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                firebaseAuth.currentUser?.let {
                    uploadImage(it.uid, result.data!!.data!!) { imageUrl ->
                        firebaseFirestore.collection("users").document(it.uid)
                            .update("profilePicture", imageUrl).addOnSuccessListener {
                                Toast.makeText(this, getText(R.string.profile_picture_updated), Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }

            }
        }

    private fun uploadImage(
        userId: String,
        filePath: Uri,
        callback: (imageUrl: String) -> Unit
    ) {
        val storageRef = firebaseStorage.getReference("$userId/profilePicture/")
        storageRef.putFile(filePath).addOnSuccessListener { task ->
            task.storage.downloadUrl.addOnSuccessListener { imageUrl ->
                callback.invoke(imageUrl.toString())
            }
        }.addOnFailureListener {
            return@addOnFailureListener
        }
    }

    private fun goToEditProfilePage() {
        val intent = Intent(this, EditProfilePage::class.java)
        startActivity(intent)
    }

    private fun goToRecentFollowersPage() {
        val intent = Intent(this, RecentFollowersPage::class.java)
        startActivity(intent)
    }

    private fun goToChangePassword() {
        val intent = Intent(this, ChangePasswordPage::class.java)
        startActivity(intent)
    }

    override fun onItemClick(position: Int) {
        posts[position].id?.let { goToSingleVideoPage(it) }
    }

    private fun goToSingleVideoPage(postId: String) {
        val intent = Intent(this, SingleVideoPage::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

}