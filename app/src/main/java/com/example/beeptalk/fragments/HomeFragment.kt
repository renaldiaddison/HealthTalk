package com.example.beeptalk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.beeptalk.databinding.FragmentHomeBinding
import com.example.beeptalk.lib.PostVPAdapter
import com.example.beeptalk.lib.RecyclerViewInterface
import com.example.beeptalk.models.Post
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var posts: ArrayList<Post>
    private lateinit var postVPAdapter: PostVPAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        db = FirebaseFirestore.getInstance()
        posts = arrayListOf()

        postVPAdapter = context?.let { PostVPAdapter(it, posts) }!!
        binding.homeViewPager.adapter = postVPAdapter

        binding.homeViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == postVPAdapter.itemCount - 1) {
                    posts.addAll(posts);
                    postVPAdapter.notifyDataSetChanged()
                }
            }
        })

        getPosts()
        return binding.root
    }

    private fun getPosts() {
        db.collection("posts")
            .get().addOnSuccessListener {
                posts.clear();
                for (document in it.documents) {
                    val curr = document.toObject(Post::class.java)
                    curr?.id = document.id.toString()
                    curr?.let { it1 -> posts.add(it1) }
                }
                posts.shuffle();
                postVPAdapter.notifyDataSetChanged()
            }
    }

}