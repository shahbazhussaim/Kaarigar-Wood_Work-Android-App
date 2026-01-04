package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.data.local.entity.ProductEntity
import com.kaarigar.databinding.FragmentManageProductsBinding

class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ManageProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ManageProductAdapter(emptyList(), 
            onEdit = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.action_manageProducts_to_addProduct, bundle)
            },
            onDelete = { product ->
                deleteProduct(product)
            }
        )
        binding.rvManageProducts.layoutManager = LinearLayoutManager(context)
        binding.rvManageProducts.adapter = adapter
    }

    private fun loadProducts() {
        db.collection("products").get()
            .addOnSuccessListener { result ->
                val products = result.map { doc ->
                    val data = doc.data
                    ProductEntity(
                        doc.id,
                        data["name"] as? String ?: "",
                        data["description"] as? String ?: "",
                        data["category"] as? String ?: "",
                        (data["price"] as? String)?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull() ?: 0.0,
                        0,
                        data["imageUrl"] as? String ?: "",
                        null
                    )
                }
                adapter.updateList(products)
                binding.tvEmptyInventory.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun deleteProduct(product: ProductEntity) {
        db.collection("products").document(product.id).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Product Deleted", Toast.LENGTH_SHORT).show()
                loadProducts()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
