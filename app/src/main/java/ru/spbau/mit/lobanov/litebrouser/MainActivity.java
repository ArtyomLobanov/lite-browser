package ru.spbau.mit.lobanov.litebrouser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ToggleButton;

import java.io.UnsupportedEncodingException;

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
    private Mode currentMode;

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
        smartLine.setOnEditorActionListener(eventListener);
        backButton.setOnClickListener(eventListener);
        newTabButton.setOnClickListener(eventListener);
        refreshButton.setOnClickListener(eventListener);
        showTabsListButton.setOnClickListener(eventListener);
        toolbar.setOnClickListener(eventListener);
        setMode(Mode.USUAL);
    }

    @Override
    public void onBackPressed() {
        if (currentMode == Mode.SEARCH) {
            setMode(Mode.USUAL);
        } else if (tabsPanel.canGoBack()) {
            tabsPanel.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void setMode(Mode mode) {
        currentMode = mode;
        switch (mode) {
            case USUAL:
                toolbar.setVisibility(VISIBLE);
                smartLine.setVisibility(GONE);
                tabsList.setVisibility(showTabsListButton.isChecked() ? VISIBLE : GONE);
                hideKeyboard();
                break;
            case SEARCH:
                toolbar.setVisibility(GONE);
                smartLine.setVisibility(VISIBLE);
                tabsList.setVisibility(GONE);
                smartLine.setText(tabsPanel.getUrl());
                smartLine.selectAll();
        }
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            IBinder binder = getCurrentFocus().getWindowToken();
            imm.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private enum Mode {USUAL, SEARCH}

    private class EventListener implements OnClickListener, TabsPanelListener, TabAdapterListener,
            OnEditorActionListener {

        @Override
        public void onClick(View v) {
            if (v == backButton) {
                tabsPanel.goBack();
            } else if (v == newTabButton) {
                tabsPanel.newTab(getString(R.string.default_page));
            } else if (v == refreshButton) {
                //todo
            } else if (v == showTabsListButton) {
                tabsList.setVisibility(showTabsListButton.isChecked() ? VISIBLE : GONE);
            } else if (v == addressLine || v == toolbar) {
                setMode(Mode.SEARCH);
            } else if (v == tabsPanel && currentMode == Mode.SEARCH) {
                setMode(Mode.USUAL);
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

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    String url = URLHelper.createURL(smartLine.getText().toString());
                    tabsPanel.openUrl(url);
                } catch (UnsupportedEncodingException e) {
                    //todo
                }
                setMode(Mode.USUAL);
                return true;
            }
            return false;
        }
    }
}
