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

        binding.btnAcceptJob.setOnClickListener {
            acceptJob()
        }

        binding.btnUpdateStatus.setOnClickListener {
            updateStatus("COMPLETED")
        }
    }

    private fun loadRequestDetails(id: String) {
        db.collection("requests").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.tvRequestType.text = "Type: ${doc.getString("type")}"
                    binding.tvRequestDescription.text = "Details: ${doc.getString("description")}"
                    binding.tvPrice.text = "Estimated Budget: ₹ ${doc.get("estimatedPrice") ?: "0.0"}"
                    val status = doc.getString("status") ?: "PENDING"
                    binding.tvCurrentStatus.text = "Status: $status"
                    
                    // Toggle actions based on status
                    when (status.uppercase()) {
                        "PENDING" -> {
                            binding.btnAcceptJob.visibility = View.VISIBLE
                            binding.btnUpdateStatus.visibility = View.GONE
                        }
                        "ACCEPTED" -> {
                            binding.btnAcceptJob.visibility = View.GONE
                            binding.btnUpdateStatus.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.btnAcceptJob.visibility = View.GONE
                            binding.btnUpdateStatus.visibility = View.GONE
                        }
                    }
                    
                    val phone = doc.getString("customerPhone") ?: "N/A"
                    binding.tvCustomerPhone.text = "Call Customer: $phone"
                    
                    val imageUrl = doc.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        com.bumptech.glide.Glide.with(this)
                            .load(imageUrl)
                            .into(binding.ivProjectImage)
                        binding.ivProjectImage.visibility = View.VISIBLE
                    }

                    binding.btnCallCustomer.setOnClickListener {
                        if (phone != "N/A") {
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                            intent.data = android.net.Uri.parse("tel:$phone")
                            startActivity(intent)
                        }
                    }
                } else {
                    Toast.makeText(context, "Request not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun acceptJob() {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(currentUserId).get().addOnSuccessListener { userDoc ->
            val workerName = userDoc.getString("name") ?: "Worker"
            val workerEmail = userDoc.getString("email") ?: ""
            
            val updates = hashMapOf<String, Any>(
                "status" to "ACCEPTED",
                "assignedWorkerId" to currentUserId,
                "assignedWorkerName" to workerName,
                "assignedWorkerEmail" to workerEmail
            )
            
            requestId?.let { id ->
                db.collection("requests").document(id).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Job Accepted!", Toast.LENGTH_SHORT).show()
                        loadRequestDetails(id)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to accept job", Toast.LENGTH_SHORT).show()
                    }
            }
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
