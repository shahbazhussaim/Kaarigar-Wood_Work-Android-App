package com.kaarigar.ui.worker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.data.local.entity.RequestEntity
import com.kaarigar.databinding.ItemRequestBinding

class RequestAdapter(
    private val requests: List<RequestEntity>,
    private val onAcceptClick: (RequestEntity) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(val binding: ItemRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.tvRequestTitle.text = request.type
        holder.binding.tvDescription.text = request.description
        holder.binding.tvPrice.text = "₹ ${request.predictedPrice}"
        
        holder.binding.btnAccept.setOnClickListener {
            onAcceptClick(request)
        }
    }

    override fun getItemCount() = requests.size
}
