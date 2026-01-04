package com.kaarigar.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kaarigar.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Hide bottom nav on specific destinations
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.loginFragment,
                R.id.registerFragment,
                R.id.workerDashboardFragment,
                R.id.adminDashboard,
                R.id.addProductFragment,
                R.id.manageOrdersFragment,
                R.id.manageWorkersFragment,
                R.id.manageRequestsFragment -> {
                    bottomNav.visibility = View.GONE
                }

                else -> {
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }
}
