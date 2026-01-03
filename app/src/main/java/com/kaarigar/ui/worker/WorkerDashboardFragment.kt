package com.kaarigar.ui.worker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.KaarigarApp
import com.kaarigar.R
import com.kaarigar.databinding.FragmentWorkerDashboardBinding
import com.kaarigar.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: WorkerTaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadTasks()
        
        binding.btnLogout.setOnClickListener {
             auth.signOut()
             val database = (requireActivity().application as KaarigarApp).database
             CoroutineScope(Dispatchers.IO).launch {
                 database.userDao().clearUsers()
             }
             val intent = Intent(requireContext(), AuthActivity::class.java)
             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             startActivity(intent)
             requireActivity().finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkerTaskAdapter(mutableListOf()) { task ->
            showTaskDetailDialog(task)
        }
        binding.rvWorkerTasks.layoutManager = LinearLayoutManager(context)
        binding.rvWorkerTasks.adapter = adapter
    }

    private fun loadTasks() {
        // Workers see PENDING and ACCEPTED Requests
        db.collection("requests")
            .whereIn("status", listOf("PENDING", "ACCEPTED"))
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                 val tasks = documents.map { 
                    val data = it.data.toMutableMap()
                    data["id"] = it.id
                    data
                }
                adapter.updateData(tasks)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading tasks", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showTaskDetailDialog(task: Map<String, Any>) {
        val id = task["id"] as String
        val desc = task["description"] as? String ?: "No Description"
        val status = task["status"] as? String ?: "PENDING"
        val price = task["estimatedPrice"] as? String ?: "N/A"
        
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(if (status == "PENDING") "New Job Request" else "Active Job")
            .setMessage("Description: $desc\nPrice Estimate: ₹$price\n\nCurrent Status: $status")
            
        if (status == "PENDING") {
             builder.setPositiveButton("Accept Job") { _, _ ->
                 updateTaskStatus(id, "ACCEPTED")
             }
             builder.setNegativeButton("Ignore", null)
        } else if (status == "ACCEPTED") {
             builder.setPositiveButton("Mark Completed") { _, _ ->
                 updateTaskStatus(id, "COMPLETED")
             }
             builder.setNeutralButton("Cancel Job") { _, _ ->
                 updateTaskStatus(id, "CANCELLED")
             }
        }
        
        builder.show()
    }

    private fun updateTaskStatus(id: String, status: String) {
        db.collection("requests").document(id).update("status", status)
            .addOnSuccessListener {
                Toast.makeText(context, "Status moved to $status", Toast.LENGTH_SHORT).show()
                loadTasks()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class WorkerTaskAdapter(
    private var tasks: MutableList<Map<String, Any>>,
    private val onClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<WorkerTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvOrderId) // Reusing item_order IDs
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDesc: TextView = view.findViewById(R.id.tvOrderItems)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = tasks[position]
        val id = item["id"] as? String ?: ""
        val type = item["type"] as? String ?: "Request"
        val desc = item["description"] as? String ?: ""
        val status = item["status"] as? String ?: "PENDING"
        val timestamp = item["timestamp"] as? Long ?: 0L
        
        holder.tvId.text = "$type #...${id.takeLast(4)}"
        
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        holder.tvDate.text = sdf.format(timestamp)
        
        holder.tvDesc.text = desc
        holder.tvStatus.text = "Action Required"
        holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
        holder.tvTotal.visibility = View.GONE 
        
        holder.itemView.setOnClickListener { 
            onClick(item)
        }
    }

    override fun getItemCount() = tasks.size
    
    fun updateData(newItems: List<Map<String, Any>>) {
        tasks.clear()
        tasks.addAll(newItems)
        notifyDataSetChanged()
    }
}
