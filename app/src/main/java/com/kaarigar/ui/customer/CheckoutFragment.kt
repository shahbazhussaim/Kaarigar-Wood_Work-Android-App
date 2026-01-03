package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kaarigar.R
import com.kaarigar.databinding.FragmentCheckoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var totalAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        totalAmount = arguments?.getDouble("totalAmount") ?: 0.0
        
        binding.btnPlaceOrder.setOnClickListener {
            val address = binding.etAddress.text.toString()
            val phone = binding.etPhone.text.toString()
            val date = binding.etDate.text.toString()

            if (validate(address, phone, date)) {
                placeOrder(address, phone, date)
            }
        }
    }

    private fun validate(address: String, phone: String, date: String): Boolean {
        if (address.isBlank()) {
            binding.etAddress.error = "Address required"
            return false
        }
        if (phone.length < 10) {
            binding.etPhone.error = "Valid Phone required"
            return false
        }
        if (date.isBlank()) {
             binding.etDate.error = "Date required"
             return false
        }
        return true
    }

    private var isPlacingOrder = false

    private fun placeOrder(address: String, phone: String, date: String) {
        if (isPlacingOrder) return
        isPlacingOrder = true
        binding.btnPlaceOrder.isEnabled = false
        
        val userId = auth.currentUser?.uid ?: "guest_user"
        
        // 1. Get Cart Items
        db.collection("carts").document(userId).collection("items").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context, "Cart is empty!", Toast.LENGTH_SHORT).show()
                    isPlacingOrder = false
                    binding.btnPlaceOrder.isEnabled = true
                    return@addOnSuccessListener
                }

                val items = documents.map { it.data }
                
                // 2. Create Order Object
                val order = hashMapOf(
                    "userId" to userId,
                    "items" to items,
                    "totalAmount" to totalAmount,
                    "address" to address,
                    "phone" to phone,
                    "deliveryDate" to date,
                    "paymentMethod" to if (binding.rbCOD.isChecked) "COD" else "Online",
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )

                // 3. Save to Orders
                db.collection("orders").add(order)
                    .addOnSuccessListener {
                        // 4. Clear Cart (Best effort) - Only navigate after clearing
                        clearCart(userId, documents)
                    }
                    .addOnFailureListener {
                         isPlacingOrder = false
                         binding.btnPlaceOrder.isEnabled = true
                         Toast.makeText(context, "Failed to place order", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                isPlacingOrder = false
                binding.btnPlaceOrder.isEnabled = true
                Toast.makeText(context, "Error checking cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearCart(userId: String, documents: com.google.firebase.firestore.QuerySnapshot) {
        val batch = db.batch()
        for (doc in documents) {
            batch.delete(doc.reference)
        }
        batch.commit().addOnSuccessListener {
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
            // Navigate to Home or Orders - ONE navigation call
            findNavController().navigate(R.id.action_checkout_to_ordered)
        }
        .addOnFailureListener {
             // Even if clear cart fails, order was placed.
             Toast.makeText(context, "Order Placed (Cart clear failed)", Toast.LENGTH_LONG).show()
             findNavController().navigate(R.id.action_checkout_to_ordered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
