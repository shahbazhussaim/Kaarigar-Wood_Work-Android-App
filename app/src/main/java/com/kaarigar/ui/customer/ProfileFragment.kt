package com.kaarigar.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kaarigar.KaarigarApp
import com.kaarigar.R
import com.kaarigar.databinding.FragmentProfileBinding
import com.kaarigar.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadUserProfile()
        
        binding.cardOrders.setOnClickListener {
             findNavController().navigate(R.id.action_profile_to_ordered)
        }
        
        binding.cardRequests.setOnClickListener {
             findNavController().navigate(R.id.action_profile_to_requests)
        }
        
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val database = (requireActivity().application as KaarigarApp).database
            CoroutineScope(Dispatchers.IO).launch {
                database.userDao().clearUsers()
            }
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email
        
        binding.tvUserEmail.text = userEmail
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    binding.tvUserName.text = name.replaceFirstChar { it.uppercase() }
                }
            }
            .addOnFailureListener {
                binding.tvUserName.text = "Error Loading Name"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
