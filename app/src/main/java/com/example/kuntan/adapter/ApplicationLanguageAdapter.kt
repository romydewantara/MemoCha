package com.example.kuntan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kuntan.R
import kotlinx.android.synthetic.main.recyclerview_item_language.view.*

class ApplicationLanguageAdapter(private val context: Context, private val applicationLanguageListener: ApplicationLanguageListener) :
    RecyclerView.Adapter<ApplicationLanguageAdapter.ApplicationLanguageViewHolder>() {

    private val languages = arrayListOf(context.getString(R.string.setting_language_bahasa), context.getString(R.string.setting_language_english))
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationLanguageViewHolder {
        return ApplicationLanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_language, parent, false))
    }

    override fun onBindViewHolder(holder: ApplicationLanguageViewHolder, position: Int) {
        when(languages[position]) {
            context.getString(R.string.setting_language_bahasa) -> {
                holder.itemView.imageIconLanguage.setImageResource(R.drawable.ic_bahasa)
                holder.itemView.textViewItemLanguage.text = context.getString(R.string.setting_language_bahasa)
            }
            context.getString(R.string.setting_language_english) -> {
                holder.itemView.imageIconLanguage.setImageResource(R.drawable.ic_english)
                holder.itemView.textViewItemLanguage.text = context.getString(R.string.setting_language_english)
            }
        }
        holder.itemView.languageItem.setOnClickListener {
            applicationLanguageListener.onLanguageSelected(languages[position])
        }
    }

    override fun getItemCount(): Int = languages.size

    class ApplicationLanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface ApplicationLanguageListener {
        fun onLanguageSelected(language: String)
    }
}