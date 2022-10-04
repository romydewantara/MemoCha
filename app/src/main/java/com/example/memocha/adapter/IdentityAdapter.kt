package com.example.memocha.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.memocha.R
import com.example.memocha.entity.Identity
import com.example.memocha.utility.AppUtil
import kotlinx.android.synthetic.main.recyclerview_item_family_data.view.*


@SuppressLint("NotifyDataSetChanged")
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
        holder.itemView.textViewBOD.text = listOfIdentity[position].dob
        holder.itemView.textViewPhone.text = listOfIdentity[position].phone

        holder.itemView.textViewName.setOnClickListener {
            identityListener.onSelectedId(listOfIdentity[position])
        }
        holder.itemView.imageCopyId.setOnClickListener {
            AppUtil.copyTextToClipboard(context, listOfIdentity[position].idNumber)
            Toast.makeText(context, context.getString(R.string.identity_text_copied), Toast.LENGTH_SHORT).show()
        }
        holder.itemView.imageCopyAddress.setOnClickListener {
            AppUtil.copyTextToClipboard(context, listOfIdentity[position].address)
            Toast.makeText(context, context.getString(R.string.identity_text_copied), Toast.LENGTH_SHORT).show()
        }
        holder.itemView.imageCopyPhone.setOnClickListener {
            AppUtil.copyTextToClipboard(context, listOfIdentity[position].phone)
            Toast.makeText(context, context.getString(R.string.identity_text_copied), Toast.LENGTH_SHORT).show()
        }

        val cardViewParams = holder.itemView.cardViewIdentityCard.layoutParams
        val linearLayoutParam = LinearLayout.LayoutParams(cardViewParams.width, cardViewParams.height)
        if (position == listOfIdentity.size - 1) linearLayoutParam.setMargins(35, 42, 35, 40)
        else linearLayoutParam.setMargins(35, 42, 35, 0)
        holder.itemView.cardViewIdentityCard.layoutParams = linearLayoutParam
    }

    override fun getItemCount(): Int = listOfIdentity.size

    fun setData(list: List<Identity>) {
        listOfIdentity.clear()
        listOfIdentity.addAll(list)
        notifyDataSetChanged()
    }

    class IdentityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface IdentityListener {
        fun onSelectedId(identity: Identity)
    }
}