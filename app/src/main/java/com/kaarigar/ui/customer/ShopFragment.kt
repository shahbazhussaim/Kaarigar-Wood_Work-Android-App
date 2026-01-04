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
        
        val initialCategory = arguments?.getString("category")
        if (initialCategory != null) {
            selectCategoryChip(initialCategory)
            loadProducts(initialCategory)
        } else {
            loadProducts()
        }
    }

    private fun selectCategoryChip(category: String) {
        val chipId = when (category) {
            "Sofa" -> R.id.chipSofa
            "Table" -> R.id.chipTable
            "Bed" -> R.id.chipBed
            "Storage" -> R.id.chipStorage
            "Kitchen" -> R.id.chipKitchen
            "Artisan Door" -> R.id.chipDoors
            else -> R.id.chipAll
        }
        binding.chipGroupCategories.check(chipId)
    }

    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter
    private var allProducts = listOf<ProductEntity>()

    private fun setupUI() {
        // Filter Drawer Toggle
        binding.btnFilter.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Real-time Search
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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

        binding.btnApplyFilters.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun filterProducts(query: String) {
        val filtered = allProducts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
        
        adapter.updateList(filtered)
        
        if (filtered.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
            binding.rvProductList.visibility = View.GONE
        } else {
            binding.llEmpty.visibility = View.GONE
            binding.rvProductList.visibility = View.VISIBLE
        }
    }

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
                    
                    allProducts = products
                    
                    if (products.isNotEmpty()) {
                        binding.llEmpty.visibility = View.GONE
                        binding.rvProductList.visibility = View.VISIBLE
                    } else {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.rvProductList.visibility = View.GONE
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
