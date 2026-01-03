package com.kaarigar.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.ui.MainActivity
import com.kaarigar.ui.customer.CustomerActivity
import com.kaarigar.ui.worker.WorkerActivity
import com.kaarigar.ui.admin.AdminActivity

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Check Firestore for role and navigate
            checkUserRoleAndNavigate(currentUser.uid)
        }
        // NavHostFragment handles the initial fragment (LoginFragment) automatically via nav graph
    }

    private fun checkUserRoleAndNavigate(uid: String) {
        FirebaseFirestore.getInstance()
            .collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "CUSTOMER"
                navigateToRoleActivity(role)
            }
            .addOnFailureListener {
                // If firestore fails, default to login
                supportFragmentManager.beginTransaction()
                    .replace(R.id.auth_container, LoginFragment())
                    .commit()
            }
    }

    fun navigateToRoleActivity(role: String) {
        val intent = when (role.uppercase()) {
            "WORKER" -> Intent(this, WorkerActivity::class.java)
            "ADMIN" -> Intent(this, AdminActivity::class.java)
            else -> Intent(this, CustomerActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    fun navigateToMain() {
        // Fallback or legacy, better to use navigateToRoleActivity
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            checkUserRoleAndNavigate(currentUser.uid)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    
    fun navigateToRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }
}
