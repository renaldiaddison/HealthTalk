package com.example.healthtalk.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.healthtalk.databinding.VideoProfileContainerBinding
import com.example.healthtalk.models.Post


class PostRVAdapter(
    private val context: Context,
    private val posts: ArrayList<Post>,
    private val recyclerViewInterface: RecyclerViewInterface,
) : RecyclerView.Adapter<PostRVAdapter.ViewHolder>() {
    class ViewHolder(
        val context: Context,
        val binding: VideoProfileContainerBinding,
        val recyclerViewInterface: RecyclerViewInterface,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    recyclerViewInterface.onItemClick(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            context,
            VideoProfileContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), recyclerViewInterface
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = posts[position]

        val videoUrl = post.videoUrl
        Glide.with(context)
            .asBitmap()
            .load(videoUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    holder.binding.thumbnailPost.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })


    }

    override fun getItemCount(): Int {
        return posts.size
    }

}