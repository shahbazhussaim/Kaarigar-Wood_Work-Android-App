package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.databinding.FragmentOrderedBinding

class OrderedFragment : Fragment() {

    private var _binding: FragmentOrderedBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadOrders()
    }

    private fun setupRecyclerView() {
        adapter =
                OrderAdapter(mutableListOf()) { orderId, position ->
                    deleteOrder(orderId, position)
                }
        binding.rvOrders.layoutManager = LinearLayoutManager(context)
        binding.rvOrders.adapter = adapter
    }

    private fun deleteOrder(orderId: String, position: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Order")
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("Yes") { _, _ ->
                    db.collection("orders")
                            .document(orderId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Order Deleted", Toast.LENGTH_SHORT).show()
                                adapter.removeItem(position)
                                // Reload or just notify adapter
                                loadOrders()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                                context,
                                                "Failed to delete order",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                }
                .setNegativeButton("No", null)
                .show()
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: "guest_user"

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val orders =
                            documents
                                    .map {
                                        val data = it.data.toMutableMap()
                                        data["id"] = it.id
                                        data
                                    }
                                    .sortedByDescending { it["timestamp"] as? Long ?: 0L }

                    adapter.updateData(orders)
                    binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load orders", Toast.LENGTH_SHORT).show()
                    it.printStackTrace()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
