package com.aaron.yespdf.filepicker;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
abstract class AbstractAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    abstract void selectAll(boolean selectAll);

    abstract boolean reset();
}
