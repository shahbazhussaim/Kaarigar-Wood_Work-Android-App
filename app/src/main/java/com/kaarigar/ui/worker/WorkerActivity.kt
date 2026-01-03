package com.kaarigar.ui.worker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kaarigar.R

class WorkerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.worker_container) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(R.id.workerDashboardFragment)
        navController.graph = graph
    }
}
