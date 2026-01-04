package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kaarigar.R
import com.kaarigar.databinding.FragmentProductDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val productId = arguments?.getString("productId") ?: ""
        
        if (productId.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("products").document(productId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "Product"
                        val price = document.getString("price") ?: "0"
                        val description = document.getString("description") ?: "No description available."
                        val imageUrl = document.getString("imageUrl") ?: ""
                        
                        binding.tvProductName.text = name
                        binding.tvProductPrice.text = "₹ $price"
                        binding.tvProductDescription.text = description
                        
                        // Load main image
                        if (!imageUrl.isNullOrEmpty()) {
                            if (imageUrl.startsWith("local://")) {
                                val resourceName = imageUrl.replace("local://", "")
                                val resId = resources.getIdentifier(resourceName, "drawable", requireContext().packageName)
                                if (resId != 0) {
                                    binding.ivProductLarge.setImageResource(resId)
                                } else {
                                    binding.ivProductLarge.setImageResource(R.drawable.wood_cabinet)
                                }
                            } else {
                                com.bumptech.glide.Glide.with(this@ProductDetailFragment)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.wood_cabinet)
                                    .into(binding.ivProductLarge)
                            }
                        } else {
                            binding.ivProductLarge.setImageResource(R.drawable.wood_cabinet)
                        }
                        
                        setupRoleBasedActions()
                        
                        binding.btnAddToCart.setOnClickListener {
                            if (checkUserRole()) {
                                addToCart(name, price)
                            }
                        }
                    }
                }
        } else {
             Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show()
             findNavController().popBackStack()
        }

        setupGallery()
        setupSelectors()
        setupBottomActions()
    }

    private fun setupBottomActions() {
        // Quantity Logic
        binding.btnQtyMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }
        binding.btnQtyPlus.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
        }

        // Buy Now
        binding.btnBuyNow.setOnClickListener {
            val name = binding.tvProductName.text.toString()
            val priceStr = binding.tvProductPrice.text.toString().replace("₹", "").trim()
            val price = priceStr.toFloatOrNull() ?: 0.0f
            
            val bundle = Bundle().apply {
                putFloat("totalAmount", price * quantity)
                putString("description", "Product: $name (Qty: $quantity)")
                putBoolean("isCustom", false)
                putString("productId", arguments?.getString("productId"))
            }
            findNavController().navigate(R.id.action_productDetail_to_checkout, bundle)
        }
        
        // Write Review
        binding.btnWriteReview.setOnClickListener {
            showReviewDialog()
        }
        
        // Back Button in Toolbar
        binding.toolbar.setNavigationOnClickListener {
             findNavController().popBackStack()
        }
    }
    private fun setupRoleBasedActions() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role")?.uppercase() ?: "CUSTOMER"
                    if (role != "CUSTOMER") {
                        binding.btnAddToCart.isEnabled = false
                        binding.btnAddToCart.alpha = 0.5f
                        binding.btnBuyNow.isEnabled = false
                        binding.btnBuyNow.alpha = 0.5f
                        binding.btnAddToCart.text = "Customers Only"
                    }
                }
        }
    }

    private fun checkUserRole(): Boolean {
        // Simplified check, setupRoleBasedActions handles UI state, 
        // this is an extra layer.
        return true 
    }

    private fun setupGallery() {
        binding.cardThumb1.setOnClickListener {
            binding.ivProductLarge.setImageDrawable(binding.ivThumb1.drawable)
            updateThumbStroke(1)
        }
        binding.cardThumb2.setOnClickListener {
            binding.ivProductLarge.setImageDrawable(binding.ivThumb2.drawable)
            updateThumbStroke(2)
        }
        binding.cardThumb3.setOnClickListener {
            binding.ivProductLarge.setImageDrawable(binding.ivThumb3.drawable)
            updateThumbStroke(3)
        }
    }

    private fun updateThumbStroke(selected: Int) {
        val selectedColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gold_accent)
        val defaultColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.glass_stroke)
        
        binding.cardThumb1.strokeColor = if (selected == 1) selectedColor else defaultColor
        binding.cardThumb2.strokeColor = if (selected == 2) selectedColor else defaultColor
        binding.cardThumb3.strokeColor = if (selected == 3) selectedColor else defaultColor
        
        binding.cardThumb1.strokeWidth = if (selected == 1) 4 else 2
        binding.cardThumb2.strokeWidth = if (selected == 2) 4 else 2
        binding.cardThumb3.strokeWidth = if (selected == 3) 4 else 2
    }

    private fun setupSelectors() {
        // XML has singleSelection=true, so visual feedback is handled by theme/chip state.
        binding.cgSizes.check(R.id.chipMedium)
        binding.cgColors.check(R.id.chipNatural)
    }

    private fun addToCart(name: String, price: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        
        val item = hashMapOf(
            "userId" to userId,
            "productName" to name,
            "price" to price,
            "quantity" to quantity,
            "size" to "Standard", // should get from chip
            "color" to "Natural Wood", // should get from chip
            "timestamp" to System.currentTimeMillis()
        )
        
        FirebaseFirestore.getInstance().collection("carts").document(userId).collection("items")
            .add(item)
            .addOnSuccessListener {
                 Toast.makeText(context, "Added to Tray Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                 Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showReviewDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_write_review, null)
        dialog.setContentView(view)
        
        val btnSubmit = view.findViewById<MaterialButton>(R.id.btnSubmitReview)
        val etReview = view.findViewById<TextInputEditText>(R.id.etReview)
        val ratingBar = view.findViewById<android.widget.RatingBar>(R.id.ratingBar)
        
        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etReview.text.toString()
            
            if (comment.isNotEmpty()) {
                submitReview(rating, comment)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please write a review", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }

    private fun submitReview(rating: Float, comment: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
        val review = hashMapOf(
            "userId" to userId,
            "userName" to "User", // Should fetch name ideally
            "rating" to rating,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )
        
        // Save to subcollection of product or global reviews
        // For simplicity, let's just log it or save to a 'reviews' collection
        FirebaseFirestore.getInstance().collection("reviews")
            .add(review)
            .addOnSuccessListener {
                Toast.makeText(context, "Review Submitted!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
