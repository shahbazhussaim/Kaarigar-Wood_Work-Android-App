package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.databinding.FragmentManageOrdersBinding

class ManageOrdersFragment : Fragment() {

    private var _binding: FragmentManageOrdersBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdminOrderAdapter
    private var allOrders = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadOrders()

        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            filterOrders()
        }
    }

    private fun setupRecyclerView() {
        adapter =
                AdminOrderAdapter(mutableListOf()) { orderId, view ->
                    showStatusMenu(orderId, view)
                }
        binding.rvOrders.layoutManager = LinearLayoutManager(context)
        binding.rvOrders.adapter = adapter
    }

    private fun showStatusMenu(orderId: String, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Mark as PENDING")
        popup.menu.add("Mark as PROCESSING")
        popup.menu.add("Mark as COMPLETED")
        popup.menu.add("Mark as CANCELLED")

        popup.setOnMenuItemClickListener { item ->
            val status = item.title.toString().replace("Mark as ", "")
            updateOrderStatus(orderId, status)
            true
        }
        popup.show()
    }

    private fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId).update("status", status).addOnSuccessListener {
            Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show()
            loadOrders() // Reload
        }
    }

    private fun loadOrders() {
        db.collection("orders").get().addOnSuccessListener { documents ->
            allOrders.clear()
            for (doc in documents) {
                val data = doc.data.toMutableMap()
                data["id"] = doc.id
                allOrders.add(data)
            }
            filterOrders()
        }
    }

    private fun filterOrders() {
        val selectedChipId = binding.chipGroupStatus.checkedChipId
        val status =
                when (selectedChipId) {
                    R.id.chipPending -> "PENDING"
                    R.id.chipProcessing -> "PROCESSING"
                    R.id.chipCompleted -> "COMPLETED"
                    else -> "All"
                }

        val filtered =
                if (status == "All") allOrders else allOrders.filter { it["status"] == status }
        adapter.updateData(filtered)
        
        binding.tvEmptyOrders.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
