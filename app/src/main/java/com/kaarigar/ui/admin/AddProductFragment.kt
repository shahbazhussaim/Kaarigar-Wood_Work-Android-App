package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.databinding.FragmentAddProductBinding

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding
        get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val storage = com.google.firebase.storage.FirebaseStorage.getInstance()
    private var imageUri: android.net.Uri? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val getContent =
                registerForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        imageUri = uri
                        binding.ivPreview.setImageURI(uri)
                        binding.ivPreview.visibility = View.VISIBLE
                    }
                }

        binding.btnUploadImage.setOnClickListener { getContent.launch("image/*") }

        binding.btnSaveProduct.setOnClickListener { saveProduct() }
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        val categoryId = binding.cgCategory.checkedChipId
        val category =
                if (categoryId != View.NO_ID) {
                    binding.cgCategory.findViewById<Chip>(categoryId).text.toString()
                } else "Other"

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(context, "Name and Price required", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveProduct.isEnabled = false
        binding.btnSaveProduct.text = "Saving..."
        
        if (imageUri != null) {
            uploadImageAndSave(name, price, description, category)
        } else {
            saveToFirestore(name, price, description, category, "")
        }
    }

    private fun uploadImageAndSave(
            name: String,
            price: String,
            desc: String,
            category: String
    ) {
        val ref = storage.reference.child("products/${System.currentTimeMillis()}.jpg")
        val uploadTask = ref.putFile(imageUri!!)

        binding.btnSaveProduct.text = "Uploading Image..."

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }
        
        urlTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                binding.btnSaveProduct.text = "Saving Data..."
                saveToFirestore(name, price, desc, category, downloadUri.toString())
            } else {
                // FALLBACK: Store without image if Storage fails (e.g. Quota exceeded)
                val error = task.exception?.message ?: "Unknown"
                Toast.makeText(context, "Storage Error ($error). Saving without image.", Toast.LENGTH_LONG).show()
                
                // Use a default placeholder URL (or empty string)
                val placeholderUrl = "https://via.placeholder.com/150" 
                saveToFirestore(name, price, desc, category, placeholderUrl)
            }
        }
    }

    private fun saveToFirestore(
            name: String,
            price: String,
            desc: String,
            category: String,
            imageUrl: String
    ) {
        val product =
                hashMapOf(
                        "name" to name,
                        "price" to "₹ $price",
                        "category" to category,
                        "description" to desc,
                        "imageUrl" to imageUrl,
                        "timestamp" to System.currentTimeMillis()
                )

        db.collection("products")
                .add(product)
                .addOnSuccessListener {
                    Toast.makeText(context, "Product Added Successfully!", Toast.LENGTH_SHORT)
                            .show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    binding.btnSaveProduct.isEnabled = true
                    binding.btnSaveProduct.text = "Save Product"
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
