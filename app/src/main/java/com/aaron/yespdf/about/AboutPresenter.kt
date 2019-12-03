package com.aaron.yespdf.about

import com.aaron.yespdf.R
import com.aaron.yespdf.common.App
import java.util.*

internal class AboutPresenter(view: IAboutView) : IAboutPresenter(view) {

    private val messageList: MutableList<Message> = ArrayList()
    private val libraryList: MutableList<Library> = ArrayList()

    override fun requestMessage(iconId: IntArray, title: Array<String>) {
        if (iconId.isNotEmpty() && iconId.size == title.size) {
            for (i in iconId.indices) {
                val message = Message(iconId[i], title[i])
                messageList.add(message)
            }
            view.onShowMessage(messageList)
        }
    }

    override fun requestLibrary(name: Array<String>, author: Array<String>, introduce: Array<String>) {
        if (name.isEmpty() || author.isEmpty() || introduce.isEmpty()) {
            return
        }
        if (name.size == author.size && name.size == introduce.size) {
            for (i in name.indices) {
                val library = Library(name[i], author[i], introduce[i])
                libraryList.add(library)
            }
            view.onShowLibrary(libraryList)
        }
    }

    internal object Element {
        val TITLE = arrayOf(App.getContext().getString(R.string.app_introduce), App.getContext().getString(R.string.app_feedback), App.getContext().getString(R.string.app_source_code), App.getContext().getString(R.string.app_github), App.getContext().getString(R.string.app_rate_app), App.getContext().getString(R.string.app_gift))
        val ICON_ID = intArrayOf(
                R.drawable.app_ic_introduce,
                R.drawable.app_ic_email,
                R.drawable.app_ic_source_code,
                R.drawable.app_ic_github,
                R.drawable.app_ic_star_black_24dp,
                R.drawable.app_ic_gift)
        val LIBRARY_NAME = arrayOf(
                "AndroidTreeView",
                "RealtimeBlurView",
                "ParallaxBackLayout",
                "greenDAO",
                "ButterKnife",
                "Glide",
                "StatusBarUtil",
                "EventBus",
                "RxJava",
                "RxAndroid",
                "AndroidPdfViewer")
        val LIBRARY_AUTHOR = arrayOf(
                "bmelnychuk",
                "mmin18",
                "anzewei",
                "greenrobot",
                "JakeWharton",
                "bumptech",
                "Jaeger",
                "greenrobot",
                "ReactiveX",
                "ReactiveX",
                "barteksc")
        val LIBRARY_INTRODUCE = arrayOf(
                "AndroidTreeView. TreeView implementation for android",
                "A realtime blurring overlay for Android (like iOS UIVisualEffectView)",
                "无需改动原有activity只需要一个annotation轻松实现任意方向的滑动返回，默认提供微信滑动、跟随滑动、以及单个滑动，并且可以自定义滑动效果",
                "greenDAO is a light & fast ORM solution for Android that maps objects to SQLite databases.",
                "Bind Android views and callbacks to fields and methods.",
                "An image loading and caching library for Android focused on smooth scrolling",
                "A util for setting status bar style on Android App",
                "Event bus for Android and Java that simplifies communication between Activities, Fragments, Threads, Services, etc. Less code, better quality. ",
                "RxJava – Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM.",
                "RxJava bindings for Android",
                "Android view for displaying PDFs rendered with PdfiumAndroid")
    }

}