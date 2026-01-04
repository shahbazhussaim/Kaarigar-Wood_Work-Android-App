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
import com.kaarigar.KarigarApp
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
    private var currentFilterMode = "All"

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
        
        // Default load
        loadTasks(currentFilterMode)
        
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            currentFilterMode = if (checkedIds.contains(R.id.chipAccepted)) {
                "Accepted"
            } else {
                "All"
            }
            loadTasks(currentFilterMode)
        }
        
        binding.btnLogout.setOnClickListener {
             auth.signOut()
             val database = (requireActivity().application as KarigarApp).database
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

    private fun loadTasks(filterMode: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Show PENDING (available for all) AND My Accepted/Completed ones
        // Status PENDING is locked to nobody.
        // Status ACCEPTED/COMPLETED is locked to the assignedWorkerId.
        
        db.collection("requests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val allFetched = documents.mapNotNull { 
                    val data = it.data.toMutableMap()
                    data["id"] = it.id
                    data
                }
                
                val filtered = allFetched.filter { task ->
                    val status = (task["status"] as? String)?.uppercase() ?: "PENDING"
                    val assignedId = task["assignedWorkerId"] as? String
                    
                    if (filterMode == "Accepted") {
                        // Accepted Tab: Show only those assigned to ME (Accepted or Completed)
                        assignedId == currentUserId && (status == "ACCEPTED" || status == "COMPLETED")
                    } else {
                        // All Tab logic per requirement:
                        // "Show ALL maintenance requests EXCEPT requests already accepted by other workers"
                        // PLUS "requests already accepted by THIS worker"
                        if (status == "PENDING") {
                            true // Available for everyone
                        } else {
                            // Already accepted: Show only if it's MINE
                            assignedId == currentUserId
                        }
                    }
                }
                
                adapter.updateData(filtered)
                binding.llEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading tasks: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showTaskDetailDialog(task: Map<String, Any>) {
        val id = task["id"] as String
        val bundle = Bundle().apply {
            putString("requestId", id)
        }
        androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(
            R.id.action_workerDashboard_to_requestDetail, 
            bundle
        )
    }
    
    private fun acceptTask(id: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // We need the worker's name to save it
        db.collection("users").document(currentUserId).get().addOnSuccessListener { userDoc ->
            val workerName = userDoc.getString("name") ?: "Worker"
            
            val updates = hashMapOf<String, Any>(
                "status" to "ACCEPTED",
                "assignedWorkerId" to currentUserId,
                "assignedWorkerName" to workerName
            )
            
            db.collection("requests").document(id).update(updates)
                .addOnSuccessListener {
                     Toast.makeText(context, "Job Accepted!", Toast.LENGTH_SHORT).show()
                     loadTasks(currentFilterMode) // Refresh default
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to accept job", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateTaskStatus(id: String, status: String) {
        db.collection("requests").document(id).update("status", status)
            .addOnSuccessListener {
                Toast.makeText(context, "Status moved to $status", Toast.LENGTH_SHORT).show()
                loadTasks(currentFilterMode)
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
        holder.tvStatus.text = status
        when(status.uppercase()) {
            "PENDING" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
            "ACCEPTED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_green)
            "COMPLETED" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_gray)
            else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_capsule_yellow)
        }
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
