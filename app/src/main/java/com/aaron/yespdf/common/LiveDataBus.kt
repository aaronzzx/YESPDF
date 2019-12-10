package com.aaron.yespdf.common

import androidx.lifecycle.*

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@Suppress("UNCHECKED_CAST")
object LiveDataBus {

    private val bus: MutableMap<String, BusMutableLiveData<Any>> = mutableMapOf()

    fun <T> with(key: String): BusMutableLiveData<T> {
        if (!bus.containsKey(key)) {
            bus[key] = BusMutableLiveData()
        }
        return bus[key] as BusMutableLiveData<T>
    }

    class BusMutableLiveData<T> : MutableLiveData<T>() {
        private val observerMap: MutableMap<Observer<*>, Observer<*>> = mutableMapOf()

        fun observeSticky(
            owner: () -> Lifecycle,
            observer: (T) -> Unit
        ) {
            super.observe(owner, observer)
        }

        fun observeForeverSticky(observer: Observer<in T>) {
            super.observeForever(observer)
        }

        override fun observeForever(observer: Observer<in T>) {
            if (!observerMap.containsKey(observer)) {
                observerMap[observer] = ObserverWrapper(observer)
            }
            super.observeForever(observerMap[observer] as Observer<in T>)
        }

        override fun removeObserver(observer: Observer<in T>) {
            val realObserver: Observer<in T> =
                if (observerMap.containsKey(observer)) {
                    observerMap.remove(observer) as Observer<in T>
                } else {
                    observer
                }
            super.removeObserver(realObserver)
        }

        override fun observe(
            owner: LifecycleOwner,
            observer: Observer<in T>
        ) {
            super.observe(owner, observer)
            try {
                hook(observer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 利用反射将 LiveData 的 mVersion 赋值给 ObserverWrapper 的 mLastVersion
         */
        @Throws(Exception::class)
        private fun hook(observer: Observer<*>) {
            // Get wrapper's version.
            val liveDataClass = LiveData::class.java
            // SafeIterableMap<Observer<? super T>, ObserverWrapper> mObservers
            val observersField = liveDataClass.getDeclaredField("mObservers")
            observersField.isAccessible = true
            val observers = observersField[this]
            val observersClass: Class<*> = observers.javaClass
            // It's mObservers's get method.
            val methodGet = observersClass.getDeclaredMethod("get", Any::class.java)
            methodGet.isAccessible = true
            val observerWrapperEntry = methodGet.invoke(observers, observer)
            var observerWrapper: Any? = null
            if (observerWrapperEntry is Map.Entry<*, *>) {
                // Now we got observerWrapper.
                observerWrapper = observerWrapperEntry.value
            }
            if (observerWrapper == null) {
                throw NullPointerException("Wrapper can not be null!")
            }
            val observerWrapperParentClass: Class<*>? = observerWrapper.javaClass.superclass
            val lastVersionField = observerWrapperParentClass!!.getDeclaredField("mLastVersion")
            lastVersionField.isAccessible = true
            // Get livedata's version.
            val versionField = liveDataClass.getDeclaredField("mVersion")
            versionField.isAccessible = true
            val version = versionField[this]
            // Set wrapper's version.
            lastVersionField[observerWrapper] = version
        }
    }

    private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {
        override fun onChanged(t: T) {
            if (isCallOnObserverForever()) {
                return
            }
            observer.onChanged(t)
        }

        private fun isCallOnObserverForever(): Boolean {
            val stackTrace = Thread.currentThread().stackTrace
            for (element in stackTrace) {
                if ("androidx.lifecycle.LiveData" == element.className
                    && "observeForever" == element.methodName
                ) {
                    return true
                }
            }
            return false
        }
    }
}