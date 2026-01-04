package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kaarigar.R
import com.kaarigar.databinding.FragmentManageWorkersBinding

class ManageWorkersFragment : Fragment() {

    private var _binding: FragmentManageWorkersBinding? = null
    private val binding
        get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AdminWorkerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageWorkersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadWorkers()

        binding.fabAddWorker.setOnClickListener {
            findNavController().navigate(R.id.action_manageWorkers_to_workerRegister)
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminWorkerAdapter(mutableListOf()) { workerId ->
            deleteWorker(workerId)
        }
        binding.rvWorkers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkers.adapter = adapter
    }

    private fun loadWorkers() {
        // Use addSnapshotListener for real-time updates
        db.collection("users")
            .whereEqualTo("role", "WORKER")
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val workers = documents.map {
                        val data = it.data.toMutableMap()
                        data["id"] = it.id
                        data
                    }
                    adapter.updateData(workers)
                }
            }
    }

    private fun deleteWorker(workerId: String) {
        db.collection("users")
            .document(workerId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Worker Deleted", Toast.LENGTH_SHORT).show()
                loadWorkers()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/* ===================== ADAPTER ===================== */

class AdminWorkerAdapter(
    private var workers: MutableList<Map<String, Any>>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<AdminWorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_admin, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]

        holder.tvName.text = "Name: ${worker["name"] ?: "N/A"}"
        holder.tvEmail.text =
            "Email: ${worker["email"] ?: "N/A"} | CNIC: ${worker["cnic"] ?: "N/A"}"

        holder.btnDelete.setOnClickListener {
            val id = worker["id"] as? String
            if (id != null) {
                onDeleteClick(id)
            }
        }
    }

    override fun getItemCount(): Int = workers.size

    fun updateData(newWorkers: List<Map<String, Any>>) {
        workers.clear()
        workers.addAll(newWorkers)
        notifyDataSetChanged()
    }
}
