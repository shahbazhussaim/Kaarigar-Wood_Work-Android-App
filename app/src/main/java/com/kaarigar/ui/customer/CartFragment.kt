package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaarigar.R
import com.kaarigar.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: CartAdapter
    private var cartItems = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadCartItems()
        
        binding.btnCheckout.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val total = calculateTotal()
                val bundle = Bundle().apply {
                    putDouble("totalAmount", total)
                }
                findNavController().navigate(R.id.action_cart_to_checkout, bundle)
            } else {
                Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(cartItems, 
            onRemove = { id, pos -> removeItem(id, pos) },
            onUpdateQty = { id, qty, pos -> updateQuantity(id, qty, pos) }
        )
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = adapter
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("carts").document(userId).collection("items")
            .get()
            .addOnSuccessListener { documents ->
                cartItems.clear()
                for (doc in documents) {
                    val data = doc.data.toMutableMap()
                    data["id"] = doc.id
                    cartItems.add(data)
                }
                adapter.notifyDataSetChanged()
                updateSummary()
                
                binding.tvEmptyCart.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load cart", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateQuantity(id: String, qty: Int, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts").document(userId).collection("items").document(id)
            .update("quantity", qty)
            .addOnSuccessListener {
                loadCartItems() // Reload to refresh UI safely
            }
    }

    private fun removeItem(id: String, position: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts").document(userId).collection("items").document(id)
            .delete()
            .addOnSuccessListener {
                cartItems.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateSummary()
                binding.tvEmptyCart.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun updateSummary() {
        binding.tvItemCount.text = "${cartItems.size} items"
        val total = calculateTotal()
        binding.tvTotalPrice.text = "₹ $total"
    }

    private fun calculateTotal(): Double {
        var total = 0.0
        for (item in cartItems) {
            val priceStr = (item["price"] as? String) ?: "0"
            // Extracts all digits and optional decimal point
            val priceClean = priceStr.replace(Regex("[^0-9.]"), "")
            val price = priceClean.toDoubleOrNull() ?: 0.0
            val qty = (item["quantity"] as? Long)?.toInt() ?: 1
            total += price * qty
        }
        return total
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
