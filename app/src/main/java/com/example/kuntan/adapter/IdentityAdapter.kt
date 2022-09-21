package com.example.kuntan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import com.example.kuntan.entity.Identity
import kotlinx.android.synthetic.main.recyclerview_item_family_data.view.*

class IdentityAdapter(
    private val context: Context,
    private val listOfIdentity: ArrayList<Identity>,
    private val identityListener: IdentityListener
) : RecyclerView.Adapter<IdentityAdapter.IdentityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdentityViewHolder {
        return IdentityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_family_data, parent, false))
    }

    override fun onBindViewHolder(holder: IdentityViewHolder, position: Int) {
        holder.itemView.textViewName.text = listOfIdentity[position].name
        holder.itemView.textViewId.text = listOfIdentity[position].idNumber
        holder.itemView.textViewAddress.text = listOfIdentity[position].address

        holder.itemView.textViewName.setOnClickListener {
            identityListener.onSelectedId(listOfIdentity[position].idNumber)
        }
        holder.itemView.imageCopyId.setOnClickListener {

        }
        holder.itemView.imageCopyAddress.setOnClickListener {

        }
    }

    override fun getItemCount(): Int = listOfIdentity.size

    class IdentityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface IdentityListener {
        fun onSelectedId(id: String)
    }
}