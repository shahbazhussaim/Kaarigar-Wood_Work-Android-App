package com.kaarigar.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.data.local.entity.UserEntity
import com.kaarigar.databinding.ItemWorkerBinding

class WorkerAdapter(private val workers: List<UserEntity>) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(val binding: ItemWorkerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]
        holder.binding.tvWorkerName.text = worker.name
        holder.binding.tvWorkerEmail.text = worker.email
    }

    override fun getItemCount() = workers.size
}
