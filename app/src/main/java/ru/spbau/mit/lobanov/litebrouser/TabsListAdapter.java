package ru.spbau.mit.lobanov.litebrouser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import ru.spbau.mit.lobanov.litebrouser.TabsPanelView.TabInfo;

/**
 * Created by Артём on 25.06.2017.
 */

public class TabsListAdapter extends ArrayAdapter<TabInfo> {

    private final Context context;
    private TabAdapterListener tabAdapterListener;

    public TabsListAdapter(@NonNull Context context) {
        super(context, R.layout.view_tab_info);
        this.context = context;
    }

    public void updateData(TabInfo[] data) {
        clear();
        addAll(data);
        notifyDataSetChanged();
    }

    public void setTabAdapterListener(TabAdapterListener listener) {
        tabAdapterListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TabInfo item = getItem(position);
        TabViewHolder holder;
        if (convertView == null) {
            holder = new TabViewHolder();
        } else {
            holder = (TabViewHolder) convertView.getTag();
        }
        holder.setItem(item);
        return holder.view;
    }

    private class TabViewHolder implements View.OnClickListener {
        private final View view;
        private final TextView nameView;
        private final TextView addressView;
        private final Button closeButton;
        private TabInfo item;

        private TabViewHolder() {
            view = View.inflate(context, R.layout.view_tab_info, null);
            view.setTag(this);
            nameView = (TextView) view.findViewById(R.id.name_view);
            addressView = (TextView) view.findViewById(R.id.address_view);
            closeButton = (Button) view.findViewById(R.id.close_butoon);
            view.setOnClickListener(this);
            nameView.setOnClickListener(this);
            addressView.setOnClickListener(this);
            closeButton.setOnClickListener(this);
        }

        private void setItem(TabInfo tabInfo) {
            item = tabInfo;
            nameView.setText(tabInfo.getName());
            addressView.setText(tabInfo.getAddress());
        }

        @Override
        public void onClick(View v) {
            if (tabAdapterListener == null) {
                return;
            }
            if (v == closeButton) {
                tabAdapterListener.onTabClosed(item);
            } else{
                tabAdapterListener.onTabSelected(item);
            }
        }
    }

    interface TabAdapterListener {
        void onTabSelected(TabInfo tabInfo);
        void onTabClosed(TabInfo tabInfo);
    }
}
