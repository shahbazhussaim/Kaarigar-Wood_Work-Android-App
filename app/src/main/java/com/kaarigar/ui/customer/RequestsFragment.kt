package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaarigar.databinding.FragmentRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Reusing OrderAdapter styled items for now, or creating a RequestAdapter.
// For speed, let's create a RequestAdapter.

class RequestsFragment : Fragment() {

    private var _binding: FragmentRequestsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: RequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = RequestAdapter(mutableListOf())
        binding.rvRequests.layoutManager = LinearLayoutManager(context)
        binding.rvRequests.adapter = adapter
        
        loadRequests()
    }

    private fun loadRequests() {
        val userId = auth.currentUser?.uid ?: "guest_user"
        
        db.collection("requests")
            .whereEqualTo("userId", userId)
            // .orderBy("timestamp", Query.Direction.DESCENDING) // This might need an index, removed for safety
            .get()
            .addOnSuccessListener { documents ->
                val requests = documents.map { 
                    val data = it.data.toMutableMap()
                    data["id"] = it.id
                    data
                }
                adapter.updateData(requests)
                binding.tvEmpty.visibility = if (requests.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load requests", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
