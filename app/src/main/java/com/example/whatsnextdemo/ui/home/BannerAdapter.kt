package com.example.whatsnextdemo.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsnextdemo.data.model.BannerItem
import com.example.whatsnextdemo.databinding.ItemBannerBinding

class BannerAdapter(
    private val items: List<BannerItem>
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BannerViewHolder(
        private val binding: ItemBannerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BannerItem) {
            binding.bannerContainer.setBackgroundColor(item.backgroundColor)
            binding.tvBannerTitle.text = item.title
            binding.tvBannerSubtitle.text = item.subtitle
        }
    }
}
