package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.databinding.FragmentWorkerRegisterBinding
import com.kaarigar.ui.auth.AuthViewModel

class WorkerRegisterFragment : Fragment() {

    private var _binding: FragmentWorkerRegisterBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // We do NOT use the AuthViewModel because it uses the global FirebaseAuth instance.
        // Creating a user would sign out the Admin.
        // We must use a secondary FirebaseApp instance.

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val role = "WORKER"

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                createWorkerAccount(email, password, name, role, phone)
            } else {
                context?.let {
                    Toast.makeText(it, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createWorkerAccount(
            email: String,
            pass: String,
            name: String,
            role: String,
            phone: String
    ) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        try {
            // 1. Initialize Secondary App (safely)
            val appName = "WorkerApp"
            val defaultOptions = com.google.firebase.FirebaseApp.getInstance().options
            val secondaryApp = try {
                com.google.firebase.FirebaseApp.getInstance(appName)
            } catch (e: Exception) {
                com.google.firebase.FirebaseApp.initializeApp(requireContext(), defaultOptions, appName)
            }

            // 2. Get Auth instance for this app
            val workerAuth = com.google.firebase.auth.FirebaseAuth.getInstance(secondaryApp)

            // 3. Create User
            workerAuth
                    .createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { result ->
                        // User created in Secondary App
                        val userId = result.user?.uid
                        if (userId != null) {
                            saveWorkerToFirestore(userId, name, email, role, phone)
                        } else {
                            handleError("Created user has no ID")
                        }

                        // Sign out the worker auth immediately to clean up
                        workerAuth.signOut()
                    }
                    .addOnFailureListener { e -> handleError(e.message ?: "Registration Failed") }
        } catch (e: Exception) {
            handleError(e.message ?: "Error initializing worker app")
        }
    }

    private fun saveWorkerToFirestore(
            uid: String,
            name: String,
            email: String,
            role: String,
            phone: String
    ) {
        val userMap =
                hashMapOf(
                        "id" to uid,
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "phone" to phone,
                        "timestamp" to System.currentTimeMillis()
                )

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        val userDoc = db.collection("users").document(uid)
        batch.set(userDoc, userMap)

        val workerDoc = db.collection("workers").document(uid)
        val workerMap = hashMapOf(
            "workerId" to uid,
            "name" to name,
            "email" to email,
            "isActive" to true,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        batch.set(workerDoc, workerMap)

        batch.commit()
            .addOnSuccessListener {
                if (_binding != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(context, "Worker Profile Created Successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                handleError("Failed to save worker: ${e.message}")
            }
    }

    private fun handleError(msg: String) {
        if (_binding != null) {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
            context?.let {
                Toast.makeText(it, "Error: $msg", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
