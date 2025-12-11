package com.codetech.speechtotext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.databinding.ItemLanguageBinding
import com.codetech.speechtotext.Utils.Country

class LanguageSelectionAdapter(
    private val context: Context,
    private val countries: List<Country>
) : RecyclerView.Adapter<LanguageSelectionAdapter.LanguageViewHolder>() {

    private var selectedPosition: Int = -1
    private var onItemClickListener: ((String) -> Unit)? = null

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val country = countries[position]
            binding.languageText.text = country.name

            binding.root.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                onItemClickListener?.invoke(country.name)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    fun setSelectedLanguage(language: String) {
        selectedPosition = countries.indexOfFirst { it.name == language }.takeIf { it != -1 } ?: -1
        notifyDataSetChanged()
    }
}
