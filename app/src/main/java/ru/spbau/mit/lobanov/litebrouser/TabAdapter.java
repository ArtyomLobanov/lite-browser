package ru.spbau.mit.lobanov.litebrouser;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ru.spbau.mit.lobanov.litebrouser.TabManager.TabInfo;

/**
 * Created by Артём on 25.06.2017.
 */

public class TabAdapter extends ArrayAdapter<TabInfo> {

    private final Context context;
    private SelectionListener selectionListener;

    public TabAdapter(@NonNull Context context) {
        super(context, R.layout.view_tab_info);
        this.context = context;
    }

    public void updateData(TabInfo[] data) {
        clear();
        addAll(data);
        notifyDataSetChanged();
    }

    public void setSelectionListener(SelectionListener listener) {
        selectionListener = listener;
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
        private TabInfo item;

        private TabViewHolder() {
            view = View.inflate(context, R.layout.view_tab_info, null);
            view.setTag(this);
            nameView = (TextView) view.findViewById(R.id.name);
            addressView = (TextView) view.findViewById(R.id.address);
            view.setOnClickListener(this);
            nameView.setOnClickListener(this);
            addressView.setOnClickListener(this);
        }

        private void setItem(TabInfo tabInfo) {
            item = tabInfo;
            nameView.setText(tabInfo.getName());
            addressView.setText(tabInfo.getAddress());
        }

        @Override
        public void onClick(View v) {
            if (selectionListener != null) {
                selectionListener.onTabSelected(item);
            }
        }
    }

    interface SelectionListener {
        void onTabSelected(TabInfo tabInfo);
    }
}
