package com.kaarigar.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaarigar.R

class AdminCategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_categories, container, false)
        
        val rv = view.findViewById<RecyclerView>(R.id.rvCategories)
        rv.layoutManager = LinearLayoutManager(context)
        
        val categories = listOf(
            CategoryInfo("Sofa", "Plush seating for living spaces", "🛋️", 5),
            CategoryInfo("Chair", "Ergonomic artisan chairs", "🪑", 3),
            CategoryInfo("Table", "Handcrafted dining & coffee tables", "📑", 4),
            CategoryInfo("Bed", "Premium teak bed frames", "🛏️", 2),
            CategoryInfo("Storage", "Kiln-dried wardrobes & cabinets", "📦", 6),
            CategoryInfo("Kitchen", "Bespoke modular kitchen fittings", "🍳", 3),
            CategoryInfo("Artisan Door", "Intricate hand-carved entryways", "🚪", 4)
        )
        
        rv.adapter = CategoryAdapter(categories)
        
        return view
    }

    data class CategoryInfo(val name: String, val desc: String, val icon: String, val imageCount: Int)

    class CategoryAdapter(private val list: List<CategoryInfo>) : RecyclerView.Adapter<CategoryAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val name: TextView = v.findViewById(R.id.tvName)
            val desc: TextView = v.findViewById(R.id.tvDesc)
            val icon: TextView = v.findViewById(R.id.tvIcon)
            val count: TextView = v.findViewById(R.id.tvCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category_admin, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.name.text = item.name
            holder.desc.text = item.desc
            holder.icon.text = item.icon
            holder.count.text = "Visual Assets: ${item.imageCount}"
        }

        override fun getItemCount() = list.size
    }
}
