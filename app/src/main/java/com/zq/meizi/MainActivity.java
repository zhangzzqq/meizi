package com.zq.meizi;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.zq.meizi.module.news.NewsTabLayout;
import com.zq.meizi.module.search.SearchActivity;
import com.zq.meizi.utils.SettingUtil;
import com.zq.meizi.widget.helper.BottomNavigationViewHelper;


public class MainActivity extends AppCompatActivity {

    private static final int FRAGMENT_NEWS = 0;
    private static final int FRAGMENT_PHOTO = 1;
    private static final int FRAGMENT_VIDEO = 2;
    private static final int FRAGMENT_MEDIA = 3;
    private static final String TAG = "MainActivity";
    private static final String POSITION = "position";
    private static final String SELECT_ITEM = "bottomNavigationSelectItem";
    private long exitTime = 0;
    private long firstClickTime = 0;
    private int position;
    private Toolbar toolbar;

    private NewsTabLayout newsTabLayout;
    private BottomNavigationView bottomNavigationView;
    //    private PhotoTabLayout photoTabLayout;
//    private VideoTabLayout videoTabLayout;
//    private MediaChannelView mediaChannelView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if (savedInstanceState != null) {
            newsTabLayout = (NewsTabLayout) getSupportFragmentManager().findFragmentByTag(NewsTabLayout.class.getName());
//            photoTabLayout = (PhotoTabLayout) getSupportFragmentManager().findFragmentByTag(PhotoTabLayout.class.getName());
//            videoTabLayout = (VideoTabLayout) getSupportFragmentManager().findFragmentByTag(VideoTabLayout.class.getName());
//            mediaChannelView = (MediaChannelView) getSupportFragmentManager().findFragmentByTag(MediaChannelView.class.getName());
            // 恢复 recreate 前的位置
            showFragment(savedInstanceState.getInt(POSITION));
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt(SELECT_ITEM));
        } else {
            showFragment(FRAGMENT_NEWS);
        }

        if (SettingUtil.getInstance().getIsFirstTime()) {
            showTapTarget();
        }


    }


    private void initView() {

        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar.inflateMenu(R.menu.menu_activity_main);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_news:
                        showFragment(FRAGMENT_NEWS);
                        doubleClick(FRAGMENT_NEWS);
                        break;
                    case R.id.action_photo:
                        showFragment(FRAGMENT_PHOTO);
                        doubleClick(FRAGMENT_PHOTO);
                        break;
                    case R.id.action_video:
                        showFragment(FRAGMENT_VIDEO);
                        doubleClick(FRAGMENT_VIDEO);
                        break;
                    case R.id.action_media:
                        showFragment(FRAGMENT_MEDIA);
                        break;
                }
                return true;
            }
        });

    }


    /**
     *
     * 实现功能：
     *
     *  下面两个方法 显示toolbar上的布局 和点击事件
     *
     * 注意事项：
     *
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    private void hideFragment(FragmentTransaction ft) {
        // 如果不为空，就先隐藏起来
        if (newsTabLayout != null) {
            ft.hide(newsTabLayout);
        }
//        if (photoTabLayout != null) {
//            ft.hide(photoTabLayout);
//        }
//        if (videoTabLayout != null) {
//            ft.hide(videoTabLayout);
//        }
//        if (mediaChannelView != null) {
//            ft.hide(mediaChannelView);
//        }
    }

    /**
     *
     * 实现功能：
     *
     * 双击刷新
     *
     * 注意事项：
     *
     *
     */
    public void doubleClick(int index) {
        long secondClickTime = System.currentTimeMillis();
        if ((secondClickTime - firstClickTime < 500)) {
            switch (index) {
                case FRAGMENT_NEWS:
                    newsTabLayout.onDoubleClick();
                    break;
//                case FRAGMENT_PHOTO:
//                    photoTabLayout.onDoubleClick();
//                    break;
//                case FRAGMENT_VIDEO:
//                    videoTabLayout.onDoubleClick();
//                    break;
            }
        } else {
            firstClickTime = secondClickTime;
        }
    }




    private void showFragment(int index) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        hideFragment(ft);
        position = index;
        switch (index) {
            case FRAGMENT_NEWS:
                toolbar.setTitle(R.string.app_name);
                /**
                 * 如果Fragment为空，就新建一个实例
                 * 如果不为空，就将它从栈中显示出来
                 */
                if (newsTabLayout == null) {
                    newsTabLayout = NewsTabLayout.getInstance();
                    ft.add(R.id.container, newsTabLayout, NewsTabLayout.class.getName());
                } else {
                    ft.show(newsTabLayout);
                }
                break;

//            case FRAGMENT_PHOTO:
//                toolbar.setTitle(R.string.title_photo);
//                if (photoTabLayout == null) {
//                    photoTabLayout = PhotoTabLayout.getInstance();
//                    ft.add(R.id.container, photoTabLayout, PhotoTabLayout.class.getName());
//                } else {
//                    ft.show(photoTabLayout);
//                }
//                break;

//            case FRAGMENT_VIDEO:
//                toolbar.setTitle(getString(R.string.title_video));
//                if (videoTabLayout == null) {
//                    videoTabLayout = VideoTabLayout.getInstance();
//                    ft.add(R.id.container, videoTabLayout, VideoTabLayout.class.getName());
//                } else {
//                    ft.show(videoTabLayout);
//                }
//                break;
//
//            case FRAGMENT_MEDIA:
//                toolbar.setTitle(getString(R.string.title_media));
//                if (mediaChannelView == null) {
//                    mediaChannelView = MediaChannelView.getInstance();
//                    ft.add(R.id.container, mediaChannelView, MediaChannelView.class.getName());
//                } else {
//                    ft.show(mediaChannelView);
//                }
        }

        ft.commit();
    }

    private void showTapTarget() {
        final Display display = getWindowManager().getDefaultDisplay();
        final Rect target = new Rect(
                0,
                display.getHeight(),
                0,
                display.getHeight());
        target.offset(display.getWidth() / 8, -56);

        // 引导用户使用
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_search, "点击这里进行搜索")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorPrimary)
                                .drawShadow(true)
                                .id(1),
                        TapTarget.forToolbarNavigationIcon(toolbar, "点击这里展开侧栏")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorPrimary)
                                .drawShadow(true)
                                .id(2),
                        TapTarget.forBounds(target, "点击这里切换新闻", "双击返回顶部\n再次双击刷新当前页面")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorPrimary)
                                .targetRadius(60)
                                .transparentTarget(true)
                                .drawShadow(true)
                                .id(3)
                ).listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        SettingUtil.getInstance().setIsFirstTime(false);
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        SettingUtil.getInstance().setIsFirstTime(false);
                    }
                });
        sequence.start();
    }

}
