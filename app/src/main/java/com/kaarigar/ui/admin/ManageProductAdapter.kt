package com.kaarigar.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kaarigar.R
import com.kaarigar.data.local.entity.ProductEntity
import com.kaarigar.databinding.ItemManageProductBinding

class ManageProductAdapter(
    private var products: List<ProductEntity>,
    private val onEdit: (ProductEntity) -> Unit,
    private val onDelete: (ProductEntity) -> Unit
) : RecyclerView.Adapter<ManageProductAdapter.ViewHolder>() {

    fun updateList(newList: List<ProductEntity>) {
        products = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemManageProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManageProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvName.text = product.name
        holder.binding.tvCategory.text = product.category
        holder.binding.tvPrice.text = "₹ ${product.basePrice}"

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
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.wood_cabinet)
                .into(holder.binding.ivProduct)
        }

        holder.binding.btnEdit.setOnClickListener { onEdit(product) }
        holder.binding.btnDelete.setOnClickListener { onDelete(product) }
    }

    override fun getItemCount() = products.size
}
