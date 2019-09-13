package com.aaron.yespdf.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.Settings;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private static final int ITEM_COUNT = 2;

    private static final int POS_COLOR_REVERSE  = 0;
    private static final int POS_VOLUME_CONTROL = 1;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.app_recycler_item_settings, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            holder.switcher.setChecked(!holder.switcher.isChecked());
            tapOptions(holder.switcher, pos);
        });
        return holder;
    }

    private void tapOptions(Switch switcher, int pos) {
        switch (pos) {
            case POS_COLOR_REVERSE:
                Settings.setNightMode(switcher.isChecked());
                break;
            case POS_VOLUME_CONTROL:
                Settings.setVolumeControl(switcher.isChecked());
                break;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        switch (position) {
            case 0:
                holder.tvTitle.setText(R.string.app_color_reverse);
                holder.switcher.setChecked(Settings.isNightMode());
                break;
            case 1:
                holder.tvTitle.setText(R.string.app_volume_control);
                holder.switcher.setChecked(Settings.isVolumeControl());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Switch switcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            switcher = itemView.findViewById(R.id.app_switch);
        }
    }
}
