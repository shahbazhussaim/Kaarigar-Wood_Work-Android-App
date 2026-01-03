package com.kaarigar.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R
import java.text.SimpleDateFormat
import java.util.Locale

class AdminRequestAdapter(
    private var requests: MutableList<Map<String, Any>>,
    private val onActionClick: (String, View) -> Unit
) : RecyclerView.Adapter<AdminRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvOrderId) // Reuse item_order IDs
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDesc: TextView = view.findViewById(R.id.tvOrderItems)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = requests[position]
        val id = item["id"] as? String ?: ""
        val type = item["type"] as? String ?: "Request"
        val desc = item["description"] as? String ?: ""
        val status = item["status"] as? String ?: "PENDING"
        val timestamp = item["timestamp"] as? Long ?: 0L
        val estimate = item["estimatedPrice"] as? String ?: "Pending"

        holder.tvId.text = "$type #${id.takeLast(6).uppercase()}"
        
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        holder.tvDate.text = sdf.format(timestamp)
        
        holder.tvDesc.text = desc
        holder.tvStatus.text = status
        holder.tvTotal.text = if(estimate.contains("Pending")) estimate else "₹$estimate"
        
        when(status) {
            "PENDING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
            "COMPLETED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
            else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
        }
        
        holder.itemView.setOnClickListener { 
            onActionClick(id, holder.tvStatus) // Show popup on click
        }
    }

    override fun getItemCount() = requests.size
    
    fun updateData(newItems: List<Map<String, Any>>) {
        requests.clear()
        requests.addAll(newItems)
        notifyDataSetChanged()
    }
}
