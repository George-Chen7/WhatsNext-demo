package com.example.whatsnextdemo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsnextdemo.data.model.CareerRecommendation
import com.example.whatsnextdemo.databinding.ItemCareerCardBinding

class CareerAdapter(
    private val items: List<CareerRecommendation>
) : RecyclerView.Adapter<CareerAdapter.CareerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CareerViewHolder {
        val binding = ItemCareerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CareerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CareerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CareerViewHolder(
        private val binding: ItemCareerCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CareerRecommendation) {
            binding.tvCareerTitle.text = item.title
            binding.tvCareerReason.text = item.reason
            binding.tvCareerTags.text = item.tags.joinToString("  ")
        }
    }
}
