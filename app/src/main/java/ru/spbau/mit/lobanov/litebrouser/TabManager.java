package ru.spbau.mit.lobanov.litebrouser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Артём on 24.06.2017.
 */

public class TabManager {

    private static final String TABS_STATES_ARRAY_KEY = "tabs_states_array_key";
    private static final String ACTIVE_TAB_INDEX_KEY = "active_tab_index_key";

    private final Queue<WebView> cached = new ArrayDeque<>();
    private final List<WebView> activeTabs = new ArrayList<>();
    private final TabWebClient tabWebClient = new TabWebClient();
    private final TabChromeClient tabChromeClient = new TabChromeClient();
    private final ViewGroup panel;
    private final int cacheSize;
    private int activeTabIndex = -1;

    public TabManager(ViewGroup panel, int cacheSize) {
        this.cacheSize = cacheSize;
        this.panel = panel;
        for (int i = 0; i < cacheSize; i++) {
            cached.add(createWebView());
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
        if (activeTabs.size() + cached.size() < cacheSize) {
            webView.clearHistory();
            cached.add(webView);
        } else {
            panel.removeView(webView);
            webView.destroy();
        }
    }

    public void setActiveTab(int index) {
        updateVisibility(activeTabIndex, View.INVISIBLE);
        updateVisibility(index, View.VISIBLE);
        activeTabIndex = index;
    }

    public Bundle saveState() {
        Bundle bundle = new Bundle();
        Bundle[] states = new Bundle[activeTabs.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = new Bundle();
            activeTabs.get(i).saveState(states[i]);
        }
        bundle.putParcelableArray(TABS_STATES_ARRAY_KEY, states);
        bundle.putInt(ACTIVE_TAB_INDEX_KEY, activeTabIndex);
        return bundle;
    }

    public void restoreState(Bundle bundle) {
        setActiveTab(-1);
        for (WebView webView : activeTabs) {
            webView.clearHistory();
            cached.add(webView);
        }
        activeTabs.clear();
        Parcelable[] states = bundle.getParcelableArray(TABS_STATES_ARRAY_KEY);
        while (states.length > cached.size()) {//todo check not null
            cached.add(createWebView());
        }
        for (Parcelable state : states) {
            WebView webView = cached.poll();
            webView.restoreState((Bundle) state);
            activeTabs.add(webView);
        }
        setActiveTab(bundle.getInt(ACTIVE_TAB_INDEX_KEY, -1));
        while (!cached.isEmpty() && cached.size() + activeTabs.size() > cacheSize) {
            cached.poll().destroy();
        }
    }

    public TabInfo[] currentTabs() {
        TabInfo[] tabs = new TabInfo[activeTabs.size()];
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TabInfo(activeTabs.get(i));
        }
        return tabs;
    }

    private void updateVisibility(int index, int visibility) {
        if (index != -1) {
            activeTabs.get(index).setVisibility(visibility);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView() {
        WebView webView = (WebView) View.inflate(panel.getContext(), R.layout.view_tab, null);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(tabWebClient);
        webView.setWebChromeClient(tabChromeClient);
        panel.addView(webView);
        return webView;
    }

    private class TabWebClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
            view.loadUrl(url.getUrl().toString());
            return true;
        }
    }

    private class TabChromeClient extends WebChromeClient {
        //todo support custom views
    }

    public static class TabInfo {
        private final String name;
        private final String address;

        private TabInfo(WebView webView) {
            name = webView.getTitle();
            address = webView.getUrl();
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }
}
