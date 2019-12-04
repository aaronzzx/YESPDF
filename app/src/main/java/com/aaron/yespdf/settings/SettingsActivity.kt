package com.aaron.yespdf.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.aaron.yespdf.R
import com.aaron.yespdf.R2
import com.aaron.yespdf.common.CommonActivity
import com.github.anzewei.parallaxbacklayout.ParallaxBack
import com.github.anzewei.parallaxbacklayout.ParallaxHelper
import kotlinx.android.synthetic.main.app_activity_settings.*

@ParallaxBack
class SettingsActivity : CommonActivity() {

    override fun layoutId(): Int {
        return R.layout.app_activity_settings
    }

    override fun createToolbar(): Toolbar? {
        return findViewById(R.id.app_toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent
        disableSwipeBack(data != null && data.getBooleanExtra(EXTRA_FROM_PREVIEW, false))
        initView()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    private fun disableSwipeBack(disable: Boolean) {
        if (disable) {
            ParallaxHelper.disableParallaxBack(this)
        }
    }

    private fun initView() {
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.app_ic_action_back_black)
        }
        toolbar?.setTitle(R.string.app_settings)
        val lm = LinearLayoutManager(this)
        app_rv_settings.layoutManager = lm
        app_rv_settings.adapter = SettingsAdapter()
    }

    companion object {
        private const val EXTRA_FROM_PREVIEW = "EXTRA_FROM_PREVIEW"
        fun start(context: Context) {
            val starter = Intent(context, SettingsActivity::class.java)
            context.startActivity(starter)
        }

        @JvmStatic
        fun start(activity: Activity, requestCode: Int) {
            val starter = Intent(activity, SettingsActivity::class.java)
            starter.putExtra(EXTRA_FROM_PREVIEW, true)
            activity.startActivityForResult(starter, requestCode)
        }
    }
}