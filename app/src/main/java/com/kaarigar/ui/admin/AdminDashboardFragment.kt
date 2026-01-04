package com.kaarigar.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kaarigar.KarigarApp
import com.kaarigar.R
import com.kaarigar.databinding.FragmentAdminDashboardBinding
import com.kaarigar.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadStats()
        
        binding.btnAddProduct.setOnClickListener {
             findNavController().navigate(R.id.action_adminDashboard_to_addProduct)
        }
        
        binding.btnManageOrders.setOnClickListener {
             findNavController().navigate(R.id.action_adminDashboard_to_manageOrders)
        }
        
        binding.btnManageWorkers.setOnClickListener {
             findNavController().navigate(R.id.action_adminDashboard_to_manageWorkers)
        }
        
        binding.btnViewRequests.setOnClickListener {
             findNavController().navigate(R.id.action_adminDashboard_to_manageRequests)
        }

        binding.btnViewCategories.setOnClickListener {
             findNavController().navigate(R.id.adminCategories)
        }

        binding.btnManageInventory.setOnClickListener {
            findNavController().navigate(R.id.manageProducts)
        }
        
        binding.btnLogout.setOnClickListener {
             auth.signOut()
             val database = (requireActivity().application as KarigarApp).database
             CoroutineScope(Dispatchers.IO).launch {
                 database.userDao().clearUsers()
             }
             val intent = Intent(requireContext(), AuthActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
             requireActivity().finish()
        }
    }

    private fun loadStats() {
        // Mock or Real count
        db.collection("orders").whereEqualTo("status", "PENDING").get()
            .addOnSuccessListener { 
                binding.tvOrderCount.text = "${it.size()}"
            }
            
        db.collection("requests").whereEqualTo("status", "PENDING").get()
            .addOnSuccessListener {
                binding.tvRequestCount.text = "${it.size()}"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
