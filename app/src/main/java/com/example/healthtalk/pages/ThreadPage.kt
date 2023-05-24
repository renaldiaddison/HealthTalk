package com.example.healthtalk.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityThreadPageBinding
import com.example.healthtalk.lib.RecyclerViewEditInterface
import com.example.healthtalk.lib.RecyclerViewInterface
import com.example.healthtalk.lib.ThreadRVAdapter
import com.example.healthtalk.models.Thread
import com.example.healthtalk.parcel.ThreadID
import com.google.firebase.firestore.FirebaseFirestore

class ThreadPage : AppCompatActivity(), RecyclerViewInterface, RecyclerViewEditInterface {

    private lateinit var binding: ActivityThreadPageBinding
    private lateinit var threads: ArrayList<Thread>
    private lateinit var threadRVAdapter: ThreadRVAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThreadPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        binding.rvThread.layoutManager = LinearLayoutManager(this)
        binding.rvThread.setHasFixedSize(true)

        threads = arrayListOf()

        threadRVAdapter = ThreadRVAdapter(this, threads, this, this)

        binding.rvThread.adapter = threadRVAdapter

        subscribeThreads()
//        getThreads()
    }

    private fun subscribeThreads() {
        db.collection("threads")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, getText(R.string.error_occured), Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                querySnapshot?.let {
                    for (document in querySnapshot.documents) {
                        var curr = document.toObject(Thread::class.java)
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

        intent = Intent(this, ThreadDetailPage::class.java)
        intent.putExtra("thread", threadItem)
        startActivity(intent)
    }

    override fun onItemEdit(position: Int) {
        TODO("Not yet implemented")
    }
}