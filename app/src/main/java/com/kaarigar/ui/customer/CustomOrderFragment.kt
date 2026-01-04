package com.kaarigar.ui.customer

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
import com.kaarigar.databinding.FragmentCustomOrderBinding
import com.kaarigar.ui.gemini.GeminiViewModel

class CustomOrderFragment : Fragment() {

    private var _binding: FragmentCustomOrderBinding? = null
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
        _binding = FragmentCustomOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup ViewModel
        val factory =
                com.kaarigar.ui.ViewModelFactory(
                        geminiRepository = com.kaarigar.data.repository.GeminiRepository()
                )
        geminiViewModel =
                androidx.lifecycle.ViewModelProvider(this, factory)[GeminiViewModel::class.java]

        // Setup Dropdowns
        val materials = listOf("Teak Wood", "Sheesham", "Oak", "Plywood", "MDF", "Other")
        val finishes = listOf("Matte", "Glossy", "Duco Paint", "Varnish", "Natural Polish")

        val materialAdapter =
                android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        materials
                )
        binding.actvMaterial.setAdapter(materialAdapter)

        val finishAdapter =
                android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        finishes
                )
        binding.actvFinish.setAdapter(finishAdapter)

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

        binding.btnSubmit.setOnClickListener {
            val categoryId = binding.cgCategory.checkedChipId
            val category =
                    if (categoryId != View.NO_ID) {
                        view.findViewById<com.google.android.material.chip.Chip>(categoryId)
                                .text
                                .toString()
                    } else ""

            val w = binding.etWidth.text.toString()
            val h = binding.etHeight.text.toString()
            val d = binding.etDepth.text.toString()
            val material = binding.actvMaterial.text.toString()
            val finish = binding.actvFinish.text.toString()
            val details = binding.etRequirement.text.toString()

            if (category.isNotEmpty() &&
                            w.isNotEmpty() &&
                            h.isNotEmpty() &&
                            material != "Wood Essence"
            ) {
                val fullReq =
                        "Category: $category\nSize: $w x $h x $d inches\nMaterial: $material\nFinish: $finish\nDetails: $details"
                submitCustomOrder(fullReq)
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        binding.btnPredictPrice.setOnClickListener {
            val categoryId = binding.cgCategory.checkedChipId
            val category =
                    if (categoryId != View.NO_ID) {
                        view.findViewById<com.google.android.material.chip.Chip>(categoryId)
                                .text
                                .toString()
                    } else "Custom Furniture"

            val w = binding.etWidth.text.toString()
            val h = binding.etHeight.text.toString()
            val d = binding.etDepth.text.toString()
            val material = binding.actvMaterial.text.toString()
            val details = binding.etRequirement.text.toString()

            val description = "Size: $w x $h x $d inches. Material: $material. $details"

            if (w.isNotEmpty()) {
                val bitmap = if (binding.ivPreview.visibility == View.VISIBLE) {
                    (binding.ivPreview.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                } else null
                
                geminiViewModel.predictPrice(category, description, bitmap)
                Toast.makeText(context, "AI is analyzing your vision...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter dimensions first", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Observe AI price prediction
        geminiViewModel.pricePrediction.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    val raw = resource.data ?: ""
                    try {
                        // Sanitize raw string (remove markdown backticks if present)
                        val cleanJson = raw.replace("```json", "").replace("```", "").trim()

                        // Attempt to parse JSON if it looks like JSON
                        if (cleanJson.startsWith("{")) {
                            val jsonObject = org.json.JSONObject(cleanJson)
                            val price = jsonObject.optString("price", "N/A")
                            val reason = jsonObject.optString("reason", "")
                            
                            val cleanPrice = price.replace("₹", "").trim()

                            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("AI Price Estimate")
                                    .setMessage("Estimated Price: ₹$cleanPrice\n\nAnalysis: $reason")
                                    .setPositiveButton("Place Order") { _, _ ->
                                         val categoryId = binding.cgCategory.checkedChipId
                                         val category = if (categoryId != View.NO_ID) view.findViewById<com.google.android.material.chip.Chip>(categoryId).text.toString() else "Custom"
                                         val details = "Size: ${binding.etWidth.text}x${binding.etHeight.text}x${binding.etDepth.text}\nMaterial: ${binding.actvMaterial.text}\nDetails: ${binding.etRequirement.text}"
                                         
                                         // Submit with price
                                         submitCustomOrder(details, cleanPrice)
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                        } else {
                            // Fallback for raw text
                             androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle("AI Estimate")
                                    .setMessage(raw)
                                    .setPositiveButton("OK", null)
                                    .show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Estimate: $raw", Toast.LENGTH_LONG).show()
                    }
                }
                Resource.Status.ERROR -> {
                    Toast.makeText(
                                    context,
                                    "Prediction Error: ${resource.message}",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
                Resource.Status.LOADING -> {
                    // Optional: show progress indicator
                }
            }
        }
    }

    private fun String.neq(other: String) = this != other

    private fun submitCustomOrder(requirements: String, price: String? = null) {
        val userId = auth.currentUser?.uid ?: "guest_user"

        val order =
                hashMapOf(
                        "userId" to userId,
                        "type" to "CUSTOM",
                        "requirements" to requirements,
                        "status" to "PENDING",
                        "estimatedPrice" to (price ?: "Pending Quote"),
                        "timestamp" to System.currentTimeMillis()
                )

        binding.btnSubmit.isEnabled = false
        db.collection("orders")
                .add(order)
                .addOnSuccessListener {
                    Toast.makeText(context, "Order details saved. Please complete checkout.", Toast.LENGTH_SHORT).show()
                    
                    val bundle = Bundle().apply {
                        putFloat("totalAmount", price?.toFloatOrNull() ?: 0.0f)
                        putString("description", requirements)
                        putBoolean("isCustom", true)
                    }
                    findNavController().navigate(R.id.action_custom_to_checkout, bundle)
                }
                .addOnFailureListener { e ->
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
