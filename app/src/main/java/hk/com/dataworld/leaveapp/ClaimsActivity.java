package hk.com.dataworld.leaveapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import hk.com.dataworld.leaveapp.DAL.Notification;
import me.leolin.shortcutbadger.ShortcutBadger;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;

public class ClaimsActivity extends BaseActivity {
    private ClaimsGeneralFragment mGeneralFragment;
    private ClaimsManualFragment mManualFragment;
    private ClaimsRecommendedFragment mRecommendedFragment;
    private MenuItem mPrevItem = null;

    private ViewPager mPager;
    private BottomNavigationView mBottomNav;

    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.btn_fare_claimsCamel);
        setContentView(R.layout.activity_claims);
        mPager = findViewById(R.id.claims_pager);
        mBottomNav = findViewById(R.id.claims_bottom_nav);
        setUpViewPager();
        setUpBottomNav();
    }

    private void setUpViewPager() {
        ClaimsFragmentPagerAdapter adapter = new ClaimsFragmentPagerAdapter(getSupportFragmentManager());
        mGeneralFragment = new ClaimsGeneralFragment();
        adapter.addFragment(mGeneralFragment);
        mRecommendedFragment = new ClaimsRecommendedFragment();
        adapter.addFragment(mRecommendedFragment);
        mManualFragment = new ClaimsManualFragment();
        adapter.addFragment(mManualFragment);
        mPager.setAdapter(adapter);
    }

    private void setUpBottomNav() {
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_claims_general:
                        mPager.setCurrentItem(0);
                        break;
                    case R.id.item_claims_recommended_route:
                        mPager.setCurrentItem(1);
                        break;
                    case R.id.item_claims_manual_mode:
                        mPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mPrevItem != null)
                    mPrevItem.setChecked(false);
                else
                    mBottomNav.getMenu().getItem(0).setChecked(false);

                mBottomNav.getMenu().getItem(position).setChecked(true);
                mPrevItem = mBottomNav.getMenu().getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mNotificationAdapter == null) {
            mListView = findViewById(R.id.drawer_list);
            mNotificationAdapter = new NotificationListAdapter(this, menu);
            ArrayList<Notification> notifications = getIntent().getParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION);
            mNotificationAdapter.addItems(notifications);
            mListView.setAdapter(mNotificationAdapter);

            View v = menu.findItem(R.id.action_notification).getActionView();

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Open drawer
                    DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
                    if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                        drawerLayout.closeDrawer(GravityCompat.END);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.END);
                    }

                }
            };

            BootstrapButton raBtn = findViewById(R.id.btn_removeAllNotifications);
            raBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mNotificationAdapter.clearItems();
                }
            });

            AwesomeTextView awesomeTextView = v.findViewById(R.id.message_icon);
            awesomeTextView.setOnClickListener(onClickListener);

            mNotiCountButton = v.findViewById(R.id.message_count);
            mNotiCountButton.setOnClickListener(onClickListener);
            mNotiCountButton.setText(String.valueOf(notifications.size()));

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent itemIntent = new Intent(getBaseContext(), LeaveMasterRecordActivity.class);
                    itemIntent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                    itemIntent.putExtra(EXTRA_SOURCE_NOTIFICATION_STATUS, mNotificationAdapter.getStatus(i));
                    itemIntent.putExtra(EXTRA_TO_MY_HISTORY, true);
                    startActivity(itemIntent);
                }
            });
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mIsEnableRestartBehaviour = true;
    }

    class NotificationBroadcastReceiver extends BroadcastReceiver {
        public NotificationBroadcastReceiver() {
            super();
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Shim - Should use fragments instead of activities
            ArrayList<Notification> mReceived = intent.getParcelableArrayListExtra(EXTRA_BROADCAST_NOTIFICATION);
            Log.i("Incoming", "Incoming");
            if ((mNotificationAdapter != null) && (mReceived.size() != 0)) {
                mNotificationAdapter.addItems(mReceived);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent itemIntent = new Intent(getBaseContext(), LeaveMasterRecordActivity.class);
                        itemIntent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                        itemIntent.putExtra(EXTRA_SOURCE_NOTIFICATION_STATUS, mNotificationAdapter.getStatus(i));
                        itemIntent.putExtra(EXTRA_TO_MY_HISTORY, true);
                        startActivity(itemIntent);
                    }
                });
            }
            int count = intent.getIntExtra(EXTRA_BROADCAST_NOTIFICATION_COUNT, 0);
            mNotiCountButton.setText(String.valueOf(count));
            ShortcutBadger.applyCount(ClaimsActivity.this, count);
        }
    }
}
