package com.codetech.speechtotext.data_source

import androidx.recyclerview.widget.DiffUtil

class HistoryDiffCallback(
    private val oldList: List<String>,
    private val newList: List<String>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare the items
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Here you can check if the contents are the same
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
