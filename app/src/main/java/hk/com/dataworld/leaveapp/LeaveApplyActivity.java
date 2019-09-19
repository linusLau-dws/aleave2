package hk.com.dataworld.leaveapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.Notification;
import hk.com.dataworld.leaveapp.DAL.SimpleResultData;
import me.leolin.shortcutbadger.ShortcutBadger;

import static hk.com.dataworld.leaveapp.Constants.ACTION_INCOMING_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;
import static hk.com.dataworld.leaveapp.Constants.LONGEST_TIMEOUT_MS;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getDayOfWeekSuffixedString;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;
import static hk.com.dataworld.leaveapp.Utility.roundTo2Dp;

public class LeaveApplyActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = LeaveApplyActivity.class.getSimpleName();
    private static String baseUrl;
    String Name, Start_Date, End_Date, Date_Apply, Photo, Rejected_Reason, Approve_By;
    String Leave_Type = "";
    Double Number_Of_Days;
    int Half_Day_Ind, Approval_Status, UserId, RequestId, WorkflowTypeId, WorkflowTaskId;
    String User_Name, English_Name, Chinese_Name, Nickname, Employment_Number;
    Boolean IsAllow3Sections, IsAllowHalfDay, IsAllowHourly;
    String Balance = "";
    String Balance_As_Of_Date = "";
    int positionOtherLeave;
    int Record_Count;
    Integer La_Id;
    String ErrorCode;
    ArrayList<String> listType;
    String Result_EmpNo;
    Integer Result_UserId;
    String Result_String;
    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper dbHelper;
    private ProgressDialog pDialog;
    private Button applyButton;
    private ImageView attachedImage, cameraButton, galleryButton;
    private Uri file;
    private TextView name;
    private TextView nickname;
    private TextView RemainBalance;
    private TextView AsOfDate;
    private RadioGroup leaveGroup;
    private RadioButton sl;
    private RadioButton al;
    private RadioGroup sessionGroup;
    private RadioButton fullDay;
    private RadioButton am;
    private RadioButton pm;
    private RadioButton sections;
    private EditText startDate;
    private EditText endDate;
    //private Bitmap oldDrawable;
    //private Bitmap newDrawable;
    private Spinner otherLeaveType;
    private CheckBox mCbExcludeSat, mCbExcludeSun, mCbExcludeHolidays;
    private int mYear, mMonth, mDay;
    //Set server timeout in second
    private int serverTimeOut = 3;

    private SharedPreferences mSharedPreferences;

    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;

    private RequestQueue mRequestQueue;
    private JSONArray mDates;
    private JSONArray mBals;

    private AttachmentRecyclerViewAdapter mAttachmentAdapter;

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DWHRMS");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.i("DWHRMS", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    private void registerBroadcastReceiver(NotificationBroadcastReceiver bcr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INCOMING_NOTIFICATION);
        registerReceiver(bcr, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_apply);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        getSupportActionBar().setTitle(R.string.btn_applyleaveCamel);

        dbHelper = new SQLiteHelper(LeaveApplyActivity.this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String server_addr = extendBaseUrl(mSharedPreferences.getString(PREF_SERVER_ADDRESS, ""));
        if (!server_addr.equals("")) {
            baseUrl = server_addr;
        }

        dbHelper.openDB();
        UserContent userContent = dbHelper.getUserInfo();
        boolean hasSL = dbHelper.hasSL();
        boolean hasAL = dbHelper.hasAL();
        dbHelper.closeDB();

        User_Name = userContent.getUserName();
        UserId = userContent.getUserId();
        English_Name = userContent.getEnglishName();
        Chinese_Name = userContent.getChineseName();
        Nickname = userContent.getNickName();
        Employment_Number = userContent.getEmploymentNumber();
        IsAllow3Sections = userContent.getAllow3Sections();
        IsAllowHalfDay = userContent.getAllowHalfDay();
        IsAllowHourly = userContent.getAllowHourly();
        Record_Count = userContent.getCount();
        Log.i(TAG, "User Name is: " + User_Name);
        Log.i(TAG, "User ID: " + UserId);
        Log.i(TAG, "English Name: " + English_Name);
        Log.i(TAG, "Chinese Name: " + Chinese_Name);
        Log.i(TAG, "Employment Number: " + Employment_Number);
        Log.i(TAG, "No Record Get: " + Record_Count);
        Log.i(TAG, "IsAllow3Sections is: " + IsAllow3Sections);
        Log.i(TAG, "IsAllowHalfDay is: " + IsAllowHalfDay);
        Log.i(TAG, "IsAllowHourly is: " + IsAllowHourly);

        applyButton = (Button) findViewById(R.id.btnGet);
        //attachedImage = (ImageView) findViewById(R.id.ivAttachedImage);
        cameraButton = (ImageView) findViewById(R.id.ivAttachedImage1);
        cameraButton.setOnClickListener(this);
        galleryButton = (ImageView) findViewById(R.id.ivAttachedImage2);
        galleryButton.setOnClickListener(this);
        name = (TextView) findViewById(R.id.tvSeqNo);
        nickname = (TextView) findViewById(R.id.tvNickname);
        RemainBalance = (TextView) findViewById(R.id.tvALremain);
        RemainBalance.setText(getString(R.string.tv_remainBal, ""));
        AsOfDate = (TextView) findViewById(R.id.tvAsOfDate);
        AsOfDate.setText(getString(R.string.tv_asOfDate, ""));
        leaveGroup = (RadioGroup) findViewById(R.id.rgLeave);
        sl = (RadioButton) findViewById(R.id.rbSL);
        if (!hasSL) {
            sl.setVisibility(View.GONE);
        }
        al = (RadioButton) findViewById(R.id.rbAL);
        if (!hasAL) {
            al.setVisibility(View.GONE);
        }
        sessionGroup = (RadioGroup) findViewById(R.id.rgPeriod);
        fullDay = (RadioButton) findViewById(R.id.rbFullDay);
        am = (RadioButton) findViewById(R.id.rbAM);
        pm = (RadioButton) findViewById(R.id.rbPM);
        if (!IsAllowHalfDay) {
            am.setVisibility(View.GONE);
            pm.setVisibility(View.GONE);
        }
        sections = (RadioButton) findViewById(R.id.rbSection);
        if (!IsAllow3Sections) {
            sections.setVisibility(View.GONE);
        }
        otherLeaveType = (Spinner) findViewById(R.id.spOtherLeave);
        startDate = (EditText) findViewById(R.id.tvDateRange);
        startDate.setOnClickListener(this);
        endDate = (EditText) findViewById(R.id.etEnddate);
        endDate.setOnClickListener(this);

        fullDay.setChecked(true);
        // 2018.11.30 - Begin
        mCbExcludeSat = findViewById(R.id.exclude_sat);
        mCbExcludeSun = findViewById(R.id.exclude_sun);
        mCbExcludeHolidays = findViewById(R.id.exclude_holidays);
        // 2018.11.30 - End

        Log.i(TAG, "Nickname is: " + "[" + Nickname + "]");

        if (Nickname == null || Nickname.length() == 0 || Nickname.equals("null")) {
//            Log.i(TAG, "Empty Nickname");
            Nickname = "";
        }

        nickname.setText(getString(R.string.tv_nickname, Nickname));

        if (!IsAllow3Sections) {
            sections.setVisibility(View.GONE);
        }
        if (!IsAllowHalfDay) {
            am.setVisibility(View.GONE);
            pm.setVisibility(View.GONE);
        }

        listType = dbHelper.getLeaveDescription(Employment_Number);

        String lLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LOCALE, "");

        if (lLocale.equals("zh")) {
            if (Chinese_Name.isEmpty()) {
//                Log.i(TAG, "Language is Chinese but without Chinese name, therefore display English name");
                name.setText(getString(R.string.tv_name, English_Name));
                Name = English_Name;
            } else {
//                Log.i(TAG, "Language is Chinese, therefore display Chinese name");
                name.setText(getString(R.string.tv_name, Chinese_Name));
                Name = Chinese_Name;
            }
        } else {
            //if (lLocale.equals("en")) { //Locale.getDefault().getLanguage().equals(new Locale("en").getLanguage())
//            Log.i(TAG, "Language is English, therefore display English name");
            name.setText(getString(R.string.tv_name, English_Name));
            Name = English_Name;
            //}
        }

        ArrayAdapter<String> adapterLeaveType = new ArrayAdapter<>(this, R.layout.spinner_layout, R.id.txt, listType);
        otherLeaveType.setAdapter(adapterLeaveType);
        otherLeaveType.setOnItemSelectedListener(this);

        leaveGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // Check balance here.

//                Log.i(TAG,"Radio Button pressed");
                LeaveBalanceContent leaveBalanceContent;

                if (sl.isChecked() || al.isChecked()) {
                    dbHelper.openDB();
                    if (sl.isChecked()) {
                        leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType("SL");
                        Leave_Type = leaveBalanceContent.getLBLeaveType();
                        Balance = leaveBalanceContent.getLBLeaveBalance();
                        Balance_As_Of_Date = leaveBalanceContent.getLBLeaveBalanceAsOfDate();
                        Log.i(TAG, "Balance: " + Balance);
                        Log.i(TAG, "AsOfDate: " + Balance_As_Of_Date);
                        Log.i(TAG, "Leave_Type: " + Leave_Type);
                        if (Balance.equals("null") || Balance.isEmpty()) {
                            RemainBalance.setText(getString(R.string.tv_remainBal, "--"));
                        } else {
                            RemainBalance.setText(getString(R.string.tv_remainBal, Balance));
                        }
//                        if (Balance_As_Of_Date.equals("null") || Balance_As_Of_Date.isEmpty()) {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, "--"));
//                        } else {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, getDayOfWeekSuffixedString(LeaveApplyActivity.this, Balance_As_Of_Date)));
//                        }
                    } else if (al.isChecked()) {
                        leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType("AL");
                        Leave_Type = leaveBalanceContent.getLBLeaveType();
                        Balance = leaveBalanceContent.getLBLeaveBalance();
                        Balance_As_Of_Date = leaveBalanceContent.getLBLeaveBalanceAsOfDate();
                        Log.i(TAG, "Balance: " + Balance);
                        Log.i(TAG, "AsOfDate: " + Balance_As_Of_Date);
                        Log.i(TAG, "Leave_Type: " + Leave_Type);
                        if (Balance.equals("null") || Balance.isEmpty()) {
                            RemainBalance.setText(getString(R.string.tv_remainBal, "--"));
                        } else {
                            RemainBalance.setText(getString(R.string.tv_remainBal, Balance));
                        }
//                        if (Balance_As_Of_Date.equals("null") || Balance_As_Of_Date.isEmpty()) {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, "--"));
//                        } else {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, getDayOfWeekSuffixedString(LeaveApplyActivity.this, Balance_As_Of_Date)));
//                        }
                    }
                    otherLeaveType.setSelection(0);
                    dbHelper.closeDB();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraButton.setClickable(false);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        applyButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //newDrawable = ((BitmapDrawable) attachedImage.getDrawable()).getBitmap();

                AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this, R.style.AlertDialogCustom);

                if ((mAttachmentAdapter.getItemCount() == 0) && (sl.isChecked())) {
                    builder.setMessage(R.string.enforced_attachment).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cameraButton.requestFocus();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }

//                Log.i(TAG, "Before validate");

                validate(name.getText().toString(), RemainBalance.getText().toString(), startDate.getText().toString(), endDate.getText().toString());

//                Log.i(TAG, "After validate");

            }
        });

        RecyclerView recyclerView = findViewById(R.id.attachments_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 6));
        mAttachmentAdapter = new AttachmentRecyclerViewAdapter(this, true);
        recyclerView.setAdapter(mAttachmentAdapter);
    }

    @Override
    public void onClick(View view) {
        if (view == startDate) {
            final Calendar c = Calendar.getInstance();
            if (!startDate.getText().toString().isEmpty()) {
                Log.i(TAG, "startDate is not empty");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                try {
                    Date d = df.parse(startDate.getText().subSequence(0, 10).toString());
                    c.setTime(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            startDate.setText(getDayOfWeekSuffixedString(LeaveApplyActivity.this, String.format(Locale.ENGLISH, "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)));
                            fromDateChanged();
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        } else if (view == endDate) {
            final Calendar c = Calendar.getInstance();
            if (!endDate.getText().toString().isEmpty()) {
                Log.i(TAG, "endDate is not empty");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                try {
                    Date d = df.parse(endDate.getText().subSequence(0, 10).toString());
                    c.setTime(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            endDate.setText(getDayOfWeekSuffixedString(LeaveApplyActivity.this, String.format(Locale.ENGLISH, "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)));

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        } else if (view == cameraButton) {
            takePicture();
        } else if (view == galleryButton) {
            pickFromGallery();
        }
    }

    private void validate(String Name, String Balance, String StartDate, String EndDate) {

        if (fullDay.isChecked()) {
            Half_Day_Ind = 0;
        } else if (am.isChecked()) {
            Half_Day_Ind = 1;
        } else if (pm.isChecked()) {
            Half_Day_Ind = 2;
        } else {
            Half_Day_Ind = 3;
        }

//        Log.i(TAG,"StartDate is: " + StartDate);
//        Log.i(TAG,"EndDate is: " + EndDate);

        if (StartDate.isEmpty() || EndDate.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this, R.style.AlertDialogCustom);

//            Log.i(TAG, "Date fields be empty");

            builder.setMessage(R.string.msg_errorField).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                    Log.i(TAG, "Leave Dialog Box");
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            StartDate = StartDate.substring(0, 10);
            Date startdate = formatter.parse(StartDate);

            EndDate = EndDate.substring(0, 10);
            Date enddate = formatter.parse(EndDate);

            if (enddate.compareTo(startdate) < 0) {
//                Log.i(TAG, "End Date is Greater than Start Date");
                AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this, R.style.AlertDialogCustom);
                builder.setMessage(R.string.msg_errDate).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Log.i(TAG, "Leave Dialog Box");
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
/*
        if (sl.isChecked()) {
            CheckType = "SL";
        }
        else if (al.isChecked()) {
            CheckType = "AL";
        }
        else if (positionOtherLeave == 1) {
            CheckType = "BL";
        }
        else if (positionOtherLeave == 2) {
            CheckType = "COML";
        }
        else if (positionOtherLeave == 3) {
            CheckType = "EXAM";
        }
        else if (positionOtherLeave == 4) {
            CheckType = "IL";
        }
        else if (positionOtherLeave == 5) {
            CheckType = "JURL";
        }
        else if (positionOtherLeave == 6) {
            CheckType = "MARL";
        }
        else if (positionOtherLeave == 7) {
            CheckType = "ML";
        }
        else if (positionOtherLeave == 8) {
            CheckType = "NML";
        }
        else if (positionOtherLeave == 9) {
            CheckType = "NPL";
        }
        else if (positionOtherLeave == 10) {
            CheckType = "NSL";
        }
        else if (positionOtherLeave == 11) {
            CheckType = "PATL";
        }
        else if (positionOtherLeave == 12) {
            CheckType = "SUL";
        }
        else {
            CheckType = "";
        }

        Log.i(TAG, "Worker name is: " + Name);
        Log.i(TAG, "Leave Balance is: " + Balance);
        Log.i(TAG, "Leave Type is: " + Leave_Type);
        Log.i(TAG, "Half day ind is: " + Half_Day_Ind);
        Log.i(TAG, "Start Date is: " + StartDate);
        Log.i(TAG, "End Date is: " + EndDate);
*/

        Log.i(TAG, "Worker name is: " + Name);
        Log.i(TAG, "Leave Balance is: " + Balance);
        Log.i(TAG, "Leave Type is: " + Leave_Type);
        Log.i(TAG, "Half day ind is: " + Half_Day_Ind);
        Log.i(TAG, "Start Date is: " + StartDate);
        Log.i(TAG, "End Date is: " + EndDate);

        if ((otherLeaveType.getSelectedItemPosition() == 0) && (!al.isChecked()) && (!sl.isChecked()) || (Name.isEmpty()) || (Balance.isEmpty()) || (Leave_Type.isEmpty())) {

            if (Name.isEmpty()) {
                Log.i(TAG, "Empty Name");
            } else if (Balance.isEmpty()) {
                Log.i(TAG, "Empty Leave Balance");
            } else if (Leave_Type.isEmpty()) {
                Log.i(TAG, "Empty Leave Type");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this, R.style.AlertDialogCustom);

//            Log.i(TAG, "Some fields be empty");

            builder.setMessage(R.string.msg_errorField).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                    Log.i(TAG, "Leave Dialog Box");
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
/*
            Log.i(TAG, "All fields be inputted");
            Log.i(TAG, "Before Insert");

            Log.i(TAG, "Worker name is: " + Name);
            Log.i(TAG, "Leave Balance is: " + Balance);
            Log.i(TAG, "Leave Type is: " + Leave_Type);
            Log.i(TAG, "Half day ind is: " + Half_Day_Ind);
            Log.i(TAG, "Start Date is: " + StartDate);
            Log.i(TAG, "End Date is: " + EndDate);
*/
            Start_Date = StartDate;
            End_Date = EndDate;

            applyLeave();
        }
    }

    private void fromDateChanged() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("token", mSharedPreferences.getString(PREF_TOKEN, ""));
            obj.put("fromDate", startDate.getText().subSequence(0, 10).toString());
            obj.put("program", 0);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", baseUrl, "_GetLeaveInfo"), obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        mBals = response.getJSONArray("d").getJSONObject(0).getJSONArray("LeaveBalance");
                        clickListenerContent();
                        leaveGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                                clickListenerContent();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getGenericErrorListener(this, null));
            mRequestQueue = Volley.newRequestQueue(this);
            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void clickListenerContent() {

        AsOfDate.setText(getString(R.string.tv_asOfDate, startDate.getText().toString()));

        String code = null;
        if (sl.isChecked()) {
            code = "SL";
        } else if (al.isChecked()) {
            code = "AL";
        }

        for (int x = 0; x < mBals.length(); x++) {
            try {
                if (mBals.getJSONObject(x).getString("LeaveCode").equals(code)) {
                    double balance = mBals.getJSONObject(x).optDouble("Balance", -1d);
                    Balance = String.valueOf(balance); // Is this really necessary
                    Balance_As_Of_Date = startDate.getText().subSequence(0, 10).toString();
                    if (balance == -1d) {
                        RemainBalance.setText(getString(R.string.tv_remainBal, "--"));
                    } else {
                        RemainBalance.setText(getString(R.string.tv_remainBal, String.valueOf(roundTo2Dp(balance))));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        LeaveBalanceContent leaveBalanceContent;

        if (sl.isChecked() || al.isChecked()) {
            dbHelper.openDB();
            if (sl.isChecked()) {
                leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType("SL");
                Leave_Type = leaveBalanceContent.getLBLeaveType();
            } else if (al.isChecked()) {
                leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType("AL");
                Leave_Type = leaveBalanceContent.getLBLeaveType();
            }
            otherLeaveType.setSelection(0);
            dbHelper.closeDB();
        }
    }

    private void applyLeave() {
        // 2018.11.30 - Begin
        JSONObject includeDaysObj = new JSONObject();
        try {
            includeDaysObj.put("token", mSharedPreferences.getString(PREF_TOKEN, ""));
            includeDaysObj.put("from", Start_Date);
            includeDaysObj.put("to", End_Date);
            includeDaysObj.put("type", Half_Day_Ind);
            includeDaysObj.put("excludeSat", mCbExcludeSat.isChecked());
            includeDaysObj.put("excludeSun", mCbExcludeSun.isChecked());
            includeDaysObj.put("excludeHolidays", mCbExcludeHolidays.isChecked());
            includeDaysObj.put("program", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(LeaveApplyActivity.this);
        JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "GetIntervalDays"), includeDaysObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mDates = null;
                        try {
                            mDates = response.getJSONArray("d");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (mDates.length() == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this);
                            builder.setMessage(R.string.msg_errorNoAvailableDates)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create().show();
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < mDates.length(); i++) {
                            try {
                                sb.append(getDayOfWeekSuffixedString(LeaveApplyActivity.this, mDates.getString(i)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (i != mDates.length() - 1) {
                                sb.append("\n");
                            }
                        }
                        sb.append("\n\n");
                        sb.append(getString(R.string.tv_totalDays));
                        sb.append(mDates.length());
                        Result_String = sb.toString();
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        realApplyLeave();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //Cancel button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(LeaveApplyActivity.this);
                        builder1.setTitle(R.string.dialog_title_date_selected);
                        builder1.setMessage(Result_String).setCancelable(true).setPositiveButton(android.R.string.ok, dialogClickListener)
                                .setNegativeButton(android.R.string.cancel, dialogClickListener).show();
/*
            builder.setTitle(R.string.dialog_title_date_selected);
            builder.setMessage(Result_String).setCancelable(true).setNegativeButton(android.R.string.cancel, new DialogInterface.OnCancelListener(){}).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Leave_Type = "";
                    RemainBalance.setText(getText(R.string.tv_remainBal));
                    leaveGroup.clearCheck();
                    otherLeaveType.setEnabled(true);
                    otherLeaveType.setSelection(0);
                    fullDay.setChecked(true);
                    am.setChecked(false);
                    pm.setChecked(false);
                    sections.setChecked(false);
                    startDate.setText("");
                    endDate.setText("");
                    cameraButton.setImageResource(R.drawable.add_document);
                    oldDrawable = ((BitmapDrawable) cameraButton.getDrawable()).getBitmap();
                }
            });
*/
//        AlertDialog alert = builder.create();
//        alert.show();
                    }
                }, getGenericErrorListener(this, null));
        requestQueue.add(req);
        // 2018.11.30 - End
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                cameraButton.setClickable(true);
            }
        }
    }

    private void takePicture() {
        mIsEnableRestartBehaviour = false;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, 100);
    }

    public void pickFromGallery() {
        mIsEnableRestartBehaviour = false;
        Intent intent = new Intent(Intent.ACTION_PICK); // Action
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_OK) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(file.getEncodedPath());
                    try {
                        ExifInterface exif = new ExifInterface(file.getEncodedPath());
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        Log.i(TAG, "Exif: " + orientation);
                        Matrix matrix = new Matrix();
                        if (orientation == 6) {
                            Log.i(TAG, "Need to rotate 90 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 1024, 768);
                            matrix.postRotate(90);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else if (orientation == 3) {
                            Log.i(TAG, "Need to rotate 180 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 768, 1024);
                            matrix.postRotate(180);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else if (orientation == 8) {
                            Log.i(TAG, "Need to rotate 270 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 1024, 768);
                            matrix.postRotate(270);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else {
                            Log.i(TAG, "No rotation needed");
                            myBitmap = Utility.resizeBitmap(myBitmap, 768, 1024);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mAttachmentAdapter.add(myBitmap);
                    mAttachmentAdapter.notifyDataSetChanged();
                    //attachedImage.setImageBitmap(myBitmap);
                }
                break;
            case 101:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap myBitmap = BitmapFactory.decodeFile(picturePath);
                    try {
                        ExifInterface exif = new ExifInterface(picturePath);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        Log.i(TAG, "Exif: " + orientation);
                        Matrix matrix = new Matrix();
                        if (orientation == 6) {
                            Log.i(TAG, "Need to rotate 90 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 1024, 768);
                            matrix.postRotate(90);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else if (orientation == 3) {
                            Log.i(TAG, "Need to rotate 180 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 768, 1024);
                            matrix.postRotate(180);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else if (orientation == 8) {
                            Log.i(TAG, "Need to rotate 270 degrees");
                            myBitmap = Utility.resizeBitmap(myBitmap, 1024, 768);
                            matrix.postRotate(270);
                            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                        } else {
                            Log.i(TAG, "No rotation needed");
                            myBitmap = Utility.resizeBitmap(myBitmap, 768, 1024);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mAttachmentAdapter.add(myBitmap);
                    mAttachmentAdapter.notifyDataSetChanged();
//                    attachedImage.setImageBitmap(myBitmap);
                }
                break;
        }
    }

    public void DBCreate() {

        dbHelper.openDB();

        SQLITEDATABASE = openOrCreateDatabase("HRMSDataBase", Context.MODE_PRIVATE, null);

        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS LeaveApply(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, emp_no VARCHAR, leave_type VARCHAR, from_date VARCHAR, to_date VARCHAR, number_of_days INTEGER, date_apply VARCHAR, half_day_ind INTEGER, attached_image VARCHAR, approval_status VARCHAR, approve_by);");

        dbHelper.closeDB();
    }

    public void SubmitData2SQLiteDB() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this, R.style.AlertDialogCustom);

        Approval_Status = 1;

        Log.i(TAG, "Call SubmitData2SQLiteDB");
        Log.i(TAG, "Values for insert");

        Log.i(TAG, "Emp ID: " + Employment_Number);
        Log.i(TAG, "English Name: " + English_Name);
        Log.i(TAG, "Chinese Name: " + Chinese_Name);
        Log.i(TAG, "Leave Type: " + Leave_Type);
        Log.i(TAG, "Start Date: " + Start_Date);
        Log.i(TAG, "End Date: " + End_Date);
        Log.i(TAG, "Date Apply: " + Date_Apply);
        Log.i(TAG, "Leave Balance " + Balance);
        Log.i(TAG, "Leave Balance As Of Date: " + Balance_As_Of_Date);
        Log.i(TAG, "Half Day Ind: " + Half_Day_Ind);
        Log.i(TAG, "Attached Image: " + Photo);
        Log.i(TAG, "Approval Status: " + Approval_Status);
        Log.i(TAG, "Rejected Reason: " + Rejected_Reason);
        Log.i(TAG, "Approve By: " + Approve_By);

        Log.i(TAG, "Call Insert statement");
        LeaveContent leaveContent = new LeaveContent(La_Id, Employment_Number, English_Name, Chinese_Name, Nickname, Leave_Type, Start_Date, End_Date, Number_Of_Days, Date_Apply, Balance, Balance_As_Of_Date, Half_Day_Ind, Photo, Approval_Status, Rejected_Reason, RequestId, WorkflowTypeId, WorkflowTaskId, Approve_By, 0);

        dbHelper.openDB();

        long resultInsert = dbHelper.insertLeaveApply(leaveContent);

        if (resultInsert == -1) {
            Log.i(TAG, "Save LeaveApply fail");
            builder.setMessage(R.string.msg_infoInsertRecordFail).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    applyButton.requestFocus();
                }
            });
        }

        dbHelper.closeDB();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        resetDisconnectTimer();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spOtherLeave:
                String leavedescription = adapterView.getItemAtPosition(i).toString();

                positionOtherLeave = i;
                Log.i(TAG, "Description is: " + leavedescription);
                Log.i(TAG, "positionOtherLeave is: " + positionOtherLeave);

                if (i > 0) {
                    Log.i(TAG, "Other leave type been selected");
                    leaveGroup.clearCheck();

                    LeaveBalanceContent leaveBalanceContent;
                    dbHelper.openDB();
                    leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveDescription(leavedescription);
                    Log.i(TAG, "LEAVE BALANCE is: " + leaveBalanceContent.getLBLeaveBalance());

                    if (leaveBalanceContent.getCount() > 0) {
                        Leave_Type = leaveBalanceContent.getLBLeaveType();
                        Balance = leaveBalanceContent.getLBLeaveBalance();
                        Balance_As_Of_Date = leaveBalanceContent.getLBLeaveBalanceAsOfDate();
                        Log.i(TAG, "LEAVETYPE, BALANCE, ASOFDATE: " + "[" + Leave_Type + "]" + "[" + Balance + "]" + "[" + Balance_As_Of_Date + "]");
                        if (Balance.equals("null") || Balance.isEmpty()) {
                            RemainBalance.setText(getString(R.string.tv_remainBal, "--"));
                        } else {
                            RemainBalance.setText(getString(R.string.tv_remainBal, Balance));
                        }
//                        if (Balance_As_Of_Date.equals("null") || Balance_As_Of_Date.isEmpty()) {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, "--"));
//                        } else {
//                            AsOfDate.setText(getString(R.string.tv_asOfDate, getDayOfWeekSuffixedString(LeaveApplyActivity.this, Balance_As_Of_Date)));
//                        }
                    } else {
                        Balance = null;
                        Balance_As_Of_Date = "";
                        RemainBalance.setText(getString(R.string.tv_remainBal, "--"));
                        Leave_Type = null;
                    }
                } else {
                    Log.i(TAG, "No other leave type been selected");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mIsEnableRestartBehaviour = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

            registerBroadcastReceiver(new NotificationBroadcastReceiver());

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

    private void realApplyLeave() {
        try {
            pDialog = new ProgressDialog(LeaveApplyActivity.this);
            pDialog.setMessage(getString(R.string.leave_apply_msg_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            JSONArray tbd = new JSONArray();

            Approval_Status = 1;

            for (int x = 0; x < mDates.length(); x++) {
                JSONObject obj = new JSONObject(); //Json Object
                obj.put("LeaveCode", Leave_Type);      // 1=PR, 2=ML
                obj.put("Date", mDates.getString(x));      // "2018-12-05"
                obj.put("HalfdayInd", Half_Day_Ind);   // {0 1 2}
                if (x == 0) {
                    if (mAttachmentAdapter.getItemCount() == 0) {
                        obj.put("AttachedImage", new JSONArray());
                    } else {
                        obj.put("AttachedImage", mAttachmentAdapter.getJSONArr());
                    }
                }
                obj.put("Remarks", "");
                tbd.put(obj);
            }

            JSONObject realobj = new JSONObject();
            realobj.put("Data", tbd.toString());
            realobj.put("token", mSharedPreferences.getString(PREF_TOKEN, ""));
            realobj.put("baseURL", baseUrl);
            realobj.put("program", 0);

            mRequestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", baseUrl, "LeaveApplyActivity"),
                    realobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ArrayList<SimpleResultData> leaveApplyReceivedList = new ArrayList<>();
                    JSONObject item;
                    SimpleResultData simpleResultData;

                    try {
                        JSONArray jArray = response.getJSONArray("d");
//                        Log.i(TAG, "jArray content is: " + jArray.getString(0));
//                        Log.i(TAG, "jArray length is: " + jArray.length());
                        if (jArray.length() <= 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this);
                            Log.i(TAG, "General error from server");
                            builder.setMessage(getString(R.string.msg_errorLoginFail));
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dlg = builder.create();
                            dlg.show();
                        }
                        int i = 0;
                        while (i < jArray.length()) {
                            item = jArray.getJSONObject(i);
                            simpleResultData = new SimpleResultData();
                            simpleResultData.setUserId(item.getInt("UserID"));
                            simpleResultData.setEmploymentNumber(item.getString("EmploymentNumber"));
                            simpleResultData.setErrorCode(item.getString("ErrorCode"));
                            Log.i(TAG, "Username is: " + simpleResultData.getUserId());
                            Log.i(TAG, "EmploymentNumber is: " + simpleResultData.getEmploymentNumber());
                            Log.i(TAG, "ErrorCode is: " + simpleResultData.getErrorCode());
                            ErrorCode = simpleResultData.getErrorCode();
                            Result_UserId = simpleResultData.getUserId();
                            Result_EmpNo = simpleResultData.getEmploymentNumber();
                            i++;
                        }

                        // Formerly PostExecute
                        if (pDialog != null) {
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this);

                        if (leaveApplyReceivedList != null) {
                            if (!ErrorCode.isEmpty()) {
                                Log.i(TAG, ErrorCode);
                                builder.setMessage(ErrorCode);
                                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dlg = builder.create();
                                dlg.show();
                            } else {
                                Log.i(TAG, "leaveApplyResultList is not null");
                                Log.i(TAG, "Leave Apply success!");
                                DBCreate();
                                SubmitData2SQLiteDB();

                                Leave_Type = "";
                                RemainBalance.setText(getString(R.string.tv_remainBal, ""));
                                leaveGroup.clearCheck();
                                otherLeaveType.setEnabled(true);
                                otherLeaveType.setSelection(0);
                                fullDay.setChecked(true);
                                am.setChecked(false);
                                pm.setChecked(false);
                                sections.setChecked(false);
                                startDate.setText("");
                                endDate.setText("");

                                builder = new AlertDialog.Builder(LeaveApplyActivity.this);
                                builder.setMessage(R.string.application_sent);
                                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.create().show();
                            }
                        } else {
                            Log.i("LeaveApplyActivity: ", "Leave Apply Failed");
                            builder.setMessage(getString(R.string.msg_errorLoginFail));
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dlg = builder.create();
                            dlg.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getGenericErrorListener(this, pDialog));

            req.setRetryPolicy(new DefaultRetryPolicy(
                    LONGEST_TIMEOUT_MS,
                    0,  //Important!
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            mRequestQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            ShortcutBadger.applyCount(LeaveApplyActivity.this, count);
        }
    }

/*
private class LeaveApplyTask extends AsyncTask<String, Void, ArrayList<SimpleResultData>> {
private ProgressDialog pDialog = null;

public LeaveApplyTask() {
}

@Override
protected void onPreExecute() {
Log.i(TAG, "LeaveApplyTask");
super.onPreExecute();
pDialog = new ProgressDialog(LeaveApplyActivity.this);
pDialog.setMessage(getString(R.string.leave_apply_msg_progress));
pDialog.setIndeterminate(false);
pDialog.setCancelable(true);
pDialog.show();
}

@Override
protected ArrayList<SimpleResultData> doInBackground(String... params) {
ArrayList<SimpleResultData> leaveApplyReceivedList = new ArrayList<>();
JSONObject item;
SimpleResultData simpleResultData;

// Check server connection
try {
connectToServer = isConnectedToServer(baseUrl, serverTimeOut * 1000);
if (connectToServer) {
Log.i("connectToServer", "TRUE");
} else {
Log.i("connectToServer", "FALSE");
return null;
}
} catch (Exception e) {
e.printStackTrace();
}

Approval_Status = 1;

try {
// Prepare parameter
ArrayList<NameValuePair> nvpList = new ArrayList<>();
NameValuePair nvp;

Gson gson = new Gson();
JsonArray arr = new JsonArray();
for (int x = 0; x < mDates.length(); x++) {
JsonObject obj = new JsonObject(); //Json Object
obj.add("LeaveCode", gson.toJsonTree(Leave_Type));      // 1=PR, 2=ML
obj.add("Date", gson.toJsonTree(mDates.getString(x)));      // "2018-12-05"
obj.add("HalfdayInd", gson.toJsonTree(Half_Day_Ind));   // {0 1 2}
if (x == 0) {
if (mAttachmentAdapter.getItemCount() == 0) {
obj.add("AttachedImage",new JsonArray());
} else {
obj.add("AttachedImage", mAttachmentAdapter.getGsonArr());
}
}
arr.add(obj);
}
String jsonString = gson.toJson(arr);
Log.i("jsonString", jsonString.substring(0, 8));

nvp = new NameValuePair();
nvp.setName("Data");
nvp.setValue(jsonString);
nvpList.add(nvp);

nvp = new NameValuePair();
nvp.setName("token");
nvp.setValue(mSharedPreferences.getString(PREF_TOKEN, ""));
nvpList.add(nvp);

nvp = new NameValuePair();
nvp.setName("baseURL");
nvp.setValue(mSharedPreferences.getString(PREF_SERVER_ADDRESS, ""));
nvpList.add(nvp);

Log.i(TAG, nvpList.get(0).getName());
Log.i(TAG, nvpList.get(0).getValue().toString());
Log.i(TAG, nvpList.get(1).getName());
Log.i(TAG, nvpList.get(1).getValue().toString());


JSONParser jParser = new JSONParser();
JSONObject json = jParser.getJSONFromUrl(baseUrl + "LeaveApplyActivity", nvpList);
JSONArray jArray = json.getJSONArray("d");
Log.i(TAG, "jArray content is: " + jArray.getString(0));
Log.i(TAG, "jArray length is: " + jArray.length());
if (jArray.length() <= 0) {
AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApplyActivity.this);
Log.i(TAG, "General error from server");
builder.setMessage(getString(R.string.msg_errorLoginFail));
builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int id) {
dialog.dismiss();
}
});
AlertDialog dlg = builder.create();
dlg.show();
return leaveApplyReceivedList;
}
int i = 0;
while (i < jArray.length()) {
item = jArray.getJSONObject(i);
simpleResultData = new SimpleResultData();
simpleResultData.setUserId(item.getInt("UserID"));
simpleResultData.setEmploymentNumber(item.getString("EmploymentNumber"));
simpleResultData.setErrorCode(item.getString("ErrorCode"));
Log.i(TAG, "Username is: " + simpleResultData.getUserId());
Log.i(TAG, "EmploymentNumber is: " + simpleResultData.getEmploymentNumber());
Log.i(TAG, "ErrorCode is: " + simpleResultData.getErrorCode());
ErrorCode = simpleResultData.getErrorCode();
Result_UserId = simpleResultData.getUserId();
Result_EmpNo = simpleResultData.getEmploymentNumber();
i++;
}
} catch (Exception ex) {
Log.e(TAG, ex.toString());
}


return leaveApplyReceivedList;
}

protected void onPostExecute(ArrayList<SimpleResultData> leaveApplyReceivedList) {
}
}
*/
}
