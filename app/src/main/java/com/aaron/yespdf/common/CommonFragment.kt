package com.aaron.yespdf.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.aaron.base.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class CommonFragment : Fragment(), View.OnTouchListener, CoroutineScope {

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    protected lateinit var activity: CommonActivity
    protected var fm: FragmentManager? = null
    protected var childfm: FragmentManager? = null

    open fun lazyLoad() {}

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && isVisible) lazyLoad() // 实现懒加载
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.activity = context as CommonActivity
        this.fm = fragmentManager
        this.childfm = childFragmentManager
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (userVisibleHint) lazyLoad() // 实现懒加载
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(this) // 防止因多个 Fragment 叠加导致点击穿透
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return true // 防止因多个 Fragment 叠加导致点击穿透
    }

    override fun startActivity(intent: Intent?) { // 如果 BaseActivity 不为空则会使用 BaseActivity 中指定的过渡动画
        activity.startActivity(intent)
    }

    fun setTransitionAnim(enter: Int, exit: Int, popEnter: Int, popExit: Int) {
        sEnter = enter
        sExit = exit
        sPopEnter = popEnter
        sPopExit = popExit
    }

    companion object {
        @AnimRes
        @AnimatorRes
        private var sEnter = -1
        @AnimRes
        @AnimatorRes
        private var sExit = -1
        @AnimRes
        @AnimatorRes
        private var sPopEnter = -1
        @AnimRes
        @AnimatorRes
        private var sPopExit = -1

        /**
         * 添加 Fragment ，原 Fragment 不被销毁并且伴随动画效果，有返回栈
         *
         * @param manager     FragmentManager
         * @param fragment    要添加的 Fragment
         * @param containerId Fragment 所属父布局 ID
         */
        fun addFragment(manager: FragmentManager, fragment: Fragment?, containerId: Int) {
            val transaction = manager.beginTransaction()
            if (sEnter != -1 || sExit != -1 || sPopEnter != -1 || sPopExit != -1) {
                transaction.setCustomAnimations(sEnter, sExit, sPopEnter, sPopExit)
            }
            transaction.add(containerId, fragment!!)
                    .addToBackStack(null)
                    .commit()
        }

        /**
         * 添加 Fragment ，原 Fragment 不被销毁并且伴随动画效果，有返回栈
         *
         * @param manager     FragmentManager
         * @param fragment    要添加的 Fragment
         * @param containerId Fragment 所属父布局 ID
         * @param anim        动画数组，顺序：enter, exit, popEnter, popExit
         */
        fun addFragment(manager: FragmentManager, fragment: Fragment?, containerId: Int, anim: IntArray) {
            val transaction = manager.beginTransaction()
            transaction.setCustomAnimations(anim[0], anim[1], anim[2], anim[3])
                    .add(containerId, fragment!!)
                    .addToBackStack(null)
                    .commit()
        }

        /**
         * 添加 Fragment ，原 Fragment 被销毁并且无动画效果，无返回栈
         *
         * @param manager       FragmentManager
         * @param fragment      要添加的 Fragment
         * @param containerId   Fragment 所属父布局 ID
         */
        fun replaceFragment(manager: FragmentManager, fragment: Fragment?, containerId: Int) {
            val transaction = manager.beginTransaction()
            transaction.replace(containerId, fragment!!).commit()
        }

        /**
         * 弹出 Fragment ，无动画效果
         *
         * @param manager  FragmentManager
         * @param fragment 要移除的 Fragment
         */
        fun popFragment(manager: FragmentManager, fragment: Fragment?) {
            val transaction = manager.beginTransaction()
            transaction.remove(fragment!!)
            transaction.commit()
        }
    }
}