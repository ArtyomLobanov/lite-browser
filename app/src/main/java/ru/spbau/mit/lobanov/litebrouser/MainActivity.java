package ru.spbau.mit.lobanov.litebrouser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import ru.spbau.mit.lobanov.litebrouser.TabsPanelView.TabInfo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends Activity {

    private TabsPanelView tabManager;
    private ListView tabsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabManager = (TabsPanelView) findViewById(R.id.web_views_panel);
        if (savedInstanceState == null) {
//            tabManager.restoreState(savedInstanceState.getBundle("tabs"));
//        } else {
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
            public void onTabSelected(TabInfo tabInfo) {
                tabManager.setActiveTab(tabInfo.getIndex());
            }

            @Override
            public void onTabClosed(TabInfo tabInfo) {
                tabManager.closeTab(tabInfo.getIndex());
            }
        });
        tabManager.setDataChangeListener(new TabsPanelView.DataChangeListener() {
            @Override
            public void dataChanged(TabInfo[] actualData) {
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

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Bundle bundle = tabManager.saveState();
//        outState.putBundle("tabs", bundle);
//    }
}
