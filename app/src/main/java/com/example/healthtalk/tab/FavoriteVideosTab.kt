package com.example.healthtalk.tab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.R
import com.example.healthtalk.databinding.FragmentFavoriteVideosTabBinding
import com.example.healthtalk.lib.PostRVAdapter
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.models.Post
import com.example.healthtalk.pages.SingleVideoPage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FavoriteVideosTab(private var userId: String) : Fragment(), RecyclerViewInterface {

    private lateinit var binding: FragmentFavoriteVideosTabBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var posts: ArrayList<Post>
    private lateinit var postRVAdapter: PostRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteVideosTabBinding.inflate(layoutInflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        posts = arrayListOf()
        postRVAdapter = context?.let { PostRVAdapter(it, posts, this) }!!

        binding.postRV.adapter = postRVAdapter
        binding.postRV.layoutManager =
            GridLayoutManager(context, 3, LinearLayoutManager.VERTICAL, false)
        binding.postRV.setHasFixedSize(true)
        getPosts()

        return binding.root
    }

    private fun getPosts() {
        firebaseFirestore.collection("posts").whereArrayContains("favorites", userId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(context, getText(R.string.error_occured), Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    posts.clear()
                    for (document in querySnapshot.documents) {
                        var curr = document.toObject(Post::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> posts.add(it1) }
                    }

                    postRVAdapter.notifyDataSetChanged()
                }
            }
    }

    override fun onItemClick(position: Int) {
        posts[position].id?.let { goToSingleVideoPage(it) }
    }

    private fun goToSingleVideoPage(postId: String) {
        val intent = Intent(context, SingleVideoPage::class.java)
        intent.putExtra("postId", postId)
        startActivity(intent)
    }

}