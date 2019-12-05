package com.aaron.yespdf.common

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class CommonActivity : AppCompatActivity(), CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    protected var toolbar: Toolbar? = null

    @LayoutRes
    protected abstract fun layoutId(): Int

    protected abstract fun createToolbar(): Toolbar?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId())
        toolbar = createToolbar()
        toolbar?.run {
            setSupportActionBar(this)
            UiManager.setStatusBar(this@CommonActivity, this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        init {
            // 设置启用 5.0 以下版本对矢量图形的支持
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}