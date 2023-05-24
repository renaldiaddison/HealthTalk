package com.example.healthtalk.lib

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.healthtalk.tab.FavoriteVideosTab
import com.example.healthtalk.tab.LikedVideosTab
import com.example.healthtalk.tab.PostedVideosTab

class TabVPAdapter(fragmentActivity: FragmentActivity, private var userId: String) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {PostedVideosTab(userId)}
            1 -> { LikedVideosTab(userId)}
            2 -> {FavoriteVideosTab(userId)}
            else -> {throw Resources.NotFoundException("Position not Found!")}
        }
    }


}