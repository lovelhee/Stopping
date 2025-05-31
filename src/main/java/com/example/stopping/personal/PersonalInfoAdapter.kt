package com.example.stopping.personal

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stopping.R

class PersonalInfoAdapter(
    private val items: List<SupportInfo>,
    private val onInfoClick: (Int) -> Unit) :
    RecyclerView.Adapter<PersonalInfoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val btnAsk: Button = view.findViewById(R.id.btnAsk)
        val btnInfo: Button = view.findViewById(R.id.btnInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personal_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvCategory.text = item.category
        holder.tvTitle.text = item.programName
        holder.tvContent.text = item.description
        holder.btnAsk.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${item.contact}")
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.btnInfo.setOnClickListener {
            onInfoClick(item.id)
        }
    }

    override fun getItemCount(): Int = items.size
}
