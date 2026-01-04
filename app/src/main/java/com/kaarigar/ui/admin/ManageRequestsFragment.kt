package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.databinding.FragmentManageRequestsBinding

class ManageRequestsFragment : Fragment() {

    private var _binding: FragmentManageRequestsBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdminRequestAdapter
    private var allRequests = mutableListOf<Map<String, Any>>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadRequests()

        binding.chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            filterRequests()
        }
        // Ensure 'All' is default
        binding.chipAll.isChecked = true
    }

    private fun setupRecyclerView() {
        adapter = AdminRequestAdapter(mutableListOf()) { id, view -> showActionDialog(id, view) }
        binding.rvRequests.layoutManager = LinearLayoutManager(context)
        binding.rvRequests.adapter = adapter
    }

    private fun showActionDialog(id: String, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add("Mark COMPLETED")
        popup.menu.add("Update Quote")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Mark COMPLETED" -> updateStatus(id, "COMPLETED")
                "Update Quote" -> showQuoteDialog(id)
            }
            true
        }
        popup.show()
    }

    private fun updateStatus(id: String, status: String) {
        db.collection("requests").document(id).update("status", status).addOnSuccessListener {
            Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show()
            loadRequests()
        }
    }

    private fun showQuoteDialog(id: String) {
        val editText = EditText(context)
        editText.hint = "Enter Final Quote Amount"

        AlertDialog.Builder(requireContext())
                .setTitle("Update Quote")
                .setView(editText)
                .setPositiveButton("Update") { _, _ ->
                    val amount = editText.text.toString()
                    if (amount.isNotEmpty()) {
                        db.collection("requests")
                                .document(id)
                                .update("estimatedPrice", amount)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Quote Updated", Toast.LENGTH_SHORT)
                                            .show()
                                    loadRequests()
                                }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun loadRequests() {
        db.collection("requests").orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
            allRequests.clear()
            for (doc in documents) {
                val data = doc.data.toMutableMap()
                data["id"] = doc.id
                allRequests.add(data)
            }
            updateStats()
            filterRequests()
        }
    }

    private fun updateStats() {
        val total = allRequests.size
        val pending = allRequests.count { (it["status"] as? String) == "PENDING" }
        val accepted = allRequests.count { (it["status"] as? String) == "ACCEPTED" }
        
        binding.tvTotalRequests.text = "$total"
        binding.tvPendingRequests.text = "$pending"
        binding.tvAcceptedRequests.text = "$accepted"
    }

    private fun filterRequests() {
        val selectedChipId = binding.chipGroupStatus.checkedChipId
        val status =
                when (selectedChipId) {
                    R.id.chipPending -> "PENDING"
                    R.id.chipAccepted -> "ACCEPTED"
                    R.id.chipCompleted -> "COMPLETED"
                    else -> "All"
                }

        val filtered =
                if (status == "All") allRequests else allRequests.filter { it["status"] == status }
        adapter.updateData(filtered)
        
        binding.tvEmptyRequests.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
