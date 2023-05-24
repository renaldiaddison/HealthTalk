package com.example.healthtalk.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.databinding.FragmentThreadBinding
import com.example.healthtalk.lib.RecyclerViewEditInterface
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.lib.ThreadRVAdapter
import com.example.healthtalk.models.Thread
import com.example.healthtalk.pages.CreateThreadPage
import com.example.healthtalk.pages.EditThreadPage
import com.example.healthtalk.pages.ThreadDetailPage
import com.example.healthtalk.parcel.ThreadID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ThreadFragment : Fragment(), RecyclerViewInterface, RecyclerViewEditInterface {

    private lateinit var binding: FragmentThreadBinding
    private lateinit var threads: ArrayList<Thread>
    private lateinit var threadRVAdapter: ThreadRVAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThreadBinding.inflate(layoutInflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding.rvThread.layoutManager = LinearLayoutManager(context)
        binding.rvThread.setHasFixedSize(true)

        threads = arrayListOf()

        threadRVAdapter =
            context?.let { ThreadRVAdapter(it, threads, this, this) }!!
        binding.rvThread.adapter = threadRVAdapter

        subscribeThreads()
//        getThreads()

        binding.FABCreate.setOnClickListener {
            goToCreateThreadPage()
        }

        return binding.root
    }

    private fun subscribeThreads() {
        db.collection("threads").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                querySnapshot?.let {
                    threads.clear()
                    for (document in querySnapshot.documents) {
                        val curr = document.toObject(Thread::class.java)
                        curr?.id = document.id.toString()
                        curr?.let { it1 -> threads.add(it1) }
                    }

                    threadRVAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun getThreads() {
        db.collection("threads")
            .get().addOnSuccessListener {
                for (document in it.documents) {
                    var curr = document.toObject(Thread::class.java)
                    curr?.id = document.id.toString()
                    curr?.let { it1 -> threads.add(it1) }
                }

                threadRVAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(position: Int) {
        val curr = threads[position]
        val id = curr.id
        val uid = curr.uid
        val body = curr.body
        val stitch = curr.stitch
        val upvote = curr.upvote
        val downvote = curr.downvote
        val createdAt = curr.createdAt

        val threadItem: ThreadID =
            ThreadID(id!!, uid!!, body!!, stitch, upvote, downvote, createdAt)

        val intent = Intent(context, ThreadDetailPage::class.java)
        intent.putExtra("thread", threadItem)
        startActivity(intent)
    }

    override fun onItemEdit(position: Int) {
        val curr = threads[position]
        val id = curr.id
        val uid = curr.uid
        val body = curr.body
        val stitch = curr.stitch
        val upvote = curr.upvote
        val downvote = curr.downvote
        val createdAt = curr.createdAt

        val threadItem: ThreadID =
            ThreadID(id!!, uid!!, body!!, stitch, upvote, downvote, createdAt)

        val intent = Intent(context, EditThreadPage::class.java)
        intent.putExtra("thread", threadItem)
        startActivity(intent)
    }

    private fun goToCreateThreadPage() {
        val intent = Intent(context, CreateThreadPage::class.java)
        startActivity(intent)
    }

}