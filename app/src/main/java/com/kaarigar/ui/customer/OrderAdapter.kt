package com.kaarigar.ui.customer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
        private var orders: MutableList<Map<String, Any>>,
        private val onDelete: (String, Int) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnDeleteOrder: com.google.android.material.button.MaterialButton =
                view.findViewById(R.id.btnDeleteOrder)
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

        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        holder.tvDate.text = sdf.format(timestamp)

        val itemSummary =
                items.joinToString(", ") {
                    val qty = (it["quantity"] as? Long)?.toInt() ?: 1
                    val name = it["productName"] as? String ?: "Item"
                    "${qty}x $name"
                }
        holder.tvItems.text = itemSummary

        holder.tvTotal.text = "₹ $total"
        holder.tvStatus.text = status

        // Color code status (Basic implementation, ideally use resources)
        when (status) {
            "PENDING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
            "COMPLETED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
            else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
        }

        // Delete Logic: Allow delete if < 24 hours
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - timestamp
        val hours24 = 24 * 60 * 60 * 1000L

        if (diff < hours24 && status == "PENDING") {
            holder.btnDeleteOrder.visibility = View.VISIBLE
            holder.btnDeleteOrder.setOnClickListener { onDelete(order["id"] as String, position) }
        } else {
            holder.btnDeleteOrder.visibility = View.GONE
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Map<String, Any>>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in 0 until orders.size) {
            orders.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
