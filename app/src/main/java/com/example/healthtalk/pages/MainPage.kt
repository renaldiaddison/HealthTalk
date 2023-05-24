package com.example.healthtalk.pages

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.healthtalk.R
import com.example.healthtalk.databinding.ActivityMainPageBinding
import com.example.healthtalk.fragments.*

class MainPage : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding

    private val homeFragment = HomeFragment()
    private val threadFragment = ThreadFragment()
    private val addFragment = AddFragment()
    private val notificationFragment = NotificationFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainPageBinding.inflate(layoutInflater)
        super.setContentView(binding.root)

        replaceFragment(homeFragment)

        binding.btmNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(homeFragment)
                }
                R.id.nav_thread -> {
                    replaceFragment(threadFragment)
                }
                R.id.nav_add -> {
                    replaceFragment(addFragment)
                }
                R.id.nav_notification -> {
                    replaceFragment(notificationFragment)
                }
                R.id.nav_profile -> {
                    replaceFragment(profileFragment)
                }
            }
            true
        }

            replaceFragment(homeFragment)
        }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_wrapper, fragment)
        fragmentTransaction.commit()
    }

}