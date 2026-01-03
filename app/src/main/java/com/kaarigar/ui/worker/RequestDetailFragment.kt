package com.kaarigar.ui.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.databinding.FragmentRequestDetailBinding

class RequestDetailFragment : Fragment() {

    private var _binding: FragmentRequestDetailBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private var requestId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRequestDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        requestId = arguments?.getString("requestId") ?: "demo_id"
        loadRequestDetails(requestId!!)

        binding.btnUpdateStatus.setOnClickListener {
            updateStatus("COMPLETED")
        }
    }

    private fun loadRequestDetails(id: String) {
        db.collection("requests").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.tvRequestType.text = "Type: ${doc.getString("type")}"
                    binding.tvRequestDescription.text = "Description: ${doc.getString("description")}"
                    binding.tvPrice.text = "Price: ₹ ${doc.getDouble("predictedPrice") ?: 0.0}"
                    binding.tvCurrentStatus.text = "Status: ${doc.getString("status")}"
                } else {
                    Toast.makeText(context, "Request not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(newStatus: String) {
        requestId?.let {
            db.collection("requests").document(it)
                .update("status", newStatus)
                .addOnSuccessListener {
                    Toast.makeText(context, "Status Updated to $newStatus", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
