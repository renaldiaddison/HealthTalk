package com.example.beeptalk.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.beeptalk.R
import com.example.beeptalk.databinding.FragmentAddBinding
import com.example.beeptalk.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var videoView: VideoView
    private lateinit var caption: String

    private var videoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(layoutInflater, container, false)

        videoView = binding.videoV

        binding.apply {
            postBtn.setOnClickListener {
                caption = videoTitleEt.text.toString()
                if (caption.isEmpty()) Toast.makeText(
                    context,
                    getText(R.string.please_input_title),
                    Toast.LENGTH_SHORT
                ).show()
                else if (videoUri == null) Toast.makeText(
                    context,
                    getText(R.string.please_choose_video),
                    Toast.LENGTH_SHORT
                ).show()
                else uploadVideoToFirebase()
            }

            chooseBtn.setOnClickListener {
                pickVideo()
            }

        }

        return binding.root
    }

    private fun uploadVideoToFirebase(
    ) {
        Toast.makeText(context, getText(R.string.start_uploading), Toast.LENGTH_SHORT).show()

        val timestamp = "" + System.currentTimeMillis()
        val filePathAndName = "videos/video_$timestamp"
        val storageRef = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageRef.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { videoUrl ->
                    val post = Post(
                        videoUrl = videoUrl.toString(),
                        userId = FirebaseAuth.getInstance().currentUser?.uid,
                        caption = caption
                    )
                    FirebaseFirestore.getInstance().collection("posts").add(post)
                        .addOnSuccessListener {
                            Toast.makeText(context, getText(R.string.post_created), Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, getText(R.string.post_failed), Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT

        pickVideoFromGallery.launch(intent)
    }

    private var pickVideoFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                videoUri = result.data!!.data
                val mediaController = MediaController(context)
                mediaController.setAnchorView(videoView)
                videoView.setMediaController(mediaController)
                videoView.setVideoURI(result.data!!.data)
                videoView.requestFocus()
                videoView.setOnPreparedListener {
                    videoView.start()
                }

                videoView.setOnCompletionListener {
                    videoView.start()
                }

            }
        }

}