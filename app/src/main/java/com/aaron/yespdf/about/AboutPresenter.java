package com.aaron.yespdf.about;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;

import java.util.ArrayList;
import java.util.List;

class AboutPresenter implements IAboutContract.P {

    private List<Message> mMessageList = new ArrayList<>();
    private List<Library> mLibraryList = new ArrayList<>();

    private IAboutContract.V<Message, Library> mView;

    AboutPresenter(IAboutContract.V<Message, Library> view) {
        mView = view;
    }

    @Override
    public void detachView() {
        mView = null;
    }

    @Override
    public void requestMessage(int[] iconId, String[] title) {
        if (iconId.length != 0 && iconId.length == title.length) {
            for (int i = 0; i < iconId.length; i++) {
                Message message = new Message(iconId[i], title[i]);
                mMessageList.add(message);
            }
            mView.onShowMessage(mMessageList);
        }
    }

    @Override
    public void requestLibrary(String[] name, String[] author, String[] introduce) {
        if (name.length == 0 || author.length == 0 || introduce.length == 0) {
            return;
        }
        if (name.length == author.length && name.length == introduce.length) {
            for (int i = 0; i < name.length; i++) {
                Library library = new Library(name[i], author[i], introduce[i]);
                mLibraryList.add(library);
            }
            mView.onShowLibrary(mLibraryList);
        }
    }

    static class Element {
        static final String[] TITLE = {App.getContext().getString(R.string.app_introduce), App.getContext().getString(R.string.app_feedback), App.getContext().getString(R.string.app_source_code), App.getContext().getString(R.string.app_github), App.getContext().getString(R.string.app_gift)};

        static final int[] ICON_ID = {
                R.drawable.app_ic_introduce,
                R.drawable.app_ic_email,
                R.drawable.app_ic_source_code,
                R.drawable.app_ic_github,
                R.drawable.app_ic_gift};

        static final String[] LIBRARY_NAME = {
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
                "AndroidPdfViewer"};

        static final String[] LIBRARY_AUTHOR = {
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
                "barteksc"};

        static final String[] LIBRARY_INTRODUCE = {
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
                "Android view for displaying PDFs rendered with PdfiumAndroid"};
    }
}
