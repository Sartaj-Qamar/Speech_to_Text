package com.codetech.speechtotext.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.R

class SuggestionAdapter(private val suggestions: List<String>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val suggestionTextView: TextView = itemView.findViewById(R.id.suggestion_text_view)

        init {
            itemView.setOnClickListener {
                onItemClick(suggestions[adapterPosition]) // Call the click listener
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.suggestionTextView.text = suggestions[position]
    }

    override fun getItemCount(): Int = suggestions.size
}
