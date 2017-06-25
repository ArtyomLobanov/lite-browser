package ru.spbau.mit.lobanov.litebrouser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity {

    private TabManager tabManager;
    private ListView tabsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.web_views_panel);

        tabManager = new TabManager(frameLayout, 5);
        if (savedInstanceState != null) {
            tabManager.restoreState(savedInstanceState.getBundle("tabs"));
        } else {
            tabManager.newTab("http://www.google.com");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");
            tabManager.newTab("http://www.ya.a.ru");

        }
        Button button1 = (Button) findViewById(R.id.back);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabManager.setActiveTab(0);
            }
        });
        Button button2 = (Button) findViewById(R.id.refresh);
        final TabAdapter tabAdapter = new TabAdapter(this);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabManager.setActiveTab(2);
                tabAdapter.updateData(tabManager.currentTabs());
            }
        });
        tabsList = (ListView) findViewById(R.id.tabs_list);
        Button button3 = (Button) findViewById(R.id.tabs);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabsList.getVisibility() == VISIBLE) {
                    tabsList.setVisibility(GONE);
                } else {
                    tabsList.setVisibility(VISIBLE);
                }
            }
        });
        tabsList.setAdapter(tabAdapter);
        tabAdapter.updateData(tabManager.currentTabs());
        tabAdapter.setTabAdapterListener(new TabAdapter.TabAdapterListener() {
            @Override
            public void onTabSelected(TabManager.TabInfo tabInfo) {
                tabManager.setActiveTab(tabInfo.getIndex());
            }

            @Override
            public void onTabClosed(TabManager.TabInfo tabInfo) {
                tabManager.closeTab(tabInfo.getIndex());
            }
        });
        tabManager.setDataChangeListener(new TabManager.DataChangeListener() {
            @Override
            public void dataChanged(TabManager.TabInfo[] actualData) {
                tabAdapter.updateData(actualData);
            }
        });
        Button newTab = (Button) findViewById(R.id.new_tab_button);
        newTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabManager.newTab("https://www.ya.ru/");
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = tabManager.saveState();
        outState.putBundle("tabs", bundle);
    }

    public class myWebClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest url) {
            view.loadUrl(url.getUrl().toString());
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            throw new RuntimeException("error");
        }
    }
}
