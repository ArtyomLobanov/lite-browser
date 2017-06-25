package ru.spbau.mit.lobanov.litebrouser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.ToggleButton;

import ru.spbau.mit.lobanov.litebrouser.TabsListAdapter.TabAdapterListener;
import ru.spbau.mit.lobanov.litebrouser.TabsPanelView.TabInfo;
import ru.spbau.mit.lobanov.litebrouser.TabsPanelView.TabsPanelListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity {

    private TabsPanelView tabsPanel;
    private ListView tabsList;
    private ViewGroup toolbar;
    private TextView addressLine;
    private MultiAutoCompleteTextView smartLine;
    private Button backButton;
    private Button newTabButton;
    private Button refreshButton;
    private ToggleButton showTabsListButton;
    private TabsListAdapter tabsListAdapter;

    private void findAllViews() {
        tabsPanel = (TabsPanelView) findViewById(R.id.web_views_panel);
        addressLine = (TextView) findViewById(R.id.address_line);
        smartLine = (MultiAutoCompleteTextView) findViewById(R.id.smart_line);
        backButton = (Button) findViewById(R.id.back_button);
        newTabButton = (Button) findViewById(R.id.new_tab_button);
        refreshButton = (Button) findViewById(R.id.refresh_button);
        showTabsListButton = (ToggleButton) findViewById(R.id.show_tabs_button);
        toolbar = (ViewGroup) findViewById(R.id.toolbar);
        tabsList = (ListView) findViewById(R.id.tabs_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();
        tabsListAdapter = new TabsListAdapter(this);
        tabsList.setAdapter(tabsListAdapter);

        EventListener eventListener = new EventListener();
        tabsListAdapter.setTabAdapterListener(eventListener);
        tabsPanel.setTabsPanelListener(eventListener);
        addressLine.setOnClickListener(eventListener);
        smartLine.setOnClickListener(eventListener);
        backButton.setOnClickListener(eventListener);
        newTabButton.setOnClickListener(eventListener);
        refreshButton.setOnClickListener(eventListener);
        showTabsListButton.setOnClickListener(eventListener);
        toolbar.setOnClickListener(eventListener);
    }

    @Override
    public void onBackPressed() {
        if (tabsPanel.canGoBack()) {
            tabsPanel.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private class EventListener implements OnClickListener, TabsPanelListener, TabAdapterListener {

        @Override
        public void onClick(View v) {
            if (v == backButton) {
                tabsPanel.goBack();
            } else if (v == newTabButton) {
                tabsPanel.newTab(getString(R.string.default_page));
            } else if (v == refreshButton) {
                //todo
            } else if (v == showTabsListButton) {
                tabsList.setVisibility(showTabsListButton.isChecked()? VISIBLE : GONE);
            } else if (v == smartLine) {
                //todo
            }
        }

        @Override
        public void onTabSelected(TabInfo tabInfo) {
            tabsPanel.setActiveTab(tabInfo.getIndex());
        }

        @Override
        public void onTabClosed(TabInfo tabInfo) {
            tabsPanel.closeTab(tabInfo.getIndex());
        }

        @Override
        public void tabsSetChanged(TabInfo[] actualData) {
            tabsListAdapter.updateData(actualData);
            backButton.setEnabled(tabsPanel.canGoBack());
            addressLine.setText(tabsPanel.getUrl());
        }
    }
}
