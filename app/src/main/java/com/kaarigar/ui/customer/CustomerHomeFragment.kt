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
            // Navigate to shop with arg? For now just shop.
             findNavController().navigate(R.id.action_home_to_shop)
        }
        binding.cardKitchen.setOnClickListener {
             findNavController().navigate(R.id.action_home_to_shop)
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
