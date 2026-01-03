package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kaarigar.databinding.FragmentInventoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvInventory.layoutManager = LinearLayoutManager(context)
        
        loadInventory()

        binding.btnAddProduct.setOnClickListener {
            Toast.makeText(context, "Feature: Add Product Dialog Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadInventory() {
        db.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error loading inventory", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                val products = snapshot?.map { doc ->
                    com.kaarigar.data.local.entity.ProductEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "Misc",
                        basePrice = doc.getDouble("price") ?: 0.0,
                        stock = doc.getLong("stock")?.toInt() ?: 0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        variantsJson = null
                    )
                } ?: emptyList()

                binding.rvInventory.adapter = com.kaarigar.ui.customer.ProductAdapter(products) { product ->
                    Toast.makeText(context, "Clicked: ${product.name}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
