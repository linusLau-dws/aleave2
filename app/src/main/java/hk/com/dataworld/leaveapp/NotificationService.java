package hk.com.dataworld.leaveapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import hk.com.dataworld.leaveapp.DAL.Notification;

import static hk.com.dataworld.leaveapp.Constants.ACTION_ALTERING_COUNT;
import static hk.com.dataworld.leaveapp.Constants.ACTION_INCOMING_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.DEBUG_FALLBACK_URL;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_IS_ALL;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;
import static hk.com.dataworld.leaveapp.Constants.NOTIFICATION_CHANNEL_NAME;
import static hk.com.dataworld.leaveapp.Constants.NOTIFICATION_STATUS_APPROVER;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Constants.STATUS_APPROVED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CONFIRMED_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_REJECTED;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;

public class NotificationService extends Service {
    private RequestQueue mRequestQueue;
    private int notification_count = 0;
    private SharedPreferences sharedPreferences;

    public NotificationService() {
        super();
    }

    private void notificationCountMinus() {
        notification_count -= 1;
        Log.i("Alter", String.valueOf(notification_count));
    }

    private void notificationClear() {
        notification_count = 0;
        Log.i("Alter", String.valueOf(notification_count));
    }

    private void registerBroadcastReceiver(AlterBroadcastReceiver bcr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ALTERING_COUNT);
        registerReceiver(bcr, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void setLocale(String locale) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale lc;
        if (locale.length() > 2) {
            lc = new Locale(locale.substring(0, 2), locale.substring(3, 5));
        } else {
            lc = new Locale(locale);
        }
        if (!configuration.locale.equals(lc)) {
            configuration.setLocale(lc);
            resources.updateConfiguration(configuration, null);
        }
    }

    // TODO: Keep track of the notification count
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBroadcastReceiver(new AlterBroadcastReceiver());

        Log.d("esKlapptCaMarche", "esKlapptCaMarche");

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_NAME, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400, 300, 200});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                NotificationCompat.Builder notificationBuilder = new
//                        NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_NAME);
//                notificationBuilder.setSmallIcon(R.drawable.ihr)
//                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.accept_icon))
//                        .setContentTitle(getString(R.string.approved_1, "Lorem"))
//                        .setContentText(getString(R.string.approved_2, "Lorem", "ipsum", "dolor", "sit", "amet"))
//                        .setLights(Color.GREEN, 500, 2000);
//                NotificationManager notificationManager =
//                        (NotificationManager)
//                                getApplicationContext().getSystemService(
//                                        Context.NOTIFICATION_SERVICE);
//                notificationManager.notify(0,
//                        notificationBuilder.build());

                Log.d("CaMarche", "CaMarche");

                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
                String address = extendBaseUrl(sharedPreferences.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL));

                String token = sharedPreferences.getString(PREF_TOKEN, "");

                JSONObject object = new JSONObject();
                try {
                    object.put("token", token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mRequestQueue = Volley.newRequestQueue(NotificationService.this);
                JsonObjectRequest notificationRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", address, "CheckNotification"),
                        object, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("Notification", "Responded");

                        Uri notificationSoundUri =
                                RingtoneManager.getDefaultUri(
                                        RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder notificationBuilder = new
                                NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_NAME);
                        try {
                            if (response.has("d")) {
                                JSONArray leaveApplications = response.getJSONArray("d");
                                int diff = leaveApplications.length() - notification_count;
                                notification_count = leaveApplications.length();
                                ArrayList<Notification> extraList = new ArrayList<>();

                                //region Force locale
                                setLocale(sharedPreferences.getString(PREF_LOCALE, "en"));
                                //endregion
                                for (int i = 0; i < diff; i++) {

                                    JSONObject leaveInfo = leaveApplications.getJSONObject(i);
                                    String leaveDesc = leaveInfo.getString("Leave Desc");
                                    String leaveFrom = leaveInfo.getString("Leave From");
                                    String leaveTo = leaveInfo.getString("Leave To");
                                    String reviewerName = leaveInfo.getString("Reviewer Name");
                                    String createDate = leaveInfo.getString("Create Date");
                                    int status = leaveInfo.getInt("Status");

                                    Intent itemIntent = new Intent(getBaseContext(), LeaveMasterRecordActivity.class);
                                    itemIntent.putExtra(EXTRA_SOURCE_NOTIFICATION_STATUS, status);
                                    itemIntent.putExtra(EXTRA_TO_MY_HISTORY, true);

                                    String message;
                                    switch (status) {
                                        case STATUS_APPROVED:
                                            message = !leaveFrom.equals(leaveTo) ? getString(R.string.approved_2, leaveDesc,
                                                    leaveFrom,
                                                    leaveTo,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate) : getString(R.string.approved_3, leaveDesc,
                                                    leaveFrom,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate);
                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.accept_icon))
                                                    .setContentTitle(getString(R.string.approved_1, leaveDesc))
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                                    .setContentText(message)
                                                    .setSound(notificationSoundUri)
                                                    .setContentIntent(PendingIntent.getActivity(NotificationService.this, 0, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                    .setLights(Color.GREEN, 500, 2000);
                                            break;
                                        case STATUS_REJECTED:
                                            message = !leaveFrom.equals(leaveTo) ? getString(R.string.rejected_2, leaveDesc,
                                                    leaveFrom,
                                                    leaveTo,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate) : getString(R.string.rejected_3, leaveDesc,
                                                    leaveFrom,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate);
                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.reject_icon))
                                                    .setContentTitle(getString(R.string.rejected_1, leaveDesc))
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                                    .setContentText(message)
                                                    .setSound(notificationSoundUri)
                                                    .setContentIntent(PendingIntent.getActivity(NotificationService.this, 0, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                    .setLights(Color.RED, 500, 2000);
                                            break;
                                        case STATUS_CONFIRMED_CANCELLED:
                                            message = !leaveFrom.equals(leaveTo) ? getString(R.string.confirmed_cancelled_2, leaveDesc,
                                                    leaveFrom,
                                                    leaveTo,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate) : getString(R.string.confirmed_cancelled_3, leaveDesc,
                                                    leaveFrom,
                                                    reviewerName != null ? reviewerName : "",
                                                    createDate);
                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.cancel_approved_icon))
                                                    .setContentTitle(getString(R.string.confirmed_cancelled_1, leaveDesc))
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                                    .setContentText(message)
                                                    .setSound(notificationSoundUri)
                                                    .setContentIntent(PendingIntent.getActivity(NotificationService.this, 0, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                    .setLights(Color.RED, 500, 2000);
                                            break;
                                        case NOTIFICATION_STATUS_APPROVER:
                                            String applierName = leaveInfo.getString("Applier Name");
                                            message = !leaveFrom.equals(leaveTo) ? getString(R.string.new_leave_application_2, applierName, leaveFrom, leaveTo, leaveDesc)
                                                    : getString(R.string.new_leave_application_3, applierName, leaveFrom, leaveDesc);
                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.new_application))
                                                    .setContentTitle(getString(R.string.new_leave_application_1, leaveDesc))
                                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                                    .setContentText(message)
                                                    .setSound(notificationSoundUri)
                                                    .setContentIntent(PendingIntent.getActivity(NotificationService.this, 0, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                    .setLights(Color.RED, 500, 2000);
                                            break;
                                        // New: Notifications for Approver

                                    }

                                    notificationManager.notify(createDate.hashCode() + i,
                                            notificationBuilder.build());
                                    Notification notification = new Notification(leaveDesc, leaveFrom, leaveTo, reviewerName, createDate, status);
                                    extraList.add(notification);
                                }
                                if (leaveApplications.length() > 0) {
                                    Intent broadcast = new Intent();
                                    Log.i("Broadcast", "Broadcast sent");
                                    broadcast.setAction(ACTION_INCOMING_NOTIFICATION);
                                    broadcast.putParcelableArrayListExtra(EXTRA_BROADCAST_NOTIFICATION, extraList);
                                    broadcast.putExtra(EXTRA_BROADCAST_NOTIFICATION_COUNT, notification_count);
//                                        ArrayList<MtrStation> notification = new ArrayList<>(Arrays.asList(MtrStation.CREATOR.newArray(3)));
//                                        broadcast.putParcelableArrayListExtra("Notification", notification);
                                    sendBroadcast(broadcast);
                                }
                            }
                        } catch (JSONException e) {
                            //Invalid token
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.wtf("NotificationErr", "Network error");
                    }
                });
                mRequestQueue.add(notificationRequest);
            }
        }, 0, 20, TimeUnit.SECONDS);


//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("CaMarche", "CaMarche");
//
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
//                String address = sharedPreferences.getString(PREF_SERVER_ADDRESS, DEBUG_FALLBACK_URL);
//
//                String token = sharedPreferences.getString(PREF_TOKEN, "");
//
//                JSONObject object = new JSONObject();
//                try {
//                    object.put("token", token);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                mRequestQueue = Volley.newRequestQueue(NotificationService.this);
//                JsonObjectRequest notificationRequest = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", address, "CheckNotification"),
//                        object, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.i("Notification", "Responded");
//
//                        Uri notificationSoundUri =
//                                RingtoneManager.getDefaultUri(
//                                        RingtoneManager.TYPE_NOTIFICATION);
//                        NotificationCompat.Builder notificationBuilder = new
//                                NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_NAME);
//                        try {
//                            if (response.has("d")) {
//                                JSONArray leaveApplications = response.getJSONArray("d");
//                                for (int i = 0; i < leaveApplications.length(); i++) {
//                                    JSONObject leaveInfo = leaveApplications.getJSONObject(i);
//                                    switch (leaveInfo.getInt("Status")) {
//                                        case STATUS_APPROVED:
//                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
//                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.accept_icon))
//                                                    .setContentTitle(getString(R.string.approved_1, leaveInfo.getString("Leave Desc")))
//                                                    .setContentText(getString(R.string.approved_2, leaveInfo.getString("Leave Desc"),
//                                                            leaveInfo.getString("Leave From"),
//                                                            leaveInfo.getString("Leave To"),
//                                                            leaveInfo.getString("Reviewer Name") != null ? leaveInfo.getString("Reviewer Name") : "",
//                                                            leaveInfo.getString("Create Date")))
//                                                    .setSound(notificationSoundUri)
//                                                    .setLights(Color.GREEN, 500, 500);
//                                            break;
//                                        case STATUS_REJECTED:
//                                            notificationBuilder.setSmallIcon(R.drawable.ihr)
//                                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.reject_icon))
//                                                    .setContentTitle(getString(R.string.rejected_1, leaveInfo.getString("Leave Desc")))
//                                                    .setContentText(getString(R.string.rejected_2, leaveInfo.getString("Leave Desc"),
//                                                            leaveInfo.getString("Leave From"),
//                                                            leaveInfo.getString("Leave To"),
//                                                            leaveInfo.getString("Reviewer Name") != null ? leaveInfo.getString("Reviewer Name") : "",
//                                                            leaveInfo.getString("Create Date")))
//                                                    .setSound(notificationSoundUri)
//                                                    .setLights(Color.RED, 500, 500);
//                                            break;
//                                    }
//                                    NotificationManager notificationManager =
//                                            (NotificationManager)
//                                                    getApplicationContext().getSystemService(
//                                                            Context.NOTIFICATION_SERVICE);
//                                    notificationManager.notify(0,
//                                            notificationBuilder.build());
//
//                                    if (leaveApplications.length() > 0) {
//                                        Intent broadcast = new Intent();
//                                        Log.i("Broadcast", "Broadcast sent");
//                                        broadcast.setAction(ACTION_INCOMING_NOTIFICATION);
////                                        ArrayList<MtrStation> notification = new ArrayList<>(Arrays.asList(MtrStation.CREATOR.newArray(3)));
////                                        broadcast.putParcelableArrayListExtra("Notification", notification);
//                                        sendBroadcast(broadcast);
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            //Invalid token
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.wtf("NotificationErr", error.networkResponse.toString());
//                    }
//                });
//                mRequestQueue.add(notificationRequest);
//            }
//        });
//        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class AlterBroadcastReceiver extends BroadcastReceiver {
        public AlterBroadcastReceiver() {
            super();
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Shim - Should use fragments instead of activities
            boolean isAll = intent.getBooleanExtra(EXTRA_IS_ALL, false);
            Log.i("Altering", "Altering");
            if (isAll) {
                notificationClear();
            } else {
                notificationCountMinus();
            }
        }
    }
}
