package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kaarigar.R
import com.kaarigar.databinding.FragmentCustomerHomeBinding
import com.kaarigar.data.local.entity.ProductEntity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.kaarigar.ui.auth.AuthActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.kaarigar.KarigarApp

class CustomerHomeFragment : Fragment() {

    private var _binding: FragmentCustomerHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTopBar()
        setupServices()
        setupAIChat()
        setupBottomCTA()
    }

    private fun setupTopBar() {
        // Top Right Icons
        binding.ivCart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cart)
        }
        binding.ivOrders.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_ordered)
        }
        binding.ivRequests.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_requests)
        }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
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

    private fun setupServices() {
        // Service Cards Navigation
        // Assuming we update XML to have IDs for these cards
        // Furniture -> Shop
        binding.cardFurniture.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_shop)
        }
        
        // Maintenance -> Maintenance
        binding.cardMaintenanceService.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_maintenance)
        }
        
        // Custom Order -> Custom Order
        binding.cardCustomService.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_customOrder)
        }

        // Other categories can go to Shop with filter argument if we implement that
        // For now, let's point them to Shop
        binding.cardDoors.setOnClickListener {
             val bundle = Bundle().apply { putString("category", "Artisan Door") }
             findNavController().navigate(R.id.action_home_to_shop, bundle)
        }
        binding.cardKitchen.setOnClickListener {
             val bundle = Bundle().apply { putString("category", "Kitchen") }
             findNavController().navigate(R.id.action_home_to_shop, bundle)
        }
    }
    
    private fun setupAIChat() {
        binding.fabAIChat.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_chat)
        }
    }
    
    private fun setupBottomCTA() {
        binding.btnGetQuote.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_customOrder)
        }
        binding.btnBrowseProduct.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_shop)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
