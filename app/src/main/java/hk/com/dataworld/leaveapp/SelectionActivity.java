package hk.com.dataworld.leaveapp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import hk.com.dataworld.leaveapp.DAL.Notification;
import me.leolin.shortcutbadger.ShortcutBadger;

import static hk.com.dataworld.leaveapp.Constants.ACTION_INCOMING_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_ALLOWED_APPROVALS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_PAY_SLIP;

public class SelectionActivity extends BaseActivity {

    private static final String TAG = SelectionActivity.class.getSimpleName();
    private Button mLeaveApplyButton, mMyApplicationsButton, mLeaveApprovalsButton, mLeaveCalendarButton, mPaySlipButton, mTaxationButton, mFareClaimsButton, mAttendanceButton, mChatroomButton;
    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;
    private ArrayList<Notification> mReceived = new ArrayList<>();
    private String mInstanceId;

    private void startNotificationService() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                Log.i("Notification", "Service already running.");
                return;
            }
        }
        Log.i("Notification", "Starting service...");
        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);
    }

    private void registerBroadcastReceiver(NotificationBroadcastReceiver bcr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INCOMING_NOTIFICATION);
        registerReceiver(bcr, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selection);

        Typeface avenir_black = ResourcesCompat.getFont(this, R.font.avenirlt_black);

        registerBroadcastReceiver(new NotificationBroadcastReceiver());
        startNotificationService();
//        Intent debugIntent = new Intent(this, AttendanceSyncService.class);
//        startService(debugIntent);

        mLeaveApplyButton = findViewById(R.id.btnApplyleave);
        mMyApplicationsButton = findViewById(R.id.btnMyApplication);
        mLeaveApprovalsButton = findViewById(R.id.btnLeaveApproval);
        mLeaveCalendarButton = findViewById(R.id.btnLeaveCalendar);
        mFareClaimsButton = findViewById(R.id.btnFareClaims);
        mAttendanceButton = findViewById(R.id.btnBluetoothAttendance);
        mPaySlipButton = findViewById(R.id.btnPaySlip);
        mTaxationButton = findViewById(R.id.btnTaxation);
        mChatroomButton = findViewById(R.id.btnChatroom);

        mLeaveApplyButton.setTypeface(avenir_black, Typeface.NORMAL);
        mMyApplicationsButton.setTypeface(avenir_black, Typeface.NORMAL);
        mLeaveApprovalsButton.setTypeface(avenir_black, Typeface.NORMAL);
        mLeaveCalendarButton.setTypeface(avenir_black, Typeface.NORMAL);
        mFareClaimsButton.setTypeface(avenir_black, Typeface.NORMAL);
        mAttendanceButton.setTypeface(avenir_black, Typeface.NORMAL);
        mPaySlipButton.setTypeface(avenir_black, Typeface.NORMAL);
        mTaxationButton.setTypeface(avenir_black, Typeface.NORMAL);
        mChatroomButton.setTypeface(avenir_black, Typeface.NORMAL);

//        Intent intent = getIntent();

        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("IsAllowApprovals", false)) {
            mLeaveApprovalsButton.setVisibility(View.GONE);
        }

        mLeaveApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Log.i(TAG, "Will go to Apply Leave");
//                Intent intent = new Intent(SelectionActivity.this, LeaveApplyActivity.class);
                Intent intent = new Intent(SelectionActivity.this, LeaveApplyActivityRevised.class);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                startActivity(intent);
            }
        });

        mMyApplicationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Log.i(TAG, "Will go to Leave Master Record From Staff Apply Leave History Page");
                Intent intent = new Intent(SelectionActivity.this, LeaveMasterRecordActivity.class);
                intent.putExtra(EXTRA_TO_MY_HISTORY, true);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                startActivity(intent);
            }
        });

        mLeaveApprovalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Log.i(TAG, "Will go to Leave Master Record from Selection Page");
                Intent intent = new Intent(SelectionActivity.this, LeaveMasterRecordActivity.class);
                intent.putExtra(EXTRA_TO_MY_HISTORY, false);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                startActivity(intent);
            }
        });

        mPaySlipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(SelectionActivity.this, PaySlipActivity.class);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, mReceived);
                startActivity(intent);
            }
        });

        mTaxationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(SelectionActivity.this, IR56BActivity.class);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, mReceived);
                startActivity(intent);
            }
        });

        mFareClaimsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(SelectionActivity.this, ClaimsActivity.class);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, mReceived);
                startActivity(intent);
            }
        });

        mLeaveCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsEnableRestartBehaviour = false;
                Intent intent = new Intent(SelectionActivity.this, LeaveCalendarActivity.class);
                intent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, mReceived);
                startActivity(intent);
            }
        });

        mAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isIAttendanceInstalled("hk.com.dataworld.iattendance", getPackageManager())) {
                    // TODO: Open iAttendance Activity instead of App
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("hk.com.dataworld.iattendance");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
            }
        });

        mChatroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID mInstanceId
                                InstanceIdResult instanceIdResult = task.getResult();
                                if (instanceIdResult != null) {
                                    mInstanceId = instanceIdResult.getToken();
                                    Log.d(TAG, mInstanceId);
                                    Toast.makeText(SelectionActivity.this, mInstanceId, Toast.LENGTH_SHORT).show();
                                }

                                Intent intent = new Intent(SelectionActivity.this, MessengerSelectionActivity.class);
//                                intent.putExtra(EXTRA_ALLOWED_APPROVALS, IsAllowApprovals);
//                        finish();
                                startActivity(intent);
                            }
                        });
            }
        });

    }

    private static boolean isIAttendanceInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        stopDisconnectTimer();
    }

    //add delay to prevent app hang for older android version
    @Override
    protected void onRestart() {
        super.onRestart();
//        Log.i(TAG, "onRestart");
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                    overridePendingTransition(0, 0);
//                    startActivity(getIntent());
//                    overridePendingTransition(0, 0);
//                }
//            }, 1);
//        } else {
//            setLocale();
//            recreate();
//        }
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mNotificationAdapter == null) {
            Log.i("MenuAction", "Prepare");
            mListView = findViewById(R.id.drawer_list);
            mNotificationAdapter = new NotificationListAdapter(this, menu);
//        List<String> test = new ArrayList<>();
//        test.add("abc");
//        test.add("def");
//        mNotificationAdapter.addItems(test);
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
        }
        return true;
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
            mReceived = intent.getParcelableArrayListExtra(EXTRA_BROADCAST_NOTIFICATION);
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
            ShortcutBadger.applyCount(SelectionActivity.this, count);
        }


        public void oldOnReceive(Context context, Intent intent) {
            // Shim - Should use fragments instead of activities
            mReceived = intent.getParcelableArrayListExtra(EXTRA_BROADCAST_NOTIFICATION);
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
            ShortcutBadger.applyCount(SelectionActivity.this, count);
        }
    }
}
