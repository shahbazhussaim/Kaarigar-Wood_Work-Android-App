package com.kaarigar.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.kaarigar.R
import com.kaarigar.data.local.entity.ProductEntity
import com.kaarigar.databinding.FragmentShopBinding

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadProducts()
    }

    private fun setupUI() {
        // Filter Drawer Toggle
        binding.btnFilter.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Chip Categories
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()
                loadProducts(category)
            } else {
                loadProducts(null)
            }
        }

        // Admin Check (Mock - assume we check AuthRepository or Prefs)
        // val isAdmin = ...
        // if(isAdmin) binding.btnAddProduct.visibility = View.VISIBLE

        binding.btnApplyFilters.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            // Apply logic
        }
    }

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter

    private fun loadProducts(category: String? = null) {
        val collection = db.collection("products")
        val query =
                if (category != null && category != "All") {
                    collection.whereEqualTo("category", category)
                } else {
                    collection
                }

        query.get()
                .addOnSuccessListener { documents ->
                    val products =
                            documents.map { doc ->
                                val data = doc.data
                                ProductEntity(
                                        doc.id,
                                        data["name"] as? String ?: "Unknown",
                                        data["description"] as? String ?: "",
                                        data["category"] as? String ?: "General",
                                        (data["price"] as? String)
                                                ?.replace(Regex("[^0-9.]"), "")
                                                ?.toDoubleOrNull()
                                                ?: 0.0,
                                        (data["stock"] as? Long)?.toInt() ?: 0,
                                        data["imageUrl"] as? String ?: "",
                                        null
                                )
                            }

                    if (::adapter.isInitialized) {
                        adapter.updateList(products)
                    } else {
                        setupAdapter(products)
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
    }

    private fun setupAdapter(products: List<ProductEntity>) {
        adapter =
                ProductAdapter(products) { product ->
                    val bundle = Bundle().apply { putString("productId", product.id) }
                    findNavController().navigate(R.id.action_shop_to_productDetail, bundle)
                }
        binding.rvProductList.layoutManager = GridLayoutManager(context, 2)
        binding.rvProductList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
