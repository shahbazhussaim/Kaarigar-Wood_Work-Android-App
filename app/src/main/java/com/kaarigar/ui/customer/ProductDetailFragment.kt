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
                        val price = document.getString("price") ?: "₹0" // Assuming price is stored as string in DB
                        val description = document.getString("description") ?: "No description available."
                        
                        binding.tvProductName.text = name
                        binding.tvProductPrice.text = "₹$price"
                        binding.tvProductDescription.text = description
                        
                        // Update click listeners to use the fetched data
                        binding.btnAddToCart.setOnClickListener {
                            addToCart(name, price)
                        }
                    }
                }
        } else {
             Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show()
             findNavController().popBackStack()
        }
        
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

        // Add to Cart
        // Listeners for quantity are below

        // Buy Now
        binding.btnBuyNow.setOnClickListener {
            // First add to cart or pass data directly? 
            // Plan says: "When user clicks Buy Now ... Open a new page where user enters..."
            // Usually Buy Now skips cart or adds to cart and goes to checkout.
            // Let's go to Checkout directly with args.
            findNavController().navigate(R.id.action_productDetail_to_checkout)
        }
        
        // Write Review
        binding.btnWriteReview.setOnClickListener {
            showReviewDialog()
        }
        
        // Back Button in Toolbar
        binding.toolbar.setNavigationOnClickListener {
             findNavController().popBackStack()
        }
        
        // Select Default Chips
        binding.cgColors.check(R.id.chipNatural)
        binding.cgSizes.check(R.id.chipMedium)
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
            .addOnFailureListener {
                // Log failure
            }
            
        // Optimistic UI
        Toast.makeText(context, "Added to Cart (Syncing...)", Toast.LENGTH_SHORT).show()
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
