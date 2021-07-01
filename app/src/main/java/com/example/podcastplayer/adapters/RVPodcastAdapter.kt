package com.example.podcastplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.podcastplayer.databinding.ItemEpisodeBinding
import tw.ktrssreader.kotlin.model.item.AutoMixItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RVPodcastAdapter : RecyclerView.Adapter<RVPodcastAdapter.ViewHolder>() {

    private var mList: List<AutoMixItem> = mutableListOf()
    private var mListener: Listener? = null

    fun setList(items : List<AutoMixItem>) {
        mList = items
        notifyDataSetChanged()
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onEpisodeClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position], position)
    }

    inner class ViewHolder(private val itemBinding: ItemEpisodeBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: AutoMixItem, position: Int) {
            itemBinding.tvEpisodePubDate.text = ""
            if (!item.pubDate.isNullOrBlank()) {
                try {
                    val localDate = LocalDate.parse(item.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    val outputFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                    itemBinding.tvEpisodePubDate.text = localDate.format(outputFormatter)
                } catch (e : Exception) {
                    // DoNothing
                }
            }
            itemBinding.tvEpisodeTitle.text = item.title ?: ""
            Glide.with(itemBinding.root)
                .clear(itemBinding.ivEpisodeCover)
            Glide.with(itemBinding.root)
                .load(item.image).placeholder(android.R.drawable.stat_sys_warning)
                .into(itemBinding.ivEpisodeCover)
            itemBinding.root.setOnClickListener {
                mListener?.onEpisodeClicked(position)
            }
        }
    }
}