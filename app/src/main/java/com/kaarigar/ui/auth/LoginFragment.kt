package com.kaarigar.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.KaarigarApp
import com.kaarigar.R
import com.kaarigar.data.Resource
import com.kaarigar.data.repository.AuthRepository
import com.kaarigar.databinding.FragmentLoginBinding
import com.kaarigar.ui.ViewModelFactory
import android.util.Log

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as KaarigarApp
        val db = app.database
        val authRepo = AuthRepository(
            FirebaseAuth.getInstance(),
            FirebaseFirestore.getInstance(),
            db.userDao()
        )
        val factory = ViewModelFactory(authRepo)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.d("LoginFragment", "Triggering login for $email")
                viewModel.login(email, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
           Toast.makeText(context, "Click Received. Navigating...", Toast.LENGTH_SHORT).show()
           try {
               // Use Direct Destination ID to avoid Action lookup issues
               findNavController().navigate(R.id.registerFragment)
           } catch (e: Exception) {
               Log.e("LoginFragment", "Nav Error", e)
               Toast.makeText(context, "Nav Error: ${e.message}", Toast.LENGTH_LONG).show()
           }
        }

        viewModel.authState.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                Resource.Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    val role = resource.data?.role ?: "CUSTOMER"
                    
                    Toast.makeText(context, "Welcome $role", Toast.LENGTH_SHORT).show()
                    
                    when (role) {
                        "ADMIN", "WORKER", "CUSTOMER" -> {
                            if (requireActivity() is AuthActivity) {
                                (requireActivity() as AuthActivity).navigateToRoleActivity(role)
                            } else {
                                // Fallback just in case, though this fragment should be in AuthActivity
                                Toast.makeText(context, "Navigation Warning: Not in AuthActivity", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                             if (requireActivity() is AuthActivity) {
                                (requireActivity() as AuthActivity).navigateToRoleActivity("CUSTOMER")
                            }
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(context, "Login Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
