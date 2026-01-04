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
import com.kaarigar.data.Resource
import com.kaarigar.databinding.FragmentPriceResultBinding
import com.kaarigar.ui.gemini.GeminiViewModel

class PriceResultFragment : Fragment() {

    private var _binding: FragmentPriceResultBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var geminiViewModel: GeminiViewModel
    private var description = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPriceResultBinding.inflate(inflater, container, false)
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

        description = arguments?.getString("description") ?: "General maintenance"
        val imageUriString = arguments?.getString("imageUri")
        binding.tvReason.text = "Analyzing: $description..."

        // Trigger AI Prediction with Image if available
        if (imageUriString != null) {
            val uri = android.net.Uri.parse(imageUriString)
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                geminiViewModel.predictPrice("Maintenance", description, bitmap)
            } catch (e: Exception) {
                geminiViewModel.predictPrice("Maintenance", description)
            }
        } else {
            geminiViewModel.predictPrice("Maintenance", description)
        }

        geminiViewModel.pricePrediction.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.LOADING -> {
                    binding.tvPriceRange.text = "Calculating..."
                    binding.tvReason.text = "Consulting AI Expert..."
                }
                Resource.Status.SUCCESS -> {
                    val raw = resource.data ?: ""
                    // Try parsing JSON or show raw
                    try {
                        // regex to find { ... } block
                        val pattern = java.util.regex.Pattern.compile("\\{.*\\}", java.util.regex.Pattern.DOTALL)
                        val matcher = pattern.matcher(raw)
                        
                        if (matcher.find()) {
                            val jsonString = matcher.group(0)
                            val jsonObject = org.json.JSONObject(jsonString)
                            val price = jsonObject.optString("price", "N/A")
                            val reason = jsonObject.optString("reason", "")
                            
                            // Remove currency symbol if double present to avoid ₹ ₹ 1500
                            val cleanPrice = price.replace("₹", "").trim()
                            
                            binding.tvPriceRange.text = "₹ $cleanPrice"
                            binding.tvReason.text = reason
                        } else {
                            // Fallback if no JSON found
                             binding.tvPriceRange.text = "Estimate in Description"
                             binding.tvReason.text = raw
                        }
                    } catch (e: Exception) {
                        binding.tvPriceRange.text = "See Details"
                        binding.tvReason.text = raw
                    }
                }
                Resource.Status.ERROR -> {
                    binding.tvPriceRange.text = "Error"
                    binding.tvReason.text = "Could not estimate: ${resource.message}"
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            val phone = arguments?.getString("phone") ?: ""
            submitRequest(description, binding.tvPriceRange.text.toString(), phone)
        }

        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
    }

    private fun submitRequest(description: String, price: String, phone: String) {
        val userId = auth.currentUser?.uid ?: "guest_user"
        val request =
                hashMapOf(
                        "userId" to userId,
                        "type" to "MAINTENANCE",
                        "description" to description,
                        "customerPhone" to phone,
                        "estimatedPrice" to price,
                        "status" to "PENDING",
                        "timestamp" to System.currentTimeMillis()
                )

        db.collection("requests")
                .add(request)
                .addOnSuccessListener {
                    Toast.makeText(context, "Request Confirmed!", Toast.LENGTH_SHORT).show()
                    binding.btnConfirm.isEnabled = true
                    findNavController().navigate(R.id.action_priceResult_to_requests)
                }
                .addOnFailureListener { e ->
                    binding.btnConfirm.isEnabled = true
                    Toast.makeText(context, "Error submitting: ${e.message}", Toast.LENGTH_LONG).show()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
