package com.kaarigar.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R

class CartAdapter(
    private var items: MutableList<Map<String, Any>>,
    private val onRemove: (String, Int) -> Unit,
    private val onUpdateQty: (String, Int, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvProductName)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvVariant: TextView = view.findViewById(R.id.tvVariant)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
        val btnPlus: ImageButton = view.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = view.findViewById(R.id.btnMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val id = item["id"] as String
        val name = item["productName"] as? String ?: "Item"
        val price = item["price"] as? String ?: "0"
        val qty = (item["quantity"] as? Long)?.toInt() ?: 1
        val size = item["size"] as? String ?: "-"
        val color = item["color"] as? String ?: "-"

        holder.tvName.text = name
        holder.tvPrice.text = price
        holder.tvVariant.text = "Size: $size | Color: $color"
        holder.tvQuantity.text = qty.toString()
        
        val imageUrl = item["imageUrl"] as? String
        val ivProduct = holder.itemView.findViewById<ImageView>(R.id.ivProduct)
        if (!imageUrl.isNullOrEmpty()) {
             com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_circle_white)
                .into(ivProduct)
        } else {
             ivProduct.setImageResource(R.drawable.bg_circle_white)
        }

        holder.btnRemove.setOnClickListener { onRemove(id, position) }
        
        holder.btnPlus.setOnClickListener { 
            onUpdateQty(id, qty + 1, position)
        }
        
        holder.btnMinus.setOnClickListener {
            if(qty > 1) {
                onUpdateQty(id, qty - 1, position)
            }
        }
    }

    override fun getItemCount() = items.size
    
    fun updateData(newItems: List<Map<String, Any>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
