package com.aaron.yespdf.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaron.yespdf.R
import kotlinx.android.synthetic.main.app_recycler_item_library.view.*

class LibraryAdapter<T>(
        private val context: Context,
        private val list: List<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_recycler_item_library, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            when (holder.adapterPosition) {
                0 -> {
                    val androidTreeView = Intent(Intent.ACTION_VIEW)
                    androidTreeView.data = Uri.parse("https://github.com/bmelnychuk/AndroidTreeView")
                    context.startActivity(androidTreeView)
                }
                1 -> {
                    val realtimeBlurView = Intent(Intent.ACTION_VIEW)
                    realtimeBlurView.data = Uri.parse("https://github.com/mmin18/RealtimeBlurView")
                    context.startActivity(realtimeBlurView)
                }
                2 -> {
                    val parallaxBackLayout = Intent(Intent.ACTION_VIEW)
                    parallaxBackLayout.data = Uri.parse("https://github.com/anzewei/ParallaxBackLayout")
                    context.startActivity(parallaxBackLayout)
                }
                3 -> {
                    val greenDao = Intent(Intent.ACTION_VIEW)
                    greenDao.data = Uri.parse("https://github.com/greenrobot/greenDAO")
                    context.startActivity(greenDao)
                }
                4 -> {
                    val butterKnife = Intent(Intent.ACTION_VIEW)
                    butterKnife.data = Uri.parse("https://github.com/JakeWharton/butterknife")
                    context.startActivity(butterKnife)
                }
                5 -> {
                    val glide = Intent(Intent.ACTION_VIEW)
                    glide.data = Uri.parse("https://github.com/bumptech/glide")
                    context.startActivity(glide)
                }
                6 -> {
                    val statusBarUtil = Intent(Intent.ACTION_VIEW)
                    statusBarUtil.data = Uri.parse("https://github.com/laobie/StatusBarUtil")
                    context.startActivity(statusBarUtil)
                }
                7 -> {
                    val eventBus = Intent(Intent.ACTION_VIEW)
                    eventBus.data = Uri.parse("https://github.com/greenrobot/EventBus")
                    context.startActivity(eventBus)
                }
                8 -> {
                    val rxJava = Intent(Intent.ACTION_VIEW)
                    rxJava.data = Uri.parse("https://github.com/ReactiveX/RxJava")
                    context.startActivity(rxJava)
                }
                9 -> {
                    val rxAndroid = Intent(Intent.ACTION_VIEW)
                    rxAndroid.data = Uri.parse("https://github.com/ReactiveX/RxAndroid")
                    context.startActivity(rxAndroid)
                }
                10 -> {
                    val androidPdfViewer = Intent(Intent.ACTION_VIEW)
                    androidPdfViewer.data = Uri.parse("https://github.com/barteksc/AndroidPdfViewer")
                    context.startActivity(androidPdfViewer)
                }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val library = list[position] as Library
        holder as ViewHolder
        holder.itemView.app_tv_name.text = library.name
        holder.itemView.app_tv_author.text = library.author
        holder.itemView.app_tv_detail.text = library.introduce
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}