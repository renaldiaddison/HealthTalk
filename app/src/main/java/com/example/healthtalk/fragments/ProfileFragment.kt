package com.example.healthtalk.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.healthtalk.R
import com.example.healthtalk.databinding.FragmentProfileBinding
import com.example.healthtalk.lib.TabVPAdapter
import com.example.healthtalk.models.Post
import com.example.healthtalk.pages.ChangePasswordPage
import com.example.healthtalk.pages.EditProfilePage
import com.example.healthtalk.pages.LoginPage
import com.example.healthtalk.pages.RecentFollowersPage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var posts: ArrayList<Post>

    private var followers: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        posts = arrayListOf()

        firebaseAuth.currentUser?.uid?.let { it ->
            firebaseFirestore.collection("users").document(it).addSnapshotListener { value, error ->
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
                }
            }

            binding.profilePicture.setOnClickListener {
                pickImg()
            }

            binding.tabViewPager.adapter =
                firebaseAuth.currentUser?.uid?.let { TabVPAdapter(requireActivity(), it) }

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

            binding.button.setOnClickListener {
                goToEditProfilePage()
            }


            binding.profileMenu.setOnClickListener {
                val popup = PopupMenu(context, it)
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
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .build()
                            FirebaseAuth.getInstance()
                                .signOut()
                            val loginIntent = Intent(requireContext(), LoginPage::class.java)
                            loginIntent.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK // clear previous task (optional)
                            startActivity(loginIntent)
                            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                            googleSignInClient.signOut().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        getText(R.string.log_out_success),
                                        Toast.LENGTH_SHORT
                                    )
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
        return binding.root
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
                                Toast.makeText(
                                    context,
                                    getText(R.string.profile_picture_updated),
                                    Toast.LENGTH_SHORT
                                )
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
        val intent = Intent(requireContext(), EditProfilePage::class.java)
        startActivity(intent)
    }

    private fun goToRecentFollowersPage() {
        val intent = Intent(requireContext(), RecentFollowersPage::class.java)
        startActivity(intent)
    }

    private fun goToChangePassword() {
        val intent = Intent(requireContext(), ChangePasswordPage::class.java)
        startActivity(intent)
    }

}