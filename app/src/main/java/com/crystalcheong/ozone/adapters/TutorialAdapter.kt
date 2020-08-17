package com.crystalcheong.ozone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crystalcheong.ozone.R
import kotlinx.android.synthetic.main.activity_tutorial.view.*
import kotlinx.android.synthetic.main.item_tutorial.view.*
import timber.log.Timber

class TutorialAdapter(
    val images : List<Int>,
    val titles : List<String>,
    val descriptions : List<String>
) : RecyclerView.Adapter<TutorialAdapter.ViewPagerViewHolder>(){

    inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutorial, parent, false)
        return ViewPagerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {

        //TODO: Set the content to the current tutorial image from list
        holder.itemView.ivTutorial.setImageResource(images[position])

        //TODO: Set the content to the current tutorial title from list
        holder.itemView.tvTutorialTitle.text = titles[position]

        //TODO: Set the content to the current tutorial description from list
        holder.itemView.tvDescription.text = descriptions[position]

    }
}