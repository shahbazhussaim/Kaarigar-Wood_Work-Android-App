package com.kaarigar.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.kaarigar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.KaarigarApp
import com.kaarigar.data.Resource
import com.kaarigar.data.repository.AuthRepository
import com.kaarigar.databinding.FragmentRegisterBinding
import com.kaarigar.ui.ViewModelFactory
import android.util.Log

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
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

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            val role = "CUSTOMER"

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                Log.d("RegisterFragment", "Triggering registration for $email")
                viewModel.register(email, password, name, role, phone)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.authState.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.LOADING -> {
                    Log.d("RegisterFragment", "State: LOADING")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                Resource.Status.SUCCESS -> {
                    Log.d("RegisterFragment", "State: SUCCESS")
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(context, "Registration Success", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_customerHomeFragment)
                }
                Resource.Status.ERROR -> {
                    Log.e("RegisterFragment", "State: ERROR - ${resource.message}")
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
