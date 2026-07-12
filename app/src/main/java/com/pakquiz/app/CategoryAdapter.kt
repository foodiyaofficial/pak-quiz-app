package com.pakquiz.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val background: LinearLayout = view.findViewById(R.id.cardBackground)
        val emoji: TextView = view.findViewById(R.id.categoryEmoji)
        val title: TextView = view.findViewById(R.id.categoryTitle)
        val subtitle: TextView = view.findViewById(R.id.categorySubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.title.text = category.title
        holder.subtitle.text = category.subtitle
        holder.emoji.text = category.emoji
        holder.background.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, category.colorRes))
        holder.itemView.setOnClickListener { onClick(category) }
    }

    override fun getItemCount(): Int = categories.size
}
