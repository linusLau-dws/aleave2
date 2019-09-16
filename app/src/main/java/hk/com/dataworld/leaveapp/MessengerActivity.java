package hk.com.dataworld.leaveapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.shrikanthravi.chatview.data.Message;
import com.shrikanthravi.chatview.widget.ChatView;
import com.zhihu.matisse.Matisse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hk.com.dataworld.leaveapp.DAL.Notification;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;

public class MessengerActivity extends BaseActivity {

    private ChatView mChatView;
    private static final int IMAGE_PICKER = 123;
    private static final int SELECT_VIDEO = 146;
    private static final int CAMERA_REQUEST = 352;
    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private boolean mIsIncoming = true;

    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;
    private List<Uri> mSelected;

    private SharedPreferences mSharedPreferences;
    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Self Test");
        setContentView(R.layout.activity_messenger);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mChatView = findViewById(R.id.chatView);
        Message message = new Message();
        message.setMessageType(Message.MessageType.RightSimpleImage);
        message.setBody("Test");
        mChatView.addMessage(message);
        mChatView.setLeftBubbleLayoutColor(0xFF4287F5);
        mChatView.setLeftBubbleTextColor(0xFFFFFFFF);
        mChatView.setRightBubbleLayoutColor(0xFFFFFFFF);
        mChatView.setRightBubbleTextColor(0xFF000000);

        //Send button click listener
        mChatView.setOnClickSendButtonListener(new ChatView.OnClickSendButtonListener() {
            @Override
            public void onSendButtonClick(String body) {
                sendMessage(body);
            }
        });

        //Gallery button click listener
        mChatView.setOnClickGalleryButtonListener(new ChatView.OnClickGalleryButtonListener() {
            @Override
            public void onGalleryButtonClick() {
                Intent intent = new Intent(Intent.ACTION_PICK); // Action
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICKER);
//                Matisse.from(MessengerActivity.this)
//                        .choose(MimeType.allOf())
//                        .countable(true)
//                        .maxSelectable(9)
//                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                        .thumbnailScale(0.85f)
//                        .imageEngine(new PicassoEngine())
//                        .forResult(IMAGE_PICKER);
            }
        });

        //Video button click listener
        mChatView.setOnClickVideoButtonListener(new ChatView.OnClickVideoButtonListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onVideoButtonClick() {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                i.setType("video/*");
                startActivityForResult(i, SELECT_VIDEO);
            }
        });

        //Camera button click listener
        mChatView.setOnClickCameraButtonListener(new ChatView.OnClickCameraButtonListener() {
            @Override
            public void onCameraButtonClicked() {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                File file1 = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");

                Uri uri = FileProvider.getUriForFile(MessengerActivity.this, getApplicationContext().getPackageName() + ".provider", file1);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    private void sendMessage(String body) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mSharedPreferences.getString(PREF_TOKEN, ""));
            obj.put("StationCode", "01");
            obj.put("ZoneCode", "01");
            obj.put("Message", body);
            obj.put("ChatroomID", 1);
            obj.put("MessageType", 0);
            obj.put("program", 0);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", extendBaseUrl(mSharedPreferences.getString(PREF_SERVER_ADDRESS, "")), "SendMsg"), obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                            try {
//                                 //
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
                }
            }, getGenericErrorListener(MessengerActivity.this, null));
            mRequestQueue = Volley.newRequestQueue(MessengerActivity.this);
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mIsIncoming) {
            Message message = new Message();
            message.setBody(body);
            message.setMessageType(Message.MessageType.RightSimpleImage);
            message.setTime(mFormatter.format(new Date()));
            message.setUserName("Groot");
            message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
            mChatView.addMessage(message);

            mIsIncoming = false;
        } else {
            Message message1 = new Message();
            message1.setBody(body);
            message1.setMessageType(Message.MessageType.LeftSimpleMessage);
            message1.setTime(mFormatter.format(new Date()));
            message1.setUserName("You");
            message1.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/hodor"));
            mChatView.addMessage(message1);

            mIsIncoming = true;
        }
    }

    private void receiveMessage(String sender, String body) {
        Message message = new Message();
        message.setUserName(sender);
        message.setBody(body);
        message.setMessageType(Message.MessageType.LeftSimpleMessage);
        message.setTime(mFormatter.format(new Date()));
        message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
        mChatView.addMessage(message);

        mIsIncoming = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image Selection result
        if (requestCode == IMAGE_PICKER && resultCode == RESULT_OK) {
//            mSelected = Matisse.obtainResult(data);
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//            Message _message = new Message();
//            _message.setBody("Lorem ipsum");//messageET.getText().toString().trim());
//            _message.setMessageType(Message.MessageType.LeftSingleImage);
//            _message.setTime(mFormatter.format(new Date()));
//            _message.setUserName("Groot");
//            Uri uri = Uri.parse(picturePath);
//            List<Uri> uriList = new ArrayList<>();
//            uriList.add(uri);
//            _message.setImageList(uriList);//mSelected);
//            _message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
//            mChatView.addMessage(message);
//            mIsIncoming = false;
//            return;            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String picturePath = cursor.getString(columnIndex);
//            cursor.close();
//            Message _message = new Message();
//            _message.setBody("Lorem ipsum");//messageET.getText().toString().trim());
//            _message.setMessageType(Message.MessageType.LeftSingleImage);
//            _message.setTime(mFormatter.format(new Date()));
//            _message.setUserName("Groot");
//            Uri uri = Uri.parse(picturePath);
//            List<Uri> uriList = new ArrayList<>();
//            uriList.add(uri);
//            _message.setImageList(uriList);//mSelected);
//            _message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
//            mChatView.addMessage(message);
//            mIsIncoming = false;
//            return;

            if (mIsIncoming) {
                if (mSelected.size() == 1) {
                    Message message = new Message();
                    message.setBody("Lorem ipsum");//messageET.getText().toString().trim());
                    message.setMessageType(Message.MessageType.LeftSingleImage);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Groot");
                    message.setImageList(mSelected);//mSelected);
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
                    mChatView.addMessage(message);
                    mIsIncoming = false;
                } else {

                    Message message = new Message();
                    message.setBody("dolor sit amet");//messageET.getText().toString().trim());
                    message.setMessageType(Message.MessageType.RightSingleImage);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Groot");
                    message.setImageList(mSelected);
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
                    mChatView.addMessage(message);
                    mIsIncoming = false;
                }
            } else {

                if (mSelected.size() == 1) {
                    Message message = new Message();
                    message.setBody("conspcicuous");//messageET.getText().toString().trim());
                    message.setMessageType(Message.MessageType.LeftMultipleImages);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Hodor");
                    message.setImageList(mSelected);
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/hodor"));
                    mChatView.addMessage(message);
                    mIsIncoming = true;
                } else {

                    Message message = new Message();
                    message.setBody("quiproquo");//messageET.getText().toString().trim());
                    message.setMessageType(Message.MessageType.RightMultipleImages);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Hodor");
                    message.setImageList(mSelected);
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/hodor"));
                    mChatView.addMessage(message);
                    mIsIncoming = true;
                }

            }
        } else {

            //Video Selection result
            if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK) {
                if (mIsIncoming) {
                    Message message = new Message();
                    message.setMessageType(Message.MessageType.LeftVideo);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Groot");
                    message.setVideoUri(Uri.parse(data.getData().getPath()));
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
                    mChatView.addMessage(message);
                    mIsIncoming = false;
                } else {
                    Message message = new Message();

                    message.setMessageType(Message.MessageType.RightVideo);
                    message.setTime(mFormatter.format(new Date()));
                    message.setUserName("Hodor");
                    message.setVideoUri(Uri.parse(data.getData().getPath()));
                    message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/hodor"));
                    mChatView.addMessage(message);
                    mIsIncoming = true;
                }
            } else {

                //Image Capture result
                if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
                    if (mIsIncoming) {
                        Message message = new Message();
                        message.setMessageType(Message.MessageType.LeftSingleImage);
                        message.setTime(mFormatter.format(new Date()));
                        message.setUserName("Groot");
                        mSelected.clear();
                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                        //Uri of camera image
                        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
                        mSelected.add(uri);
                        message.setImageList(mSelected);
                        message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/groot"));
                        mChatView.addMessage(message);
                        mIsIncoming = false;
                    } else {
                        Message message = new Message();

                        message.setMessageType(Message.MessageType.RightSingleImage);
                        message.setTime(mFormatter.format(new Date()));
                        message.setUserName("Hodor");
                        mSelected.clear();
                        File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                        //Uri of camera image
                        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
                        mSelected.add(uri);
                        message.setImageList(mSelected);
                        message.setUserIcon(Uri.parse("android.resource://hk.com.dataworld.leaveapp/drawable/login_page_icon"));//Uri.parse("android.resource://com.shrikanthravi.chatviewlibrary/drawable/hodor"));
                        mChatView.addMessage(message);
                        mIsIncoming = true;
                    }
                }

            }


        }


    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mNotificationAdapter == null) {
            mListView = findViewById(R.id.drawer_list);
            mNotificationAdapter = new NotificationListAdapter(this, menu);
            ArrayList<Notification> notifications = new ArrayList<>();//getIntent().getParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION);
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

            registerBroadcastReceiver(new MessagingBroadcastReceiver());

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

    private void registerBroadcastReceiver(MessagingBroadcastReceiver mbr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("incoming");
        registerReceiver(mbr, intentFilter);
    }

    class MessagingBroadcastReceiver extends BroadcastReceiver {
        public MessagingBroadcastReceiver() {
            super();
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            receiveMessage(intent.getStringExtra("sender"), intent.getStringExtra("message"));
        }
    }
}
