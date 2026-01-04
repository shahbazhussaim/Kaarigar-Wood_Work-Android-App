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
    private var totalAmount = 0.0f
    private var orderDescription: String? = null
    private var isCustomOrder = false
    private var directProductId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        arguments?.let {
            totalAmount = it.getFloat("totalAmount", 0.0f)
            orderDescription = it.getString("description")
            isCustomOrder = it.getBoolean("isCustom", false)
            directProductId = it.getString("productId")
        }
        
        binding.tvTotalPrice.text = String.format("Total: ₹%.2f", totalAmount)
        if (!orderDescription.isNullOrEmpty()) {
             binding.tvOrderSummary.text = orderDescription
             binding.tvOrderSummary.visibility = View.VISIBLE
        }
        
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        binding.etDate.isFocusable = false // Prevent keyboard

        binding.btnPlaceOrder.setOnClickListener {
            val address = binding.etAddress.text.toString()
            val phone = binding.etPhone.text.toString()
            val date = binding.etDate.text.toString()

            if (validate(address, phone, date)) {
                placeOrder(address, phone, date)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val dpd = android.app.DatePickerDialog(requireContext(), { _, y, m, d ->
            val formattedDate = String.format("%02d/%02d/%04d", d, m + 1, y)
            binding.etDate.setText(formattedDate)
        }, year, month, day)
        
        dpd.datePicker.minDate = System.currentTimeMillis() // Only future dates
        dpd.show()
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
        if (isCustomOrder || directProductId != null) {
            // Direct order - skip cart clearing
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_checkout_to_ordered)
            return
        }

        val batch = db.batch()
        for (doc in documents) {
            batch.delete(doc.reference)
        }
        batch.commit().addOnSuccessListener {
            Toast.makeText(context, "Order Placed Successfully!", Toast.LENGTH_LONG).show()
            // Navigate back to the originating flow
            if (isCustomOrder) {
                findNavController().popBackStack(R.id.customOrderFragment, false)
            } else if (directProductId != null) {
                findNavController().popBackStack(R.id.shopFragment, false)
            } else {
                findNavController().navigate(R.id.action_checkout_to_ordered)
            }
        }
        .addOnFailureListener {
             Toast.makeText(context, "Order Placed (Cart clear failed)", Toast.LENGTH_LONG).show()
             findNavController().navigate(R.id.action_checkout_to_ordered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
