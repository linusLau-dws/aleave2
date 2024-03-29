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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import hk.com.dataworld.leaveapp.DAL.Notification;
import hk.com.dataworld.leaveapp.DAL.RequestLeaveApprovalListResultData;
import me.leolin.shortcutbadger.ShortcutBadger;

import static hk.com.dataworld.leaveapp.Constants.ACTION_INCOMING_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BACK_FROM_DETAIL_CONFIRM;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_BROADCAST_NOTIFICATION_COUNT;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SHIM_NOTIFICATION;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_SOURCE_NOTIFICATION_STATUS;
import static hk.com.dataworld.leaveapp.Constants.EXTRA_TO_MY_HISTORY;
import static hk.com.dataworld.leaveapp.Constants.LONGEST_TIMEOUT_MS;
import static hk.com.dataworld.leaveapp.Constants.PREF_ITEM;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Constants.STATUS_APPROVED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CONFIRMED_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_PENDING_CANCEL;
import static hk.com.dataworld.leaveapp.Constants.STATUS_REJECTED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_RELEGATED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_WAITING;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;
import static hk.com.dataworld.leaveapp.Utility.roundTo2Dp;

public class LeaveMasterRecordActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = LeaveMasterRecordActivity.class.getSimpleName();
    private String mBaseUrl;
    private String mToken;
    private ProgressDialog pDialog;
    private RequestQueue mQueue;
    private ArrayList<Notification> mReceived = new ArrayList<>();

    private SQLiteHelper SQLITEHELPER, dbHelper;
    private SQLiteDatabase SQLITEDATABASE;
    private Cursor cursor;
    private SQLiteLeaveListAdapter ListAdapter;
    private ArrayList<String> ID_ArrayList = new ArrayList<>();
    private ArrayList<String> APPROVAL_STATUS_ArrayList = new ArrayList<>();
    private ArrayList<String> NAME_ArrayList = new ArrayList<>();
    private ArrayList<String> LEAVE_TYPE_ArrayList = new ArrayList<>();
    private ArrayList<String> START_DATE_ArrayList = new ArrayList<>();
    private ArrayList<String> END_DATE_ArrayList = new ArrayList<>();
    private ArrayList<String> TOTAL_DAYS_ArrayList = new ArrayList<>();
    private ArrayList<String> REMAIN_DAYS_ArrayList = new ArrayList<>();
    private ListView LISTVIEW;
    private RadioGroup StatusGroup;
    private RadioButton PendingRB, RejectedRB, AcceptedRB, WithdrewOrTransferRB;
    private Integer POSITION;
    private boolean mToMyHistory;
    private int BackFromLeaveApprovalStatus = 0;
    private boolean BackFromLeaveApprovalDetail = false;
    private boolean BackFromDetailConfirm = false;
    private Integer Status = null;
    private Integer Status2 = null;
    private Integer RetrievePosition = 0;
    private TextView Heading;
    private EditText FromDate, ToDate;
    private String FromDate_Str = "1900-01-01", ToDate_Str = "2047-12-31";
    private Integer mYear, mMonth, mDay;
    private String Usr_Employment_number, ErrorCode;
    private String La_Approve_By;
    private Integer LA_Half_Day_Ind;
    private Integer Usr_UserId;
    private Integer Record_Count;
    private JSONArray LeaveApprovalList;
    private Integer LeaveList_UserID, LeaveList_ApprovalStatus, LeaveList_RequestID, LeaveList_WorkflowTypeID, LeaveList_WorkflowTaskID;
    private Double LeaveList_TotalDayApply;
    private String LeaveList_Balance;
    private String LeaveList_EmploymentNumber, LeaveList_Nickname, LeaveList_ChineseName, LeaveList_EnglishName, LeaveList_LeaveCode, LeaveList_FromDate, LeaveList_ToDate, LeaveList_DateApplyList, LeaveList_BalanceAsOfDate, LeaveList_RejectedReason, LeaveList_AttachedImage;
    private Integer La_Id;

    private SharedPreferences mSharedPreferences;

    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;

    private void registerBroadcastReceiver(NotificationBroadcastReceiver bcr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INCOMING_NOTIFICATION);
        registerReceiver(bcr, intentFilter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_master_record);

        Log.i(TAG, "Enter onCreate LeaveMasterRecordActivity");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mBaseUrl = extendBaseUrl(mSharedPreferences.getString(PREF_SERVER_ADDRESS, ""));
        mToken = mSharedPreferences.getString(PREF_TOKEN, "");


        dbHelper = new SQLiteHelper(LeaveMasterRecordActivity.this);
        dbHelper.openDB();

        UserContent userContent = dbHelper.getUserInfo();

        Usr_UserId = userContent.getUserId();
        Usr_Employment_number = userContent.getEmploymentNumber();
        Record_Count = userContent.getCount();
        Log.i(TAG, "User employment number " + Usr_Employment_number);
        Log.i(TAG, "No Record Get: " + Record_Count);

        dbHelper.closeDB();

        Heading = (TextView) findViewById(R.id.tvHeadingTitle);
        FromDate = (EditText) findViewById(R.id.etFromDate);
        FromDate.setOnClickListener(this);
        ToDate = (EditText) findViewById(R.id.etToDate);
        ToDate.setOnClickListener(this);

        StatusGroup = (RadioGroup) findViewById(R.id.rgStatus);
        PendingRB = (RadioButton) findViewById(R.id.rbPending);
        RejectedRB = (RadioButton) findViewById(R.id.rbRejected);
        AcceptedRB = (RadioButton) findViewById(R.id.rbAccepted);
        WithdrewOrTransferRB = (RadioButton) findViewById(R.id.rbWithdrewOrTransfer);

        mToMyHistory = getIntent().getBooleanExtra(EXTRA_TO_MY_HISTORY, true);
        if (!mToMyHistory) {
            Heading.setText(R.string.btn_leaveapproval);
            WithdrewOrTransferRB.setText(R.string.tv_tranferStatus);
            getSupportActionBar().setTitle(R.string.btn_leaveapprovalCamel);
        } else {
            Heading.setText(R.string.btn_myapplication);
            WithdrewOrTransferRB.setText(R.string.tv_cancelledStatus);
            getSupportActionBar().setTitle(R.string.btn_myapplicationCamel);
        }
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, 0);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 2);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "gb"));
        FromDate.setText(simpleDateFormat.format(startDate.getTime()));
        ToDate.setText(simpleDateFormat.format(endDate.getTime()));

        StatusGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                dbHelper.openDB();
                long resultDeleteLeaveApply = dbHelper.deleteLeaveApply();
                if (resultDeleteLeaveApply == 0) {
                    Log.i(TAG, "No Leave record found");
                } else {
                    Log.i(TAG, "Leave record successfully deleted");
                }
                dbHelper.closeDB();
                if (PendingRB.isChecked()) {
                    Log.i(TAG, "Pending Radio Button been pressed");
                    Status = STATUS_WAITING;
                    Status2 = STATUS_PENDING_CANCEL;
                    if (mToMyHistory) {
                        validate(FromDate.getText().toString(), ToDate.getText().toString());
                        MyHistory();
                    } else {
                        GetApprovals();
                    }
                } else if (RejectedRB.isChecked()) {
                    Log.i(TAG, "Rejected Radio Button been pressed");
                    Status = STATUS_REJECTED;
                    Status2 = 99; //dummy value
                    if (mToMyHistory) {
                        validate(FromDate.getText().toString(), ToDate.getText().toString());
                        MyHistory();
                    } else {
                        GetApprovals();
                    }
                } else if (AcceptedRB.isChecked()) {
                    Log.i(TAG, "Accepted Radio Button been pressed");
                    Status = STATUS_APPROVED;
                    Status2 = 99; //dummy value
                    if (mToMyHistory) {
                        validate(FromDate.getText().toString(), ToDate.getText().toString());
                        MyHistory();
                    } else {
                        GetApprovals();
                    }
                } else if (WithdrewOrTransferRB.isChecked()) {
                    if (mToMyHistory) {
                        Log.i(TAG, "Withdraw Radio Button been pressed");
                        Status = STATUS_CANCELLED;
                        Status2 = STATUS_CONFIRMED_CANCELLED;
                        validate(FromDate.getText().toString(), ToDate.getText().toString());
                        MyHistory();
                    } else {
                        Log.i(TAG, "Transfer Radio Button been pressed");
                        Status = STATUS_RELEGATED;  // 7
                        Status2 = STATUS_WAITING;
                        GetApprovals();
                    }
                } else {
                    Log.i(TAG, "No Radio Button been pressed");
                }
            }
        });

        LISTVIEW = (ListView) findViewById(R.id.lvLeave);

        SQLITEHELPER = new SQLiteHelper(this);

        dbHelper = new SQLiteHelper(LeaveMasterRecordActivity.this);

        LISTVIEW.setAdapter(ListAdapter);

        LISTVIEW.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "Item Position is: " + (i));
                Log.i(TAG, "Array ID is: " + ID_ArrayList.get(i));
                POSITION = (Integer.parseInt(ID_ArrayList.get(i))) - 1;
                Log.i(TAG, "POSITION is: " + POSITION);
                Intent intent = new Intent(getBaseContext(), LeaveApprovalActivity.class);
                intent.putExtra("position", POSITION);
                intent.putExtra("item_position", i);
                intent.putExtra(EXTRA_SHIM_NOTIFICATION, mReceived);
                //TODO

                if (mToMyHistory) {
                    intent.putExtra(EXTRA_TO_MY_HISTORY, true);
                }

                mIsEnableRestartBehaviour = false;
                startActivityForResult(intent, 1);
            }
        });

        if (getIntent().hasExtra(EXTRA_SOURCE_NOTIFICATION_STATUS)) {
            switch (getIntent().getIntExtra(EXTRA_SOURCE_NOTIFICATION_STATUS, 0)) {
                case STATUS_APPROVED:
                    AcceptedRB.toggle();
                    break;
                case STATUS_REJECTED:
                    RejectedRB.toggle();
                    break;
                case STATUS_CONFIRMED_CANCELLED:
                    WithdrewOrTransferRB.toggle();
                    break;
            }
        }

        PendingRB.toggle();
    }

    @Override
    public void onClick(View view) {
        if (view == FromDate) {
            LISTVIEW.setAdapter(null);
            StatusGroup.clearCheck();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date d = df.parse(FromDate.getText().toString());
                Log.i(TAG, "TEST DATE is:" + d);
                final Calendar c = Calendar.getInstance();
                c.setTime(d);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Log.i(TAG, "Start Date is: " + year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth));

                                FromDate.setText(String.format("%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (view == ToDate) {
            LISTVIEW.setAdapter(null);
            StatusGroup.clearCheck();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date d = df.parse(ToDate.getText().toString());
                final Calendar c = Calendar.getInstance();
                c.setTime(d);
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Log.i(TAG, "End Date is: " + year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth));

                                ToDate.setText(String.format("%02d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void validate(String StartDate, String EndDate) {

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            String strStart = StartDate;
            Date startdate = formatter.parse(strStart);

            String strEnd = EndDate;
            Date enddate = formatter.parse(strEnd);

            if (enddate.compareTo(startdate) < 0) {
                Log.i(TAG, "End Date is Greater than Start Date");
                AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMasterRecordActivity.this, R.style.AlertDialogCustom);
                builder.setMessage(R.string.msg_errDate).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            } else {
                long DaysBetween = Daysbetween(StartDate, EndDate, "yyyy-MM-dd");
                if (DaysBetween + 1 > 180) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMasterRecordActivity.this, R.style.AlertDialogCustom);
                    builder.setMessage(R.string.msg_dateRange).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if ((StartDate.isEmpty()) || (EndDate.isEmpty())) {
            if (StartDate.isEmpty()) {
//                Log.i(TAG, "Empty Start Date");
                FromDate.requestFocus();
            } else {
//                Log.i(TAG, "Empty End Date");
                ToDate.requestFocus();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMasterRecordActivity.this, R.style.AlertDialogCustom);

//            Log.i(TAG, "Some fields be empty");

            builder.setMessage(R.string.msg_errorField).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Log.i(TAG, "Validation completed");
        }
    }

    public long Daysbetween(String date1, String date2, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        Date Date1 = null, Date2 = null;
        try {
            Date1 = sdf.parse(date1);
            Date2 = sdf.parse(date2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (Date2.getTime() - Date1.getTime()) / (24 * 60 * 60 * 1000);
    }

    public void DBCreate() {

        dbHelper.openDB();

        SQLITEDATABASE = openOrCreateDatabase("HRMSDataBase", Context.MODE_PRIVATE, null);

        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS LeaveApply(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, emp_no VARCHAR, leave_type VARCHAR, from_date VARCHAR, to_date VARCHAR, number_of_days INTEGER, date_apply VARCHAR, half_day_ind INTEGER, attached_image VARCHAR, approval_status VARCHAR, approve_by VARCHAR);");

        dbHelper.closeDB();
    }

    private void ShowSQLiteDBdata(Integer status1, Integer status2, String fromDate, String toDate) {

        Log.i(TAG, "Enter ShowSQLiteDBdata");

        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();

        dbHelper.openDB();

        String[] args = {status1.toString(), status2.toString(), fromDate, toDate};

        cursor = SQLITEDATABASE.rawQuery(
                "SELECT _id, approval_status, englishname, chinesename, leave_type, from_date, to_date, number_of_days, balance FROM LeaveApply " +
                        " WHERE (approval_status = ? OR approval_status = ?) AND from_date >= ? AND to_date <= ? " +
                        " ORDER BY from_date DESC, approval_status DESC, _id DESC ",
                args, null);

        Log.i(TAG, "Set Cursor for LeaveApply");

        ID_ArrayList.clear();
        APPROVAL_STATUS_ArrayList.clear();
        NAME_ArrayList.clear();
        LEAVE_TYPE_ArrayList.clear();
        START_DATE_ArrayList.clear();
        END_DATE_ArrayList.clear();
        TOTAL_DAYS_ArrayList.clear();
        REMAIN_DAYS_ArrayList.clear();

        Log.i(TAG, "Set Array List");

        if (cursor.moveToFirst()) {
            do {
                ID_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_ID)));

                if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 1) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_pendingStatus));
                } else if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 2) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_acceptStatus));
                } else if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 3) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_rejectStatus));
                } else if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 4) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_cancelledStatus));
                } else if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 5) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_pendingforcancelStatus));
                } else if (cursor.getInt(cursor.getColumnIndex(SQLiteHelper.LA_Approval_Status)) == 6) {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_confirmedCancelledStatus));
                } else {
                    APPROVAL_STATUS_ArrayList.add(getString(R.string.tv_undefineStatus));
                }

                LeaveBalanceContent leaveBalanceContent;
                dbHelper.openDB();
                leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)));
                if (leaveBalanceContent.getCount() == 0) {
                    LEAVE_TYPE_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)));
                } else {
                    LEAVE_TYPE_ArrayList.add(leaveBalanceContent.getLBLeaveDescription());
                }

                String lLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_LOCALE, "");
                if (lLocale.equals("en")) {
                    NAME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_EnglishName)));
                } else if (lLocale.equals("zh")) {
                    if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_ChineseName)).isEmpty()) {
                        NAME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_EnglishName)));
                    } else {
                        NAME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_ChineseName)));
                    }
                }
/*
                if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("AL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_al).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("SL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_sl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("BL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_bl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("COML")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_cpl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("EXAM")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_el).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("IL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_il).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("JURL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_jl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("MARL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_marl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("ML")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_matl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("NML")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_nml).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("NPL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_npl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("NSL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_nsl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("PATL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_pl).toString());
                } else if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)).equals("SUL")) {
                    LEAVE_TYPE_ArrayList.add(getText(R.string.et_stl).toString());
                } else {
                    LEAVE_TYPE_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Leave_Type)));
                }
*/
                START_DATE_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_From_Date)));
                END_DATE_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_To_Date)));
                TOTAL_DAYS_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Number_Of_Days)));

                if (cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Balance)).equals("null") || cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Balance)).isEmpty()) {
                    REMAIN_DAYS_ArrayList.add("--");
                } else {
                    REMAIN_DAYS_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_Balance)));
                }
            } while (cursor.moveToNext());
        }

        ListAdapter = new SQLiteLeaveListAdapter(LeaveMasterRecordActivity.this,

                ID_ArrayList,
                APPROVAL_STATUS_ArrayList,
                NAME_ArrayList,
                LEAVE_TYPE_ArrayList,
                START_DATE_ArrayList,
                END_DATE_ArrayList,
                TOTAL_DAYS_ArrayList,
                REMAIN_DAYS_ArrayList
        );

        LISTVIEW.setAdapter(ListAdapter);

        cursor.close();

        dbHelper.closeDB();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetDisconnectTimer();
        if (BackFromLeaveApprovalDetail) {
            Log.i(TAG, "CONDITION A");
            LISTVIEW = (ListView) findViewById(R.id.lvLeave);
            SQLITEHELPER = new SQLiteHelper(this);
            dbHelper = new SQLiteHelper(LeaveMasterRecordActivity.this);
            LISTVIEW.setAdapter(ListAdapter);
            ShowSQLiteDBdata(BackFromLeaveApprovalStatus, Status2, FromDate_Str, ToDate_Str);
            RetrievePosition = PreferenceManager.getDefaultSharedPreferences(LeaveMasterRecordActivity.this).getInt(PREF_ITEM, 0);
            LISTVIEW.setSelection(RetrievePosition);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
//        if (this instanceof LeaveMasterRecordActivity) {
//            Log.i(TAG, "onStop");
//            stopDisconnectTimer();
//        } else {
//            LISTVIEW.destroyDrawingCache();
//            LISTVIEW.removeAllViews();
//            Log.i(TAG, "onStop destroyDrawingCache removeAllViews ");
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.hasExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS)) {
                    BackFromLeaveApprovalStatus = data.getIntExtra("BackFromLeaveApprovalStatus", 0);
                    Log.i(TAG, "Back from Leave Approval Detail Approval Status: " + BackFromLeaveApprovalStatus);
                    if (BackFromLeaveApprovalStatus == 1 || BackFromLeaveApprovalStatus == 5) {
                        if (BackFromLeaveApprovalStatus == 1) {
                            Status2 = 5;
                        } else {
                            Status2 = 1;
                        }
                        PendingRB.setChecked(true);
                    } else if (BackFromLeaveApprovalStatus == 2) {
                        Status2 = BackFromLeaveApprovalStatus;
                        AcceptedRB.setChecked(true);
                    } else if (BackFromLeaveApprovalStatus == 3) {
                        Status2 = BackFromLeaveApprovalStatus;
                        RejectedRB.setChecked(true);
                    } else {
                        if (BackFromLeaveApprovalStatus == 4) {
                            Status2 = 6;
                        } else {
                            Status2 = 4;
                        }
                        WithdrewOrTransferRB.setChecked(true);
                    }
                }

                if (data.hasExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL)) {
                    BackFromLeaveApprovalDetail = data.getBooleanExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL, false);
                }

                if (data.hasExtra(EXTRA_TO_MY_HISTORY)) {
                    mToMyHistory = data.getBooleanExtra(EXTRA_TO_MY_HISTORY, false);
                }

                if (data.hasExtra(EXTRA_BACK_FROM_DETAIL_CONFIRM)) {
                    BackFromDetailConfirm = data.getBooleanExtra(EXTRA_BACK_FROM_DETAIL_CONFIRM, false);

                    dbHelper.openDB();
                    long resultDeleteLeaveApply = dbHelper.deleteLeaveApply();
                    if (resultDeleteLeaveApply == 0) {
                        Log.i(TAG, "No Leave Apply record found");
                    } else {
                        Log.i(TAG, "Leave Apply record successfully deleted");
                    }
                    dbHelper.closeDB();

                    if (data.getBooleanExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, false)) {
                        GetApprovals();
                    } else {
                        MyHistory();
                    }
                }

            } else if (resultCode == 0) {
                Log.i(TAG, "RESULT CANCELLED");
            }
        }
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mNotificationAdapter == null) {
            mListView = findViewById(R.id.drawer_list);
            mNotificationAdapter = new NotificationListAdapter(this, menu);
            ArrayList<Notification> notifications = new ArrayList<>();
            if (getIntent().hasExtra(EXTRA_SHIM_NOTIFICATION)) {
                notifications = getIntent().getParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION);
                mNotificationAdapter.addItems(notifications);
            }
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
                }
            });
        }
        return true;
    }

    //region Approval
    private void GetApprovals() {
        try {
            pDialog = new ProgressDialog(LeaveMasterRecordActivity.this);
            pDialog.setMessage(getString(R.string.request_leave_approval_list_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("ApprovalStatus", Status);
            obj.put("token", mToken);
            obj.put("FromDate", FromDate.getText().toString());
            obj.put("ToDate", ToDate.getText().toString());
            obj.put("program", 0);

            mQueue = Volley.newRequestQueue(this);

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "RequestLeaveApprovalListActivity"),
                    obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        ArrayList<RequestLeaveApprovalListResultData> requestLeaveApprovalListResultList = new ArrayList<>();
                        ArrayList<LeaveContent> leaveList = new ArrayList<>();
                        LeaveContent leave;
                        JSONObject itemUserInfo;
                        RequestLeaveApprovalListResultData requestLeaveApprovalListResultData;

                        JSONArray jArray = response.getJSONArray("d");
                        Log.i(TAG, "asdf size is: " + jArray.length());
                        String bytelength = jArray.getString(0);
                        Log.i(TAG, "asdf size in bytes is: " + bytelength.getBytes().length);
                        DBCreate();
                        for (int i = 0; i < jArray.length(); i++) {
                            itemUserInfo = jArray.getJSONObject(i);
                            requestLeaveApprovalListResultData = new RequestLeaveApprovalListResultData();
                            requestLeaveApprovalListResultData.setErrorCode(itemUserInfo.getString("ErrorCode"));
                            ErrorCode = requestLeaveApprovalListResultData.getErrorCode();

                            requestLeaveApprovalListResultList.add(requestLeaveApprovalListResultData);
                            Log.i(TAG, "ErrorCode is: " + requestLeaveApprovalListResultData.getErrorCode());

                            if (ErrorCode.isEmpty()) {
                                requestLeaveApprovalListResultData.setLeaveApprovalList(itemUserInfo.getJSONArray("LeaveApprovalList"));
                                Log.i(TAG, "LeaveApprovalList content is: " + requestLeaveApprovalListResultData.getLeaveApprovalList());
                                LeaveApprovalList = requestLeaveApprovalListResultData.getLeaveApprovalList();
                                Log.i(TAG, "LeaveApprovalList array length is: " + LeaveApprovalList.length());

                                for (int j = 0; j < LeaveApprovalList.length(); ++j) {
                                    JSONObject subMenuObject = LeaveApprovalList.getJSONObject(j);
                                    La_Id = j;
                                    LeaveList_UserID = 1;
                                    LeaveList_EmploymentNumber = subMenuObject.getString("EmploymentNumber");
                                    LeaveList_Nickname = subMenuObject.isNull("Nickname") ? "" : subMenuObject.getString("Nickname");
                                    LeaveList_ChineseName = subMenuObject.getString("ChineseName");
                                    LeaveList_EnglishName = subMenuObject.getString("EnglishName");
                                    LeaveList_LeaveCode = subMenuObject.getString("LeaveCode");
                                    LeaveList_FromDate = subMenuObject.getString("FromDate");
                                    LeaveList_ToDate = subMenuObject.getString("ToDate");
                                    LeaveList_TotalDayApply = subMenuObject.getDouble("TotalDayApply");
                                    if (subMenuObject.isNull("Balance") || subMenuObject.getString("Balance").equals("null")) {
                                        LeaveList_Balance = "--";
                                    } else if (subMenuObject.getDouble("Balance") == 0d) {
                                        LeaveList_Balance = "0.0";
                                    } else {
                                        LeaveList_Balance = String.valueOf(roundTo2Dp(subMenuObject.getDouble("Balance")));
                                    }
                                    LeaveList_BalanceAsOfDate = subMenuObject.getString("FromDate"); //Same as FromDate
                                    LeaveList_DateApplyList = "";
                                    LeaveList_ApprovalStatus = Status;
                                    LeaveList_RejectedReason = "";
                                    LeaveList_RequestID = subMenuObject.getInt("RequestID");
                                    LeaveList_WorkflowTypeID = 0;
                                    LeaveList_WorkflowTaskID = subMenuObject.getInt("WorkflowTaskID"); //TODO: Use this?
                                    Log.i("Leave_Ind", " " + LeaveList_WorkflowTaskID);
                                    LeaveList_AttachedImage = "";

                                    leave = new LeaveContent(La_Id, LeaveList_EmploymentNumber, LeaveList_EnglishName, LeaveList_ChineseName, LeaveList_Nickname, LeaveList_LeaveCode, LeaveList_FromDate, LeaveList_ToDate, LeaveList_TotalDayApply, LeaveList_DateApplyList, LeaveList_Balance, LeaveList_BalanceAsOfDate, LA_Half_Day_Ind, LeaveList_AttachedImage, LeaveList_ApprovalStatus, LeaveList_RejectedReason, LeaveList_RequestID, LeaveList_WorkflowTypeID, LeaveList_WorkflowTaskID, La_Approve_By, 0);
                                    leaveList.add(leave);
                                    //region Log
                                    Log.i(TAG, "Record Number is: " + "[" + j + "]");
                                    Log.i(TAG, "LeaveList_UserID is: " + "[" + LeaveList_UserID + "]");
                                    Log.i(TAG, "LeaveList_EmploymentNumber is: " + "[" + LeaveList_EmploymentNumber + "]");
                                    Log.i(TAG, "LeaveList_Nickname is: " + "[" + LeaveList_Nickname + "]");
                                    Log.i(TAG, "LeaveList_ChineseName is: " + "[" + LeaveList_ChineseName + "]");
                                    Log.i(TAG, "LeaveList_EnglishName is: " + "[" + LeaveList_EnglishName + "]");
                                    Log.i(TAG, "LeaveList_LeaveCode is: " + "[" + LeaveList_LeaveCode + "]");
                                    Log.i(TAG, "LeaveList_FromDate is: " + "[" + LeaveList_FromDate + "]");
                                    Log.i(TAG, "LeaveList_ToDate is: " + "[" + LeaveList_ToDate + "]");
                                    Log.i(TAG, "LeaveList_TotalDayApply is: " + "[" + LeaveList_TotalDayApply + "]");
                                    Log.i(TAG, "LeaveList_Balance is: " + "[" + LeaveList_Balance + "]");
                                    Log.i(TAG, "LeaveList_DateApplyList is: " + "[" + LeaveList_DateApplyList + "]");
                                    Log.i(TAG, "LeaveList_ApprovalStatus is: " + "[" + LeaveList_ApprovalStatus + "]");
                                    Log.i(TAG, "LeaveList_RejectedReason is: " + "[" + LeaveList_RejectedReason + "]");
                                    Log.i(TAG, "LeaveList_RequestID is: " + "[" + LeaveList_RequestID + "]");
                                    Log.i(TAG, "LeaveList_WorkflowTypeID is: " + "[" + LeaveList_WorkflowTypeID + "]");
                                    Log.i(TAG, "LeaveList_WorkflowTaskID is: " + "[" + LeaveList_WorkflowTaskID + "]");
                                    Log.i(TAG, "LeaveList_AttachedImage is: " + "[" + LeaveList_AttachedImage + "]");
//endregion
                                }
                                if (!(leaveList.isEmpty())) {
                                    dbHelper.openDB();
                                    dbHelper.insertLeaveList(leaveList);
                                    dbHelper.closeDB();
                                }
                            }
                        }

                        // Formerly PostExecute
                        try {
                            if (pDialog != null) {
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            pDialog = null;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMasterRecordActivity.this);

                        if (requestLeaveApprovalListResultList != null) {
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
                                Log.i(TAG, "Error Code is Empty");
                                Log.i(TAG, "FromDate and ToDate: " + FromDate_Str + " " + ToDate_Str);
                                ShowSQLiteDBdata(Status, Status2, FromDate_Str, ToDate_Str);
                                if (BackFromDetailConfirm) {
                                    RetrievePosition = PreferenceManager.getDefaultSharedPreferences(LeaveMasterRecordActivity.this).getInt(PREF_ITEM, 0);
                                    LISTVIEW.setSelection(RetrievePosition);
                                }
                            }
                        } else {
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
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            mQueue.add(req);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //region My requests
    private void MyHistory() {
        try {
            pDialog = new ProgressDialog(LeaveMasterRecordActivity.this);
            pDialog.setMessage(getString(R.string.request_leave_history_list_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

            JSONObject data = new JSONObject();
            data.put("UserID", Usr_UserId);
            data.put("EmploymentNumber", Usr_Employment_number);
            data.put("FromDate", FromDate.getText().toString());
            data.put("ToDate", ToDate.getText().toString());
            data.put("ApprovalStatus", Status);

            JSONArray tbd = new JSONArray();
            tbd.put(data);

            JSONObject realobj = new JSONObject();
            realobj.put("Data", tbd.toString());
            realobj.put("token", mToken);
            realobj.put("program", 0);
            Log.i("Testtest", realobj.toString());

            mQueue = Volley.newRequestQueue(this);

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST,
                    String.format("%s%s", mBaseUrl, "RequestLeaveHistoryListActivity"),
                    realobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray d = response.getJSONArray("d");

                        ArrayList<RequestLeaveApprovalListResultData> requestLeaveApprovalListResultList = new ArrayList<>();
                        ArrayList<LeaveContent> leaveList = new ArrayList<>();
                        LeaveContent leave;
                        JSONObject itemUserInfo;
                        RequestLeaveApprovalListResultData requestLeaveApprovalListResultData;

                        DBCreate();
                        for (int i = 0; i < d.length(); i++) {
                            itemUserInfo = d.getJSONObject(i);
                            requestLeaveApprovalListResultData = new RequestLeaveApprovalListResultData();
                            requestLeaveApprovalListResultData.setErrorCode(itemUserInfo.getString("ErrorCode"));
                            requestLeaveApprovalListResultData.setUserId(itemUserInfo.getInt("UserID"));
                            requestLeaveApprovalListResultData.setChineseName(itemUserInfo.getString("ChineseName"));
                            requestLeaveApprovalListResultData.setEnglishName(itemUserInfo.getString("EnglishName"));
                            requestLeaveApprovalListResultData.setEmploymentNumber(itemUserInfo.getString("EmploymentNumber"));
                            requestLeaveApprovalListResultData.setNickname(itemUserInfo.getString("Nickname"));
                            ErrorCode = requestLeaveApprovalListResultData.getErrorCode();
                            LeaveList_UserID = requestLeaveApprovalListResultData.getUserId();
                            LeaveList_ChineseName = requestLeaveApprovalListResultData.getChineseName();
                            LeaveList_EnglishName = requestLeaveApprovalListResultData.getEnglishName();
                            LeaveList_EmploymentNumber = requestLeaveApprovalListResultData.getEmploymentNumber();
                            LeaveList_Nickname = requestLeaveApprovalListResultData.getNickname();

                            requestLeaveApprovalListResultList.add(requestLeaveApprovalListResultData);
                            Log.i(TAG, "ErrorCode is: " + requestLeaveApprovalListResultData.getErrorCode());

                            if (!ErrorCode.isEmpty()) {
                                Log.i(TAG, "Error Code is not empty");
                            } else {
                                requestLeaveApprovalListResultData.setLeaveApprovalList(itemUserInfo.getJSONArray("LeaveHistoryList"));
                                Log.i(TAG, "LeaveHistoryList content is: " + requestLeaveApprovalListResultData.getLeaveApprovalList());
                                LeaveApprovalList = requestLeaveApprovalListResultData.getLeaveApprovalList();
                                Log.i(TAG, "LeaveHistoryList array length is: " + LeaveApprovalList.length());

                                for (int j = 0; j < LeaveApprovalList.length(); j++) {
                                    JSONObject subMenuObject = LeaveApprovalList.getJSONObject(j);
                                    La_Id = j;
                                    LeaveList_LeaveCode = subMenuObject.getString("LeaveCode");
                                    LeaveList_FromDate = subMenuObject.getString("FromDate");
                                    LeaveList_ToDate = subMenuObject.getString("ToDate");
                                    LeaveList_TotalDayApply = subMenuObject.getDouble("TotalDayApply");
                                    if (subMenuObject.isNull("Balance") || subMenuObject.getString("Balance").equals("null")) {
                                        LeaveList_Balance = "--";
                                    } else if (subMenuObject.getDouble("Balance") == 0d) {
                                        LeaveList_Balance = "0.0";
                                    } else {
                                        LeaveList_Balance = String.valueOf(roundTo2Dp(subMenuObject.getDouble("Balance")));
                                    }
                                    LeaveList_BalanceAsOfDate = subMenuObject.getString("FromDate");
                                    LeaveList_DateApplyList = subMenuObject.getString("DateApplyList");
                                    LeaveList_ApprovalStatus = subMenuObject.getInt("ApprovalStatus");
                                    LeaveList_RejectedReason = subMenuObject.getString("RejectedReason");
                                    LeaveList_RequestID = subMenuObject.getInt("RequestID");
                                    LeaveList_WorkflowTypeID = subMenuObject.getInt("WorkflowTypeID");
                                    LeaveList_WorkflowTaskID = subMenuObject.getInt("WorkflowTaskID");
                                    LeaveList_AttachedImage = subMenuObject.getString("AttachedImage");

                                    leave = new LeaveContent(La_Id, LeaveList_EmploymentNumber, LeaveList_EnglishName, LeaveList_ChineseName, LeaveList_Nickname, LeaveList_LeaveCode, LeaveList_FromDate, LeaveList_ToDate, LeaveList_TotalDayApply, LeaveList_DateApplyList, LeaveList_Balance, LeaveList_BalanceAsOfDate, LA_Half_Day_Ind, LeaveList_AttachedImage, LeaveList_ApprovalStatus, LeaveList_RejectedReason, LeaveList_RequestID, LeaveList_WorkflowTypeID, LeaveList_WorkflowTaskID, La_Approve_By, 0);
                                    leaveList.add(leave);

                                    //region TBD
                                    Log.i(TAG, "Record Number is: " + "[" + j + "]");
                                    Log.i(TAG, "LeaveList_UserID is: " + "[" + LeaveList_UserID + "]");
                                    Log.i(TAG, "LeaveList_EmploymentNumber is: " + "[" + LeaveList_EmploymentNumber + "]");
                                    Log.i(TAG, "LeaveList_Nickname is: " + "[" + LeaveList_Nickname + "]");
                                    Log.i(TAG, "LeaveList_ChineseName is: " + "[" + LeaveList_ChineseName + "]");
                                    Log.i(TAG, "LeaveList_EnglishName is: " + "[" + LeaveList_EnglishName + "]");
                                    Log.i(TAG, "LeaveList_LeaveCode is: " + "[" + LeaveList_LeaveCode + "]");
                                    Log.i(TAG, "LeaveList_FromDate is: " + "[" + LeaveList_FromDate + "]");
                                    Log.i(TAG, "LeaveList_ToDate is: " + "[" + LeaveList_ToDate + "]");
                                    Log.i(TAG, "LeaveList_TotalDayApply is: " + "[" + LeaveList_TotalDayApply + "]");
                                    Log.i(TAG, "LeaveList_Balance is: " + "[" + LeaveList_Balance + "]");
                                    Log.i(TAG, "LeaveList_DateApplyList is: " + "[" + LeaveList_DateApplyList + "]");
                                    Log.i(TAG, "LeaveList_ApprovalStatus is: " + "[" + LeaveList_ApprovalStatus + "]");
                                    Log.i(TAG, "LeaveList_RejectedReason is: " + "[" + LeaveList_RejectedReason + "]");
                                    Log.i(TAG, "LeaveList_RequestID is: " + "[" + LeaveList_RequestID + "]");
                                    Log.i(TAG, "LeaveList_WorkflowTypeID is: " + "[" + LeaveList_WorkflowTypeID + "]");
                                    Log.i(TAG, "LeaveList_WorkflowTaskID is: " + "[" + LeaveList_WorkflowTaskID + "]");
                                    Log.i(TAG, "LeaveList_AttachedImage is: " + "[" + LeaveList_AttachedImage + "]");
                                    //endregion
                                }
                                if (!(leaveList.isEmpty())) {
                                    dbHelper.openDB();
                                    dbHelper.insertLeaveList(leaveList);
                                    dbHelper.closeDB();
                                }
                            }

                            // Formerly PostExecute
                            try {
                                if (pDialog != null) {
                                    if (pDialog.isShowing()) {
                                        pDialog.dismiss();
                                    }
                                }
                            } catch (Exception e) {

                            } finally {
                                pDialog = null;
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveMasterRecordActivity.this);

                            if (requestLeaveApprovalListResultList != null) {
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
                                    Log.i(TAG, "Error Code is Empty");
                                    Log.i(TAG, "FromDate and ToDate: " + FromDate_Str + " " + ToDate_Str);
                                    ShowSQLiteDBdata(Status, Status2, FromDate_Str, ToDate_Str);
                                    if (BackFromDetailConfirm) {
                                        RetrievePosition = PreferenceManager.getDefaultSharedPreferences(LeaveMasterRecordActivity.this).getInt(PREF_ITEM, 0);
                                        LISTVIEW.setSelection(RetrievePosition);
                                    }
                                }
                            } else {
                                builder.setMessage(getString(R.string.msg_errorLoginFail));
                                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dlg = builder.create();
                                dlg.show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, getGenericErrorListener(LeaveMasterRecordActivity.this, pDialog));
            req.setRetryPolicy(new DefaultRetryPolicy(
                    LONGEST_TIMEOUT_MS,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(req);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion

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
            mReceived = intent.getParcelableArrayListExtra(EXTRA_BROADCAST_NOTIFICATION);
            Log.i("Incoming", "Incoming");
            if ((mNotificationAdapter != null) && (mReceived.size() != 0)) {
                mNotificationAdapter.addItems(mReceived);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    }
                });
            }
            int count = intent.getIntExtra(EXTRA_BROADCAST_NOTIFICATION_COUNT, 0);
            mNotiCountButton.setText(String.valueOf(count));
            ShortcutBadger.applyCount(LeaveMasterRecordActivity.this, count);
        }
    }
    //endregion
}
