package com.kaarigar.ui.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R
import com.kaarigar.data.local.entity.ProductEntity
import com.kaarigar.databinding.ItemProductBinding

class ProductAdapter(
        private var products: List<ProductEntity>,
        private val onProductClick: (ProductEntity) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateList(newProducts: List<ProductEntity>) {
        products = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(val binding: ItemProductBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvProductName.text = product.name
        holder.binding.tvDescription.text = product.description
        holder.binding.tvPrice.text = "₹ ${product.basePrice}"

        if (!product.imageUrl.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                    .load(product.imageUrl)
                    .placeholder(
                            R.drawable.bg_circle_white
                    ) // Use existing drawable as placeholder for now
                    .into(holder.binding.ivProduct)
        } else {
            holder.binding.ivProduct.setImageResource(R.drawable.bg_circle_white)
        }

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount() = products.size
}
