package com.zq.meizi.module.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zq.meizi.Constant;
import com.zq.meizi.R;
import com.zq.meizi.adapter.base.BasePagerAdapter;
import com.zq.meizi.bean.news.NewsChannelBean;
import com.zq.meizi.database.dao.NewsChannelDao;
import com.zq.meizi.module.base.BaseListFragment;
import com.zq.meizi.module.joke.content.JokeContentView;
import com.zq.meizi.module.news.article.NewsArticleView;
import com.zq.meizi.module.news.channel.NewsChannelActivity;
import com.zq.meizi.module.wenda.article.WendaArticleView;
import com.zq.meizi.utils.RxBus;
import com.zq.meizi.utils.SettingUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Created by steven on 2018/2/18.
 */

public class NewsTabLayout extends Fragment {

    public static final String TAG = "NewsTabLayout";
    private static NewsTabLayout instance = null;
    private ViewPager viewPager;
    private BasePagerAdapter adapter;
    private LinearLayout linearLayout;
    private NewsChannelDao dao = new NewsChannelDao();
    private List<Fragment> fragmentList;
    private List<String> titleList;
    private Observable<Boolean> observable;
    private Map<String, Fragment> map = new HashMap<>();



    public static NewsTabLayout getInstance() {
        if (instance == null) {
            instance = new NewsTabLayout();
        }
        return instance;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_tab, container, false);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        linearLayout.setBackgroundColor(SettingUtil.getInstance().getColor());
    }


    private void initView(View view) {
        TabLayout tab_layout = view.findViewById(R.id.tab_layout_news);
        viewPager = view.findViewById(R.id.view_pager_news);

        tab_layout.setupWithViewPager(viewPager);
        tab_layout.setTabMode(TabLayout.MODE_SCROLLABLE);
        ImageView add_channel_iv = view.findViewById(R.id.add_channel_iv);
        add_channel_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NewsChannelActivity.class));
            }
        });
        linearLayout = view.findViewById(R.id.header_layout);
        linearLayout.setBackgroundColor(SettingUtil.getInstance().getColor());
    }


    private void initData() {
        initTabs();
        adapter = new BasePagerAdapter(getChildFragmentManager(), fragmentList, titleList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(15);

        observable = RxBus.getInstance().register(NewsTabLayout.TAG);
        observable.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isRefresh) throws Exception {
                if (isRefresh) {
                    initTabs();
                    adapter.recreateItems(fragmentList, titleList);
                }
            }
        });
    }

    private void initTabs() {
        List<NewsChannelBean> channelList = dao.query(1);
        fragmentList = new ArrayList<>();
        titleList = new ArrayList<>();
        if (channelList.size() == 0) {
            dao.addInitData();
            channelList = dao.query(Constant.NEWS_CHANNEL_ENABLE);
        }

        for (NewsChannelBean bean : channelList) {

            Fragment fragment = null;
            String channelId = bean.getChannelId();

            switch (channelId) {
                case "essay_joke":
                    if (map.containsKey(channelId)) {
                        fragmentList.add(map.get(channelId));
                    } else {
                        fragment = JokeContentView.newInstance();
                        fragmentList.add(fragment);
                    }

                    break;
                case "question_and_answer":
                    if (map.containsKey(channelId)) {
                        fragmentList.add(map.get(channelId));
                    } else {
                        fragment = WendaArticleView.newInstance();
                        fragmentList.add(fragment);
                    }

                    break;
                default:
                    if (map.containsKey(channelId)) {
                        fragmentList.add(map.get(channelId));
                    } else {
                        fragment = NewsArticleView.newInstance(channelId);
                        fragmentList.add(fragment);
                    }
                    break;
            }

            titleList.add(bean.getChannelName());

            if (fragment != null) {
                map.put(channelId, fragment);
            }
        }
    }

    public void onDoubleClick() {
        if (titleList != null && titleList.size() > 0 && fragmentList != null && fragmentList.size() > 0) {
            int item = viewPager.getCurrentItem();
            ((BaseListFragment) fragmentList.get(item)).onRefresh();
        }
    }


}
