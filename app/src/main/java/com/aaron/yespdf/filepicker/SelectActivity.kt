package com.aaron.yespdf.filepicker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.aaron.base.impl.TextWatcherImpl
import com.aaron.base.util.StatusBarUtils
import com.aaron.yespdf.R
import com.aaron.yespdf.R2
import com.aaron.yespdf.common.CommonActivity
import com.blankj.utilcode.util.KeyboardUtils
import com.github.anzewei.parallaxbacklayout.ParallaxBack
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.app_activity_select.*
import kotlinx.android.synthetic.main.app_include_searchview.*
import java.util.*
import kotlin.math.sqrt

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@ParallaxBack
class SelectActivity : CommonActivity() {

    val ibtnSelectAll: ImageButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ImageButton>(R.id.app_ibtn_check)
    }
    val ibtnSearch: ImageButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ImageButton>(R.id.app_ibtn_search)
    }
    val ibtnInverse: ImageButton by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ImageButton>(R.id.app_ibtn_inverse)
    }
    val etSearch: EditText by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<EditText>(R.id.app_et_search)
    }
    var importeds: List<String>? = null
    // 揭露动画参数
    private val duration = 250
    private var centerX = 0
    private var centerY = 0
    private var radius = 0f
    private var viewAllFragment: ViewAllFragment? = null
    private var fragmentPagerAdapter: FragmentPagerAdapter? = null

    fun setRevealParam() {
        app_search_view.post {
            centerX = app_ibtn_search.left + app_ibtn_search.measuredWidth / 2
            centerY = app_ibtn_search.top + app_ibtn_search.measuredHeight / 2
            val width = centerX * 2
            val height = app_search_view.measuredHeight
            radius = (sqrt(width * width + height * height.toDouble()) / 2).toFloat()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        toolbar?.setPadding(0, 0, 0, 0)
        StatusBarUtils.setStatusBarLight(this, true)
        initView()
    }

    override fun onStop() {
        super.onStop()
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val list = supportFragmentManager.fragments
        for (f in list) {
            if (f is ViewAllFragment) viewAllFragment = f
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        if (app_search_view.visibility == View.VISIBLE) {
            closeSearchView()
            viewAllFragment?.setFocus()
        } else {
            super.onBackPressed()
        }
    }

    override fun layoutId(): Int {
        return R.layout.app_activity_select
    }

    override fun createToolbar(): Toolbar? {
        return findViewById(R.id.app_toolbar)
    }

    private fun initView() {
        val data = intent
        importeds = data.getStringArrayListExtra(EXTRA_IMPORTED)
        app_ibtn_back.setOnClickListener { closeSearchView() }
        app_ibtn_search.setOnClickListener { openSearchView() }
        app_ibtn_inverse.setOnClickListener {
            if (KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.hideSoftInput(this)
            }
            app_ibtn_inverse.isSelected = !app_ibtn_inverse.isSelected
            viewAllFragment?.adapter?.setInverse(app_ibtn_inverse.isSelected)
            viewAllFragment?.adapter?.filter?.filter(app_et_search.text)
        }
        app_ibtn_clear.setOnClickListener {
            app_et_search.setText("")
            if (!KeyboardUtils.isSoftInputVisible(this)) {
                KeyboardUtils.showSoftInput(this)
            }
        }
        app_et_search.addTextChangedListener(object : TextWatcherImpl() {
            override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
                app_ibtn_inverse.visibility = if (c.isEmpty()) View.GONE else View.VISIBLE
                app_ibtn_clear.visibility = if (c.isEmpty()) View.GONE else View.VISIBLE
                viewAllFragment?.adapter?.filter?.filter(c)
            }
        })
        app_tab_layout.setupWithViewPager(app_vp)
        fragmentPagerAdapter = SelectFragmentAdapter(supportFragmentManager)
        app_vp.adapter = fragmentPagerAdapter
    }

    private fun initToolbar() {
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.app_ic_action_back_black)
        }
        toolbar?.setTitle(R.string.app_import_file)
    }

    private fun openSearchView() {
        val animator = ViewAnimationUtils.createCircularReveal(app_search_view, centerX, centerY, 0f, radius)
        animator.duration = duration.toLong()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                app_search_view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                app_et_search.requestFocus()
                KeyboardUtils.showSoftInput(this@SelectActivity)
            }
        })
        animator.start()
    }

    fun closeSearchView() {
        val animator = ViewAnimationUtils.createCircularReveal(app_search_view, centerX, centerY, radius, 0f)
        animator.duration = duration.toLong()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (KeyboardUtils.isSoftInputVisible(this@SelectActivity)) {
                    KeyboardUtils.hideSoftInput(this@SelectActivity)
                }
                app_search_view.visibility = View.GONE
            }
        })
        animator.start()
    }

    companion object {
        const val EXTRA_SELECTED = "EXTRA_SELECTED"
        const val EXTRA_TYPE = "EXTRA_TYPE"
        const val EXTRA_GROUP_NAME = "EXTRA_GROUP_NAME"
        const val TYPE_TO_EXIST = 1
        const val TYPE_BASE_FOLDER = 2
        const val TYPE_CUSTOM = 3
        const val REQUEST_CODE = 111
        const val EXTRA_IMPORTED = "EXTRA_IMPORTED"

        fun start(activity: Activity, requestCode: Int, imported: ArrayList<String?>?) {
            val starter = Intent(activity, SelectActivity::class.java)
            starter.putStringArrayListExtra(EXTRA_IMPORTED, imported)
            activity.startActivityForResult(starter, requestCode)
        }
    }
}