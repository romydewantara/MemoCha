package com.example.kuntan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import kotlinx.android.synthetic.main.recyclerview_item_category.view.*

class CategoryAdapter(context: Context, private val categoryAdapterListener: CategoryAdapterListener) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categories = arrayListOf(
        context.getString(R.string.category_skin_care),
        context.getString(R.string.category_health),
        context.getString(R.string.category_food_and_drink),
        context.getString(R.string.category_kitchen),
        context.getString(R.string.category_book),
        context.getString(R.string.category_office_tools),
        context.getString(R.string.category_computer),
        context.getString(R.string.category_electro),
        context.getString(R.string.category_fashion),
        context.getString(R.string.category_child_fashion),
        context.getString(R.string.category_toys),
        context.getString(R.string.category_sport),
        context.getString(R.string.category_gold),
        context.getString(R.string.category_phone),
        context.getString(R.string.category_film),
        context.getString(R.string.category_tour),
        context.getString(R.string.category_automotive),
        context.getString(R.string.category_party),
        context.getString(R.string.category_tools),
        context.getString(R.string.category_property),
        context.getString(R.string.category_pet_care),
        context.getString(R.string.category_others)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_category, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.itemView.textViewItemCategory.text = category
        holder.itemView.textViewItemCategory.setOnClickListener {
            categoryAdapterListener.onCategoryClicked(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface CategoryAdapterListener {
        fun onCategoryClicked(category: String)
    }
}