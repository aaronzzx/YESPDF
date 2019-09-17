package com.aaron.yespdf.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;
import com.aaron.yespdf.common.Settings;
import com.aaron.yespdf.common.UiManager;
import com.aaron.yespdf.common.event.MaxRecentEvent;
import com.blankj.utilcode.util.ConvertUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_COUNT = 5;

    private static final int TYPE_SWITCH = 0;
    private static final int TYPE_SEEKBAR = 1;
    private static final int TYPE_NUM_PICKER = 2;

    private static final int POS_COLOR_REVERSE = 0;
    private static final int POS_VOLUME_CONTROL = 1;
    private static final int POS_SHOW_STATUS_BAR = 2;
    private static final int POS_NUM_PICKER = 3;
    private static final int POS_SCROLL_LEVEL = 4;

    private MaxRecentEvent mMaxRecentEvent;
    private List<String> mMaxRecentCounts;

    SettingsAdapter() {
        mMaxRecentEvent = new MaxRecentEvent();
        String[] array = App.getContext().getResources().getStringArray(R.array.max_recent_count);
        mMaxRecentCounts = Arrays.asList(array);
    }

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
        } else if (viewType == TYPE_NUM_PICKER) {
            View itemView = inflater.inflate(R.layout.app_recycler_item_settings_recent_count, parent, false);
            MaxRecentHolder holder = new MaxRecentHolder(itemView);
            holder.itemView.setOnClickListener(v -> holder.spinner.performClick());
            holder.spinner.setDropDownHorizontalOffset(-ConvertUtils.dp2px(46));
            holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String maxRecentCount = mMaxRecentCounts.get(position);
                    holder.tvCount.setText(maxRecentCount);
                    Settings.setMaxRecentCount(maxRecentCount);
                    EventBus.getDefault().post(mMaxRecentEvent);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
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
            case POS_SHOW_STATUS_BAR:
                Settings.setShowStatusBar(switcher.isChecked());
                break;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SwitchHolder) {
            SwitchHolder holder = (SwitchHolder) viewHolder;
            switch (position) {
                case POS_COLOR_REVERSE:
                    holder.tvTitle.setText(R.string.app_color_reverse);
                    holder.switcher.setChecked(Settings.isNightMode());
                    break;
                case POS_VOLUME_CONTROL:
                    holder.tvTitle.setText(R.string.app_volume_control);
                    holder.switcher.setChecked(Settings.isVolumeControl());
                    break;
                case POS_SHOW_STATUS_BAR:
                    holder.tvTitle.setText(R.string.app_show_status_bar);
                    holder.switcher.setChecked(Settings.isShowStatusBar());
                    break;
            }
        } else if (viewHolder instanceof SeekbarHolder) {
            SeekbarHolder holder = (SeekbarHolder) viewHolder;
            holder.tvTitle.setText(R.string.app_scroll_velocity);
            holder.seekBar.setProgress((int) Settings.getScrollLevel() - 1);
        } else if (viewHolder instanceof MaxRecentHolder) {
            MaxRecentHolder holder = (MaxRecentHolder) viewHolder;
            holder.tvTitle.setText(R.string.app_max_recent_count);
            holder.tvCount.setText(Settings.getMaxRecentCount());
            holder.spinner.setSelection(mMaxRecentCounts.indexOf(Settings.getMaxRecentCount()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == POS_SCROLL_LEVEL) {
            return TYPE_SEEKBAR;
        } else if (position == POS_NUM_PICKER) {
            return TYPE_NUM_PICKER;
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

    static class MaxRecentHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvCount;
        Spinner spinner;

        MaxRecentHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.app_tv_title);
            tvCount = itemView.findViewById(R.id.app_tv_count);
            spinner = itemView.findViewById(R.id.app_spinner);
        }
    }
}
