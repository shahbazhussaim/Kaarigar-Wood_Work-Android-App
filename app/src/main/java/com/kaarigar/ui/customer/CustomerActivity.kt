package com.kaarigar.ui.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaarigar.R
import com.kaarigar.ui.customer.CustomerHomeFragment

class CustomerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.customer_container) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup Bottom Nav
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav_view)
        androidx.navigation.ui.NavigationUI.setupWithNavController(bottomNav, navController)
        
        // Reuse the main graph but start at Customer Home
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(R.id.customerHomeFragment)
        navController.graph = graph
    }
}
