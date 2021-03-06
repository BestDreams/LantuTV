package com.dream.lantutv;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.dream.adapter.FragmentPageAdapter;
import com.dream.fragment.MenuListFragment;
import com.dream.fragment.MusicFragment;
import com.dream.fragment.NetworkMusicFragment;
import com.dream.fragment.NetworkVideoFragment;
import com.dream.fragment.VideoFragment;
import com.dream.utils.LyricUtils;
import com.dream.view.NoTouchViewPager;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import devlight.io.library.ntb.NavigationTabBar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFragment();
        initNavigationTabBar();
        initDrawer();
    }



    private FlowingDrawer mDrawer;
    public void initDrawer(){
        mDrawer = (FlowingDrawer) findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        setupMenu();
    }

    private ImageView showMenu;
    private void setupMenu() {
        FragmentManager fm = getSupportFragmentManager();
        MenuListFragment mMenuFragment = (MenuListFragment) fm.findFragmentById(R.id.id_container_menu);
        if (mMenuFragment == null) {
            mMenuFragment = new MenuListFragment();
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment).commit();
        }
        showMenu = (ImageView) findViewById(R.id.main_menu);
        showMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.toggleMenu();
            }
        });
    }

    private List<Fragment> fragmentList;
    public void initFragment(){
        fragmentList=new ArrayList<>();
        VideoFragment localVideoFragment=new VideoFragment();
        MusicFragment localMusicFragment=new MusicFragment();
        NetworkVideoFragment networkVideoFragment=new NetworkVideoFragment();
        NetworkMusicFragment networkMusicFragment=new NetworkMusicFragment();
        fragmentList.add(localVideoFragment);
        fragmentList.add(localMusicFragment);
        fragmentList.add(networkVideoFragment);
        fragmentList.add(networkMusicFragment);
    }

    private NavigationTabBar mainNav;
    private NoTouchViewPager mainViewpager;
    private List<NavigationTabBar.Model> models;
    private String[] colors;
    public void initNavigationTabBar(){
        mainViewpager = (NoTouchViewPager) findViewById(R.id.main_viewpager);
        mainViewpager.setOffscreenPageLimit(4);
        mainViewpager.setAdapter(new FragmentPageAdapter(getSupportFragmentManager(),fragmentList));
        mainNav = (NavigationTabBar) findViewById(R.id.main_nav);
        colors=getResources().getStringArray(R.array.default_preview);
        models=new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.mipmap.ic_fifth),
                        Color.parseColor(colors[0]))
                        .selectedIcon(getResources().getDrawable(R.mipmap.ic_sixth))
                        .title("视频")
                        .badgeTitle("one")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.mipmap.ic_second),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(R.mipmap.ic_eighth))
                        .title("音乐")
                        .badgeTitle("two")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.mipmap.ic_third),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(R.mipmap.ic_seventh))
                        .title("梨视频")
                        .badgeTitle("three")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.mipmap.ic_fourth),
                        Color.parseColor(colors[3]))
                        .selectedIcon(getResources().getDrawable(R.mipmap.ic_eighth))
                        .title("酷狗")
                        .badgeTitle("four")
                        .build()
        );

        mainNav.setModels(models);
        mainNav.setViewPager(mainViewpager, 0);
        mainNav.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {

            }

            @Override
            public void onPageScrollStateChanged(final int state) {

            }
        });

        mainNav.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mainNav.getModels().size(); i++) {
                    final NavigationTabBar.Model model = mainNav.getModels().get(i);
                    mainNav.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);
    }

}
