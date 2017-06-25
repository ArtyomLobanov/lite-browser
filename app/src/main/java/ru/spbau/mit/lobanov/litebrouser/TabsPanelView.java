package ru.spbau.mit.lobanov.litebrouser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Артём on 24.06.2017.
 */

public class TabsPanelView extends FrameLayout {

    private static final String TABS_STATES_ARRAY_KEY = "tabs_states_array_key";
    private static final String ACTIVE_TAB_INDEX_KEY = "active_tab_index_key";
    private static final String SUPER_STATE_KEY = "super_state_key";
    private static final int CACHE_SIZE = 5;

    private final Queue<WebView> cached = new ArrayDeque<>(CACHE_SIZE);
    private final List<WebView> activeTabs = new ArrayList<>();
    private final TabWebClient tabWebClient = new TabWebClient();
    private final TabChromeClient tabChromeClient = new TabChromeClient();
    private final OnClickListener tabClickListener = new OnTabClickListener();
    private int activeTabIndex = -1;
    private TabsPanelListener tabsPanelListener;

    public TabsPanelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            for (int i = 0; i < CACHE_SIZE; i++) {
                cached.add(createWebView());
            }
        }
    }

    public void newTab(String url) {
        WebView webView;
        if (cached.isEmpty()) {
            webView = createWebView();
        } else {
            webView = cached.poll();
        }
        webView.loadUrl(url);
        activeTabs.add(webView);
        setActiveTab(activeTabs.size() - 1);
        reportTabsSetChanged();
    }

    public void closeTab(int index) {
        WebView webView = activeTabs.get(index);
        activeTabs.remove(index);
        if (index < activeTabIndex) {
            activeTabIndex--;
        } else if (index == activeTabIndex) {
            activeTabIndex = -1;
            webView.setVisibility(View.INVISIBLE);
            if (!activeTabs.isEmpty()) {
                setActiveTab(index == 0? 0 : index - 1);
            }
        }
        if (activeTabs.size() + cached.size() < CACHE_SIZE) {
            webView.clearHistory();
            cached.add(webView);
        } else {
            removeView(webView);
            webView.destroy();
        }
        reportTabsSetChanged();
    }

    public void setActiveTab(int index) {
        updateVisibility(activeTabIndex, View.INVISIBLE);
        updateVisibility(index, View.VISIBLE);
        activeTabIndex = index;
        reportTabsSetChanged();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle state = (Bundle) parcelable;
        super.onRestoreInstanceState(state.getParcelable(SUPER_STATE_KEY));
        setActiveTab(-1);
        for (WebView webView : activeTabs) {
            webView.clearHistory();
            cached.add(webView);
        }
        activeTabs.clear();
        Parcelable[] states = state.getParcelableArray(TABS_STATES_ARRAY_KEY);
        while (states.length > cached.size()) {//todo check not null
            cached.add(createWebView());
        }
        for (Parcelable tabState : states) {
            WebView webView = cached.poll();
            webView.restoreState((Bundle) tabState);
            activeTabs.add(webView);
        }
        setActiveTab(state.getInt(ACTIVE_TAB_INDEX_KEY, -1));
        while (!cached.isEmpty() && cached.size() + activeTabs.size() > CACHE_SIZE) {
            cached.poll().destroy();
        }
        reportTabsSetChanged();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());
        Bundle[] tabsStates = new Bundle[activeTabs.size()];
        for (int i = 0; i < tabsStates.length; i++) {
            tabsStates[i] = new Bundle();
            activeTabs.get(i).saveState(tabsStates[i]);
        }
        state.putParcelableArray(TABS_STATES_ARRAY_KEY, tabsStates);
        state.putInt(ACTIVE_TAB_INDEX_KEY, activeTabIndex);
        return state;
    }

    public TabInfo[] currentTabs() {
        TabInfo[] tabs = new TabInfo[activeTabs.size()];
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabInfo(activeTabs.get(i), i);
        }
        return tabs;
    }

    public void setTabsPanelListener(TabsPanelListener listener) {
        tabsPanelListener = listener;
    }

    public void openUrl(String url) {
        if (activeTabs.isEmpty()) {
            newTab(url);
        } else {
            activeTabs.get(activeTabIndex).loadUrl(url);
        }
    }

    public boolean canGoBack() {
        return !activeTabs.isEmpty() && activeTabs.get(0).canGoBack();
    }

    public void goBack() {
        activeTabs.get(0).goBack();
        reportTabsSetChanged();
    }

    public String getUrl() {
        return activeTabIndex == -1? "" : activeTabs.get(activeTabIndex).getUrl();
    }

    private void reportTabsSetChanged() {
        if (tabsPanelListener != null) {
            tabsPanelListener.tabsSetChanged(currentTabs());
        }
    }

    private void updateVisibility(int index, int visibility) {
        if (index != -1) {
            activeTabs.get(index).setVisibility(visibility);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView() {
        WebView webView = (WebView) View.inflate(getContext(), R.layout.view_tab, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(tabWebClient);
        webView.setWebChromeClient(tabChromeClient);
        webView.setOnClickListener(tabClickListener);
        addView(webView);
        return webView;
    }

    private class TabWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
            view.loadUrl(url.getUrl().toString());
            reportTabsSetChanged();
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            reportTabsSetChanged();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            reportTabsSetChanged();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            reportTabsSetChanged();
        }
    }

    private class TabChromeClient extends WebChromeClient {

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            reportTabsSetChanged();
        }
    }

    private class OnTabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            callOnClick();
        }
    }

    public class TabInfo {
        private final String name;
        private final String address;
        private final int index;

        private TabInfo(WebView webView, int index) {
            name = webView.getTitle();
            address = webView.getUrl();
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public int getIndex() {
            return index;
        }
    }

    interface TabsPanelListener {
        void tabsSetChanged(TabInfo[] actualData);
    }
}
