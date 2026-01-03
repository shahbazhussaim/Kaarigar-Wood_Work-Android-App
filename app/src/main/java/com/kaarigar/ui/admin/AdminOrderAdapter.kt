package com.kaarigar.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R
import java.text.SimpleDateFormat
import java.util.Locale

class AdminOrderAdapter(
    private var orders: MutableList<Map<String, Any>>,
    private val onStatusClick: (String, View) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val id = order["id"] as? String ?: "Unknown"
        val timestamp = order["timestamp"] as? Long ?: 0L
        val status = order["status"] as? String ?: "PENDING"
        val total = order["totalAmount"] as? Double ?: 0.0
        val items = order["items"] as? List<Map<String, Any>> ?: emptyList()

        holder.tvOrderId.text = "Order #${id.takeLast(6).uppercase()}"
        
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        holder.tvDate.text = sdf.format(timestamp)
        
        val itemSummary = items.joinToString(", ") { 
             val name = it["productName"] as? String ?: "Item"
             "$name"
        }
        holder.tvItems.text = itemSummary
        
        holder.tvTotal.text = "₹ $total"
        holder.tvStatus.text = status
        
        when(status) {
            "PENDING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
            "PROCESSING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
            "COMPLETED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
            else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
        }
        
        // Admin click to change status
        holder.tvStatus.setOnClickListener {
            onStatusClick(id, it)
        }
    }

    override fun getItemCount() = orders.size
    
    fun updateData(newOrders: List<Map<String, Any>>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
