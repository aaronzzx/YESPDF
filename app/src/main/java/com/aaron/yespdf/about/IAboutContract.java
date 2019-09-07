package com.aaron.yespdf.about;

import java.util.List;

/**
 * @author Aaron aaronzheng9603@gmail.com
 */
interface IAboutContract {

    interface V<A, B> {
        void attachPresenter();

        void onShowMessage(List<A> list);

        void onShowLibrary(List<B> list);
    }

    interface P {
        void detachView();

        void requestMessage(int[] iconId, String[] title);

        void requestLibrary(String[] name, String[] author, String[] introduce);
    }
}
