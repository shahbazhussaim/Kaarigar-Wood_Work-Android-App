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
        
        // Check for Edit Mode
        val editId = arguments?.getString("productId")
        if (editId != null) {
            loadProductForEdit(editId)
        }
    }

    private fun loadProductForEdit(id: String) {
        binding.btnSaveProduct.text = "Update Product"
        db.collection("products").document(id).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                binding.etProductName.setText(doc.getString("name"))
                val rawPrice = doc.getString("price")?.replace("₹", "")?.trim() ?: ""
                binding.etPrice.setText(rawPrice)
                binding.etDescription.setText(doc.getString("description"))
                // Categories
                val category = doc.getString("category")
                for (i in 0 until binding.cgCategory.childCount) {
                    val chip = binding.cgCategory.getChildAt(i) as Chip
                    if (chip.text == category) {
                        chip.isChecked = true
                        break
                    }
                }
            }
        }
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
                // FALLBACK: Store with a local resource name if Storage fails
                val error = task.exception?.message ?: "Unknown"
                Toast.makeText(context, "Storage Error ($error). Using local image fallback.", Toast.LENGTH_LONG).show()
                
                // Map category to a random local image name
                val localImageName = getLocalImageForCategory(category)
                saveToFirestore(name, price, desc, category, "local://$localImageName")
            }
        }
    }

    private fun getLocalImageForCategory(category: String): String {
        val randomNum = (1..5).random()
        val cleanCategory = category.lowercase().replace(" ", "_")
        
        // This will produce strings like "door_1", "kitchen_2", etc.
        // If the user hasn't added these yet, it will fallback to wood_cabinet in the Adapter.
        return "${cleanCategory}_$randomNum"
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

        val editId = arguments?.getString("productId")
        val task = if (editId != null) {
            db.collection("products").document(editId).set(product)
        } else {
            db.collection("products").add(product)
        }

        task.addOnSuccessListener {
            val msg = if (editId != null) "Product Updated!" else "Product Added Successfully!"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            
            if (editId != null) {
                findNavController().popBackStack()
            } else {
                clearForm()
            }
        }
        .addOnFailureListener { e ->
            binding.btnSaveProduct.isEnabled = true
            binding.btnSaveProduct.text = if (editId != null) "Update Product" else "Save Product"
            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        binding.etProductName.text?.clear()
        binding.etPrice.text?.clear()
        binding.etDescription.text?.clear()
        binding.cgCategory.clearCheck()
        binding.ivPreview.visibility = View.GONE
        imageUri = null
        binding.btnSaveProduct.isEnabled = true
        binding.btnSaveProduct.text = "Save Product"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
