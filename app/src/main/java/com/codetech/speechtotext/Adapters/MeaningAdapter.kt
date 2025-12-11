package com.codetech.speechtotext.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codetech.speechtotext.models.Meaning


class MeaningPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var meanings: List<Meaning> = emptyList()

    override fun getItemCount(): Int = meanings.size

    override fun createFragment(position: Int): Fragment {
        val meaning = meanings[position]
        return MeaningFragment.newInstance(meaning)
    }

    fun updateNewData(newMeanings: List<Meaning>) {
        this.meanings = newMeanings
        notifyDataSetChanged()
    }
}

