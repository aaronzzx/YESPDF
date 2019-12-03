package com.aaron.yespdf.about

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aaron.base.image.DefaultOption
import com.aaron.base.image.ImageLoader
import com.aaron.yespdf.R
import com.aaron.yespdf.common.DialogManager
import com.aaron.yespdf.common.DialogManager.GiftDialogCallback
import com.aaron.yespdf.common.UiManager
import com.blankj.utilcode.util.ImageUtils
import kotlinx.android.synthetic.main.app_recycler_item_message.view.*

class MessageAdapter<T>(
        private val activity: Activity,
        private val list: List<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val giftDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createGiftDialog(activity) { btnLeft, btnRight ->
            btnLeft.setOnClickListener { qrcodeDialog.show() }
            btnRight.setOnClickListener {
                giftDialog.dismiss()
                val bitmap = ImageUtils.drawable2Bitmap(activity.resources.getDrawable(R.drawable.app_img_qrcode))
                AboutUtils.copyImageToDevice(activity, bitmap)
                AboutUtils.goWeChatScan(activity)
                UiManager.showShort(R.string.app_wechat_scan_notice)
            }
        }
    }
    private val qrcodeDialog: Dialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogManager.createQrcodeDialog(activity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        qrcodeDialog.setCanceledOnTouchOutside(true)
        val view = inflater.inflate(R.layout.app_recycler_item_message, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            when (holder.adapterPosition) {
                INTRODUCE -> {
                    val introduce = Intent(Intent.ACTION_VIEW)
                    introduce.data = Uri.parse(Info.MY_BLOG)
                    activity.startActivity(introduce)
                }
                FEEDBACK -> try {
                    val subject: String = Info.FEEDBACK_SUBJECT
                    val text: String = Info.FEEDBACK_TEXT
                    val sendMail = Intent(Intent.ACTION_SENDTO)
                    sendMail.data = Uri.parse(Info.MY_EMAIL)
                    sendMail.putExtra(Intent.EXTRA_SUBJECT, subject)
                    sendMail.putExtra(Intent.EXTRA_TEXT, text)
                    activity.startActivity(sendMail)
                } catch (e: Exception) {
                    Log.e("MessageAdapter", e.message)
                    UiManager.showShort(R.string.app_email_app_not_found)
                }
                SOURCE_CODE -> {
                    val sourceCode = Intent(Intent.ACTION_VIEW)
                    sourceCode.data = Uri.parse(Info.SOURCE_CODE)
                    activity.startActivity(sourceCode)
                }
                GITHUB -> {
                    val github = Intent(Intent.ACTION_VIEW)
                    github.data = Uri.parse(Info.MY_GITHUB)
                    activity.startActivity(github)
                }
                RATE_APP -> AboutUtils.openCoolApk(context, context.packageName)
                GIFT -> giftDialog.show()
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position] as Message
        holder as ViewHolder
        ImageLoader.load(activity, DefaultOption.Builder(message.iconId)
                .into(holder.itemView.app_iv_icon))
        holder.itemView.app_tv_text.text = message.title
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val INTRODUCE = 0
        private const val FEEDBACK = 1
        private const val SOURCE_CODE = 2
        private const val GITHUB = 3
        private const val RATE_APP = 4
        private const val GIFT = 5
    }

}