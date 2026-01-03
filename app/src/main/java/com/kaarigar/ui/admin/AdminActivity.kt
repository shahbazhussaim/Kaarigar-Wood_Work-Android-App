package com.kaarigar.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaarigar.R

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.admin_container) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(R.id.adminDashboard)
        navController.graph = graph
    }
}
