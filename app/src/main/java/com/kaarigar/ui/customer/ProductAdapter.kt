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
            if (product.imageUrl.startsWith("local://")) {
                val resourceName = product.imageUrl.replace("local://", "")
                val resId = holder.itemView.context.resources.getIdentifier(
                    resourceName, "drawable", holder.itemView.context.packageName
                )
                if (resId != 0) {
                    holder.binding.ivProduct.setImageResource(resId)
                } else {
                    holder.binding.ivProduct.setImageResource(R.drawable.wood_cabinet)
                }
            } else {
                com.bumptech.glide.Glide.with(holder.itemView.context)
                        .load(product.imageUrl)
                        .placeholder(R.drawable.hero_banner)
                        .into(holder.binding.ivProduct)
            }
        } else {
            holder.binding.ivProduct.setImageResource(R.drawable.wood_cabinet)
        }

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount() = products.size
}
