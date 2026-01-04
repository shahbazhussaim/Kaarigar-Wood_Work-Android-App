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
    private var requests: List<Map<String, Any>>,
    private val onActionClick: (String, View) -> Unit
) : RecyclerView.Adapter<AdminRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvOrderId)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDesc: TextView = view.findViewById(R.id.tvOrderItems)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnAction: View = view.findViewById(R.id.btnDeleteOrder) // Reusing delete button as action menu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = requests[position]
        val id = item["id"] as? String ?: ""
        val desc = item["description"] as? String ?: ""
        val status = item["status"] as? String ?: "PENDING"
        val timestamp = item["createdAt"] as? Long ?: item["timestamp"] as? Long ?: 0L
        val assignedName = item["assignedWorkerName"] as? String

        holder.tvId.text = "Request #...${id.takeLast(4)}"
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text = if (timestamp > 0) sdf.format(timestamp) else "N/A"
        
        holder.tvDesc.text = desc
        
        if (status == "ACCEPTED" && assignedName != null) {
            val assignedEmail = item["assignedWorkerEmail"] as? String ?: "N/A"
            holder.tvStatus.text = "By: $assignedName\n($assignedEmail)"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
        } else {
            holder.tvStatus.text = status
            when(status) {
                "PENDING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
                "COMPLETED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
                else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
            }
        }

        holder.tvTotal.text = item["estimatedPrice"] as? String ?: "Quote Pending"
        
        // Use the action button (bin icon renamed or reused) to show menu
        holder.btnAction.setOnClickListener { 
            onActionClick(id, it)
        }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<Map<String, Any>>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
