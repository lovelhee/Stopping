package com.example.stopping.chat

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.stopping.R

class PlaceInfoAdapter(
    private var itemList: List<PlaceInfo>
) : RecyclerView.Adapter<PlaceInfoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvPlace: TextView = itemView.findViewById(R.id.tvPlace)
        val btnAsk: Button = itemView.findViewById(R.id.btnAsk)

        fun bind(info: PlaceInfo) {
            tvCategory.text = info.category
            tvTitle.text = info.title
            tvPlace.text = info.place
            btnAsk.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${info.phone}"))
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_info, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    fun filterList(filteredList: List<PlaceInfo>) {
        itemList = filteredList
        notifyDataSetChanged()
    }
}
