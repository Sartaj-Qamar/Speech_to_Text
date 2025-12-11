package com.codetech.speechtotext.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.TinyDB
import com.codetech.speechtotext.databinding.LanguageLayoutBinding
import com.codetech.speechtotext.models.LanguageData

class LanguageAdapter(
    private var languageList: ArrayList<LanguageData>,
    private val clickListener: OnLanguageItemCLick,
    private val tinyDB: TinyDB,
) : RecyclerView.Adapter<LanguageAdapter.RatioViewHolder>() {

    private var selectedPosition = -1

    init {
        selectedPosition = tinyDB.getInt("selectedPosition", -1)
    }

    inner class RatioViewHolder(private val binding: LanguageLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(items: LanguageData, position: Int) {

            binding.lanImage.setImageResource(items.image)
            binding.langName.text = items.languageName

            if (position == selectedPosition) {
                binding.radioButton.setImageResource(R.drawable.checked)
            } else {
                binding.radioButton.setImageResource(R.drawable.ic_radio_unchecked)
            }

            binding.root.setOnClickListener {
                clickListener.onLanguageClick(items, position)
                updateSelectedPosition(position)
            }
        }

        private fun updateSelectedPosition(position: Int) {
            val prevPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(prevPosition)
            notifyItemChanged(selectedPosition)
            tinyDB.putInt("selectedPosition", selectedPosition)
            tinyDB.putInt("lastPosition", prevPosition)
            tinyDB.putString("selectedLanguageName", languageList[selectedPosition].languageName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioViewHolder {
        val binding = LanguageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatioViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    override fun onBindViewHolder(holder: RatioViewHolder, position: Int) {
        val currentItem = languageList[position]
        holder.bind(currentItem, position)
    }

    interface OnLanguageItemCLick {
        fun onLanguageClick(holder: LanguageData, position: Int)
    }
}
