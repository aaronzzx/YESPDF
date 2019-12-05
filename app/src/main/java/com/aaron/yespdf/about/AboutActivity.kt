package com.aaron.yespdf.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.aaron.yespdf.BuildConfig
import com.aaron.yespdf.R
import com.aaron.yespdf.common.CommonActivity
import com.github.anzewei.parallaxbacklayout.ParallaxBack
import kotlinx.android.synthetic.main.app_activity_about.*

@ParallaxBack
class AboutActivity : CommonActivity(), IAboutView {

    private lateinit var mPresenter: IAboutPresenter

    override fun layoutId(): Int {
        return R.layout.app_activity_about
    }

    override fun createToolbar(): Toolbar? {
        return findViewById(R.id.app_toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = AboutPresenter(this)
        initView()
        mPresenter.requestMessage(
                AboutPresenter.Element.ICON_ID,
                AboutPresenter.Element.TITLE
        )
        mPresenter.requestLibrary(
                AboutPresenter.Element.LIBRARY_NAME,
                AboutPresenter.Element.LIBRARY_AUTHOR,
                AboutPresenter.Element.LIBRARY_INTRODUCE
        )
    }

    /**
     * 标题栏返回键销毁活动
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onShowMessage(list: List<Message>) {
        initMessage(list)
    }

    override fun onShowLibrary(list: List<Library>) {
        initLibrary(list)
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        initToolbar()
        initVersionStatus()
    }

    private fun initToolbar() {
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.app_ic_action_back_black)
        }
        toolbar?.setTitle(R.string.app_about)
    }

    @SuppressLint("SetTextI18n")
    private fun initVersionStatus() {
        val version = findViewById<TextView>(R.id.app_tv_version)
        val versionName = BuildConfig.VERSION_NAME
        version.text = "Version $versionName"
    }

    private fun <T> initMessage(messageList: List<T>) {
        val messageManager = LinearLayoutManager(this)
        app_rv_message.layoutManager = messageManager
        val messageAdapter: MessageAdapter<*> = MessageAdapter(this, messageList)
        app_rv_message.adapter = messageAdapter
    }

    private fun <T> initLibrary(libraryList: List<T>) {
        val libraryManager = LinearLayoutManager(this)
        app_rv_library.layoutManager = libraryManager
        val libraryAdapter: LibraryAdapter<*> = LibraryAdapter(this, libraryList)
        app_rv_library.adapter = libraryAdapter
    }

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, AboutActivity::class.java)
            context.startActivity(starter)
        }
    }
}