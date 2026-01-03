package com.kaarigar.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.databinding.FragmentMaintenanceRequestBinding
import com.kaarigar.ui.gemini.GeminiViewModel

class MaintenanceFragment : Fragment() {

    private var _binding: FragmentMaintenanceRequestBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var geminiViewModel: GeminiViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory =
                com.kaarigar.ui.ViewModelFactory(
                        geminiRepository = com.kaarigar.data.repository.GeminiRepository()
                )
        geminiViewModel =
                androidx.lifecycle.ViewModelProvider(this, factory)[GeminiViewModel::class.java]

        binding.btnSubmit.setOnClickListener {
            val description = binding.etIssue.text.toString().trim()
            if (description.isNotEmpty()) {
                submitRequest(description)
            } else {
                Toast.makeText(context, "Please describe the issue", Toast.LENGTH_SHORT).show()
            }
        }

        val getContent =
                registerForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        binding.ivPreview.setImageURI(uri)
                        binding.ivPreview.visibility = View.VISIBLE
                    }
                }

        binding.btnUploadImage.setOnClickListener { getContent.launch("image/*") }

        binding.btnPredictPrice.setOnClickListener {
            val description = binding.etIssue.text.toString().trim()
            if (description.isNotEmpty()) {
                // Mock or Real Gemini Call - Navigate to Result
                val bundle = Bundle().apply { putString("description", description) }
                findNavController().navigate(R.id.action_maintenance_to_priceResult, bundle)
            } else {
                Toast.makeText(context, "Please describe the issue first", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun submitRequest(description: String) {
        val userId = auth.currentUser?.uid ?: "guest_user"
        val request =
                hashMapOf(
                        "userId" to userId,
                        "type" to "MAINTENANCE",
                        "description" to description,
                        "status" to "PENDING",
                        "timestamp" to System.currentTimeMillis()
                )

        db.collection("requests").add(request).addOnFailureListener { e -> e.printStackTrace() }

        // Optimistic UI
        Toast.makeText(context, "Request Submitted (Syncing...)", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
