package com.aaron.yespdf.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.Settings;
import com.aaron.yespdf.common.UiManager;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_COUNT = 3;

    private static final int TYPE_SWITCH = 0;
    private static final int TYPE_SEEKBAR = 1;

    private static final int POS_COLOR_REVERSE  = 0;
    private static final int POS_VOLUME_CONTROL = 1;
    private static final int POS_SCROLL_LEVEL = 2;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SWITCH) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_settings_switch, parent, false);
            SwitchHolder holder = new SwitchHolder(itemView);
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                holder.switcher.setChecked(!holder.switcher.isChecked());
                tapOptions(holder.switcher, pos);
            });
            return holder;
        }
        View itemView = inflater.inflate(R.layout.app_recycler_item_settings_seekbar, parent, false);
        SeekbarHolder holder = new SeekbarHolder(itemView);
        holder.seekBar.setMax(9);
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int level = seekBar.getProgress() + 1;
                UiManager.showShort(context.getString(R.string.app_cur_level_is) + level);
                Settings.setScrollLevel(level);
            }
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SwitchHolder) {
            SwitchHolder holder = (SwitchHolder) viewHolder;
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
        } else if (viewHolder instanceof SeekbarHolder) {
            SeekbarHolder holder = (SeekbarHolder) viewHolder;
            holder.tvTitle.setText(R.string.app_auto_scroll);
            holder.seekBar.setProgress((int) Settings.getScrollLevel() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == POS_SCROLL_LEVEL) {
            return TYPE_SEEKBAR;
        }
        return TYPE_SWITCH;
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    static class SwitchHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Switch switcher;

        SwitchHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            switcher = itemView.findViewById(R.id.app_switch);
        }
    }

    static class SeekbarHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        SeekBar seekBar;

        SeekbarHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            seekBar = itemView.findViewById(R.id.app_sb_scroll_level);
        }
    }
}
