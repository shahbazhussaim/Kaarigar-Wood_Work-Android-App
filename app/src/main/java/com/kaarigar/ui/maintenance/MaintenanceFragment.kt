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
    private val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
    private lateinit var geminiViewModel: GeminiViewModel
    private var selectedImageUri: android.net.Uri? = null

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
            val phone = binding.etPhone.text.toString().trim()
            if (description.isNotEmpty() && phone.isNotEmpty()) {
                submitRequest(description, phone)
            } else {
                Toast.makeText(context, "Description and Phone required", Toast.LENGTH_SHORT).show()
            }
        }

        val getContent =
                registerForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        selectedImageUri = uri
                        binding.ivPreview.setImageURI(uri)
                        binding.ivPreview.visibility = View.VISIBLE
                    }
                }

        binding.btnUploadImage.setOnClickListener { getContent.launch("image/*") }

        binding.btnPredictPrice.setOnClickListener {
            val description = binding.etIssue.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            if (description.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString("description", description)
                    putString("phone", phone)
                    selectedImageUri?.let { putString("imageUri", it.toString()) }
                }
                findNavController().navigate(R.id.action_maintenance_to_priceResult, bundle)
            } else {
                Toast.makeText(context, "Please describe the issue first", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun submitRequest(description: String, phone: String) {
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.text = "Submitting..."
        
        if (selectedImageUri != null) {
            uploadImageAndSave(description, phone)
        } else {
            saveToFirestore(description, phone, "")
        }
    }

    private fun uploadImageAndSave(description: String, phone: String) {
        val ref = storage.reference.child("maintenance/${System.currentTimeMillis()}.jpg")
        ref.putFile(selectedImageUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                ref.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveToFirestore(description, phone, task.result.toString())
                } else {
                    saveToFirestore(description, phone, "") // Fallback
                }
            }
    }

    private fun saveToFirestore(description: String, phone: String, imageUrl: String) {
        val userId = auth.currentUser?.uid ?: "guest_user"
        val request = hashMapOf(
            "userId" to userId,
            "type" to "MAINTENANCE",
            "description" to description,
            "customerPhone" to phone,
            "imageUrl" to imageUrl,
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("requests").add(request)
            .addOnSuccessListener {
                if (_binding != null) {
                    Toast.makeText(context, "Request Submitted Successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Request Expert Care"
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
