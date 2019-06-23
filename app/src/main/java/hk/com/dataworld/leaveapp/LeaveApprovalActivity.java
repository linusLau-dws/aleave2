package hk.com.dataworld.leaveapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.Notification;
import hk.com.dataworld.leaveapp.DAL.RequestLeaveApprovalListResultData;
import hk.com.dataworld.leaveapp.DAL.WorkflowStep;
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
import static hk.com.dataworld.leaveapp.Constants.EXTRA_WORKFLOW_STEPS;
import static hk.com.dataworld.leaveapp.Constants.LONGEST_TIMEOUT_MS;
import static hk.com.dataworld.leaveapp.Constants.PREF_LOCALE;
import static hk.com.dataworld.leaveapp.Constants.PREF_SERVER_ADDRESS;
import static hk.com.dataworld.leaveapp.Constants.PREF_TOKEN;
import static hk.com.dataworld.leaveapp.Constants.STATUS_APPROVED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_CONFIRMED_CANCELLED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_PENDING_CANCEL;
import static hk.com.dataworld.leaveapp.Constants.STATUS_REJECTED;
import static hk.com.dataworld.leaveapp.Constants.STATUS_WAITING;
import static hk.com.dataworld.leaveapp.Utility.extendBaseUrl;
import static hk.com.dataworld.leaveapp.Utility.getDayOfWeekSuffixedString;
import static hk.com.dataworld.leaveapp.Utility.getGenericErrorListener;
import static hk.com.dataworld.leaveapp.Utility.getStatusStringByCode;

public class LeaveApprovalActivity extends BaseActivity {

    private static final String TAG = LeaveApprovalActivity.class.getSimpleName();
    private static String baseUrl;
    Integer Approver_UserID;
    String Approver_emp_id;
    String Usr_Emp_Id, Usr_English_Name, Usr_Chinese_Name;
    String Emp_Id, English_Name, Chinese_Name, Nickname, Leave_Ind, Start_Date, End_Date, Balance_As_Of_Date, Date_Apply, Rejected_Reason, Approve_By;
    Double Number_Of_Days;
    String Balance;
    Integer Half_Day_Ind, Approval_Status, Usr_UserId, RequestId, WorkflowTypeId, WorkflowTaskId;
    Integer New_Approval_Status;
    Integer Record_Count;
    Integer Current_Count = -1;
    String Photo;
    Bitmap AttachedImage;
    String CombinedRejectReason;
    Integer La_Id;
    String ErrorCode;
    JSONArray LeaveApprovalList;
    Integer LeaveDetail_UserID, LeaveDetail_ApprovalStatus, LeaveDetail_RequestID, LeaveDetail_WorkflowTypeID, LeaveDetail_WorkflowTaskID;
    Double LeaveDetail_TotalDayApply;
    String LeaveDetail_Balance, LeaveDetail_BalanceAsOfDate;
    String LeaveDetail_EmploymentNumber, LeaveDetail_Nickname, LeaveDetail_ChineseName, LeaveDetail_EnglishName, LeaveDetail_LeaveCode, LeaveDetail_FromDate, LeaveDetail_ToDate, LeaveDetail_DateApplyList, LeaveDetail_RejectedReason, LeaveDetail_AttachedImage;
    boolean isImageFitToScreen;
    Integer Pos = -1;
    boolean FromMyApplication = false;
    boolean FromLeaveApproval = false;
    SQLiteHelper dbHelper;
    private ProgressDialog pDialog;
    private String mToken;
    private RequestQueue mQueue;
    private BootstrapButton mNotiCountButton;
    private NotificationListAdapter mNotificationAdapter = null;
    private ListView mListView = null;
    private ImageView imageView;
    private TextView tvHeading, workerName, nickName, alRemain, asOfDate, leaveType, dateRange, numberOfDays, dateApply, approvalStatus;
    private EditText auxRejectReason;
    private Button approveButton, rejectButton, withdrawButton, cancelButton, approvalProgressButton;
    private AutoCompleteTextView mainRejectReason;
    // TODO: TBD
    private ImageView more;
    private TextView toastTV;
    private LinearLayout toastLayout;

    private SharedPreferences mSharedPrefs;
    private ArrayList<WorkflowStep> mWorkflowSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_approval);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPrefs.getString(PREF_TOKEN, "");
        baseUrl = extendBaseUrl(mSharedPrefs.getString(PREF_SERVER_ADDRESS, ""));

        dbHelper = new SQLiteHelper(LeaveApprovalActivity.this);
        dbHelper.openDB();

        UserContent userContent = dbHelper.getUserInfo();

        dbHelper.closeDB();

        Record_Count = userContent.getCount();
        Usr_UserId = userContent.getUserId();
        Usr_Emp_Id = userContent.getEmploymentNumber();
        Usr_English_Name = userContent.getEnglishName();
        Usr_Chinese_Name = userContent.getChineseName();
        Record_Count = userContent.getCount();
        Log.i(TAG, "User ID: " + Usr_UserId);
        Log.i(TAG, "English Name: " + Usr_English_Name);
        Log.i(TAG, "Chinese Name: " + Usr_Chinese_Name);
        Log.i(TAG, "Approver Employment Number: " + Usr_Emp_Id);
        Log.i(TAG, "No Record Get: " + Record_Count);

        Approver_UserID = userContent.getUserId();
        Approver_emp_id = userContent.getEmploymentNumber();

        approveButton = (Button) findViewById(R.id.btnApproval);
        rejectButton = (Button) findViewById(R.id.btnReject);
        withdrawButton = (Button) findViewById(R.id.btnWithdraw);
        cancelButton = (Button) findViewById(R.id.btnCancel);
        approvalProgressButton = (Button) findViewById(R.id.btnApprovalProgress);
        imageView = (ImageView) findViewById(R.id.ivAttachedImage);
        workerName = (TextView) findViewById(R.id.tvSeqNo);
        nickName = (TextView) findViewById(R.id.tvNickname);
        alRemain = (TextView) findViewById(R.id.tvALremain);
        asOfDate = (TextView) findViewById(R.id.tvAsOfDate);
        leaveType = (TextView) findViewById(R.id.tvLeavetype);
        dateRange = (TextView) findViewById(R.id.tvDateRange);
        numberOfDays = (TextView) findViewById(R.id.tvNumberofdays);
        dateApply = (TextView) findViewById(R.id.etDateapply);
        approvalStatus = (TextView) findViewById(R.id.tvApprovalStatus);
        auxRejectReason = (EditText) findViewById(R.id.etRejectreason);
        mainRejectReason = (AutoCompleteTextView) findViewById(R.id.actvRejectReason);
        more = (ImageView) findViewById(R.id.ivMore);
        mainRejectReason.setClickable(false);
        mainRejectReason.setFocusable(false);
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        if (getIntent().hasExtra(EXTRA_TO_MY_HISTORY)) {
            FromMyApplication = true;
            FromLeaveApproval = false;
            tvHeading.setText(getString(R.string.btn_myapplication));
        } else {
            FromMyApplication = false;
            FromLeaveApproval = true;
            tvHeading.setText(getString(R.string.btn_leaveapproval));
        }

        ArrayAdapter<CharSequence> adapterRejectedReason = ArrayAdapter.createFromResource(this, R.array.rejectreason, android.R.layout.simple_dropdown_item_1line);
        mainRejectReason.setAdapter(adapterRejectedReason);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainRejectReason.showDropDown();
            }
        });

        workerName.setFocusable(false);
        nickName.setFocusable(false);
        imageView.setClickable(false);
        imageView.setEnabled(false);
        alRemain.setFocusable(false);
        asOfDate.setFocusable(false);
        leaveType.setFocusable(false);
        leaveType.setClickable(false);
        dateRange.setFocusable(false);
        dateRange.setClickable(false);
        numberOfDays.setFocusable(false);
        numberOfDays.setClickable(false);
        dateApply.setFocusable(false);
        dateApply.setClickable(false);
        approvalStatus.setFocusable(false);
        approvalStatus.setClickable(false);

        dbHelper = new SQLiteHelper(LeaveApprovalActivity.this);

        if (getIntent().hasExtra("position")) {
            Pos = getIntent().getExtras().getInt("position");
            Log.i(TAG, "Intent position is: " + Pos);
        }

        if (getIntent().hasExtra("item_position")) {
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putInt("item", getIntent().getExtras().getInt("item_position"));
            editor.apply();
        }

        LeaveContent leaveContent = new LeaveContent(La_Id, Emp_Id, English_Name, Chinese_Name, Nickname, Leave_Ind, Start_Date, End_Date, Number_Of_Days, Date_Apply, Balance, Balance_As_Of_Date, Half_Day_Ind, Photo, Approval_Status, Rejected_Reason, RequestId, WorkflowTypeId, WorkflowTaskId, Approve_By, Record_Count);

        dbHelper.openDB();

        leaveContent = dbHelper.getLeaveApply(Pos);

        dbHelper.closeDB();

        if (leaveContent.getEmpNo().equals("")) {
            Log.i(TAG, "No Record Found");
            Toast toast = Toast.makeText(this, getString(R.string.msg_infoNoRecordFound), Toast.LENGTH_SHORT);
            toastLayout = (LinearLayout) toast.getView();
            toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(18);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        } else {
            Record_Count = leaveContent.getCount();
            Emp_Id = leaveContent.getEmpNo();
            English_Name = leaveContent.getEnglishName();
            Chinese_Name = leaveContent.getChineseName();
            Nickname = leaveContent.getNickname();
            Leave_Ind = leaveContent.getLeaveType();
            Balance = leaveContent.getBalance();
            Balance_As_Of_Date = leaveContent.getBalanceAsOfDate();
            Half_Day_Ind = leaveContent.getHalfDayInd();
            Start_Date = leaveContent.getFromDate();
            End_Date = leaveContent.getToDate();
            Number_Of_Days = leaveContent.getNumberOfDays();
            Date_Apply = leaveContent.getDateApply();
            Approval_Status = leaveContent.getApprovalStatus();
            Approve_By = leaveContent.getApproveBy();
            Rejected_Reason = leaveContent.getRejectedReason();
            RequestId = leaveContent.getRequestId();
            WorkflowTypeId = leaveContent.getWorkflowTypeId();
            WorkflowTaskId = leaveContent.getWorkflowTaskId();

            Log.i(TAG, "Leave_Ind: " + WorkflowTaskId);

            LeaveBalanceContent leaveBalanceContent;

            dbHelper.openDB();

            leaveBalanceContent = dbHelper.getLeaveBalanceByLeaveType(Leave_Ind);

            dbHelper.closeDB();

            Log.i(TAG, "Description is: " + leaveBalanceContent.getLBLeaveDescription());

            getDetails();

            if (leaveBalanceContent.getCount() == 0) {
                leaveType.setText(getString(R.string.tv_leavetype, Leave_Ind));
            } else {
                leaveType.setText(getString(R.string.tv_leavetype, leaveBalanceContent.getLBLeaveDescription()));
            }

            String lLocale = mSharedPrefs.getString(PREF_LOCALE, "en");
            if (lLocale.equals("zh")) {
                if (Chinese_Name.isEmpty()) {
                    workerName.setText(getString(R.string.tv_name, English_Name));
                } else {
                    workerName.setText(getString(R.string.tv_name, Chinese_Name));
                }
            } else { //if (lLocale.equals("en")) {
                workerName.setText(getString(R.string.tv_name, English_Name));
            }

            if (Nickname == null || Nickname.length() == 0 || Nickname.equals("null")) {
//                Log.i(TAG, "Empty Nickname");
                Nickname = "";
            }
            nickName.setText(getString(R.string.tv_nickname, Nickname));

            dateRange.setText(getString(R.string.tv_date_range, getDayOfWeekSuffixedString(this, Start_Date), getDayOfWeekSuffixedString(this, End_Date)));

            numberOfDays.setText(getString(R.string.tv_totalDays) + " " + Number_Of_Days);

            dateApply.setText(Date_Apply);

            Log.i(TAG, "Approval Status is: " + Approval_Status);

            if ((Rejected_Reason == null || Rejected_Reason.length() == 0 || Rejected_Reason.equals("null")) && FromMyApplication) {
                auxRejectReason.setVisibility(View.GONE);
            } else {
                auxRejectReason.setText(Rejected_Reason);
            }

            Current_Count = Pos;
            La_Id = leaveContent.getId();

            imageView.setClickable(true);
            imageView.setEnabled(true);
            isImageFitToScreen = false;
        }

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getBaseContext(), ShowFullSizePicActivity.class);
//                String filename = createImageFromBitmap(AttachedImage);
//                intent.putExtra("picFile", filename);
//                startActivity(intent);
//            }
//        });

        approvalProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Will go to History Master Record");
                Log.i("testtt", String.valueOf(mWorkflowSteps.size()));
                Intent intent = new Intent(LeaveApprovalActivity.this, HistoryMasterRecordActivity.class);
                intent.putExtra("GetApprovalHist", Emp_Id);
                intent.putParcelableArrayListExtra(EXTRA_WORKFLOW_STEPS, mWorkflowSteps);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDisconnectTimer();
//        Doctor_Paper.recycle();
//        Log.i(TAG, "Recycle Bitmap");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
//        Doctor_Paper.recycle();
        resetDisconnectTimer();
    }

    public String createImageFromBitmap(Bitmap bitmap) {
        String fileName = "tmpImage";
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "BACK KEY PRESSED");
        if (FromMyApplication) {
            Log.i(TAG, "From My Application Selection Page");
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent.putExtra(EXTRA_TO_MY_HISTORY, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, Approval_Status));
            finish();
            super.onBackPressed();
        }
        if (FromLeaveApproval) {
            Log.i(TAG, "From Leave Approval Selection Page");
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent.putExtra(EXTRA_TO_MY_HISTORY, false).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_DETAIL, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, Approval_Status));
            finish();
            super.onBackPressed();
        }
    }
    //endregion

    private void getDetails() {
        try {
            pDialog = new ProgressDialog(LeaveApprovalActivity.this);
            if (FromMyApplication) {
                pDialog.setMessage(getString(R.string.request_leave_history_detail_progress));
            } else {
                pDialog.setMessage(getString(R.string.request_leave_approval_detail_progress));
            }
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("RequestID", RequestId);            //TODO: Use this
            obj.put("token", mToken);
            Log.i("RequestID", " " + RequestId);

            mQueue = Volley.newRequestQueue(this);

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "RequestLeaveApprovalDetailActivity"),
                    obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ArrayList<RequestLeaveApprovalListResultData> requestLeaveApprovalListResultList = new ArrayList<>();
                    ArrayList<LeaveContent> leaveList = new ArrayList<>();
                    JSONObject itemUserInfo;
                    RequestLeaveApprovalListResultData requestLeaveApprovalListResultData;

                    try {
                        JSONArray jArray = response.getJSONArray("d");
                        Log.i(TAG, "wfe content is: " + jArray.getString(0));
                        Log.i(TAG, "wfe size is: " + jArray.length());
                        String XXX = jArray.getString(0);
                        Log.i(TAG, "wfe size in bytes is: " + XXX.getBytes().length);

                        mWorkflowSteps = new ArrayList<>();

                        for (int i = 0; i < jArray.length(); i++) {
                            itemUserInfo = jArray.getJSONObject(i);
                            requestLeaveApprovalListResultData = new RequestLeaveApprovalListResultData();
                            requestLeaveApprovalListResultData.setErrorCode(itemUserInfo.getString("ErrorCode"));
                            ErrorCode = requestLeaveApprovalListResultData.getErrorCode();

                            requestLeaveApprovalListResultList.add(requestLeaveApprovalListResultData);
                            Log.i(TAG, "ErrorCode is: " + requestLeaveApprovalListResultData.getErrorCode());

                            if (ErrorCode.isEmpty()) {
                                //Shim: Why list???
                                JSONArray array = new JSONArray();
                                JSONObject obj = itemUserInfo.getJSONObject("LeaveApprovalList");
                                array.put(obj);
                                //Shim

                                requestLeaveApprovalListResultData.setLeaveApprovalList(array);
                                Log.i(TAG, "LeaveApprovalList content is: " + requestLeaveApprovalListResultData.getLeaveApprovalList());
                                LeaveApprovalList = array;
                                Log.i(TAG, "LeaveApprovalList array length is: " + LeaveApprovalList.length());

                                for (int j = 0; j < LeaveApprovalList.length(); ++j) {
                                    JSONObject subMenuObject = LeaveApprovalList.getJSONObject(j);
                                    La_Id = j;
                                    LeaveDetail_UserID = subMenuObject.getInt("UserID");
                                    LeaveDetail_EmploymentNumber = subMenuObject.getString("EmploymentNumber");
                                    LeaveDetail_Nickname = subMenuObject.getString("Nickname");
                                    LeaveDetail_ChineseName = subMenuObject.getString("ChineseName");
                                    LeaveDetail_EnglishName = subMenuObject.getString("EnglishName");
                                    LeaveDetail_LeaveCode = subMenuObject.getString("LeaveCode");
                                    LeaveDetail_FromDate = subMenuObject.getString("FromDate");
                                    LeaveDetail_ToDate = subMenuObject.getString("ToDate");
                                    LeaveDetail_TotalDayApply = subMenuObject.getDouble("TotalDayApply");
                                    LeaveDetail_Balance = subMenuObject.getString("Balance");
                                    LeaveDetail_BalanceAsOfDate = subMenuObject.getString("BalanceAsOfDate");
                                    LeaveDetail_DateApplyList = subMenuObject.getString("DateApplyList");
                                    LeaveDetail_ApprovalStatus = subMenuObject.getInt("ApprovalStatus");
                                    Approval_Status = LeaveDetail_ApprovalStatus;
                                    showOrHideButtons(LeaveDetail_ApprovalStatus);
                                    LeaveDetail_RejectedReason = subMenuObject.getString("RejectedReason");
                                    LeaveDetail_RequestID = subMenuObject.getInt("RequestID");
                                    LeaveDetail_WorkflowTypeID = subMenuObject.getInt("WorkflowTypeID");
                                    LeaveDetail_WorkflowTaskID = subMenuObject.getInt("WorkflowTaskID");

                                    // TODO: Change to array
//                                    LeaveDetail_AttachedImage = subMenuObject.getString("AttachedImage");
                                    RecyclerView recyclerView = findViewById(R.id.attachedImageRecycView);
                                    recyclerView.setLayoutManager(new GridLayoutManager(LeaveApprovalActivity.this, 6));
                                    AttachmentRecyclerViewAdapter attachmentRecyclerViewAdapter = new AttachmentRecyclerViewAdapter(LeaveApprovalActivity.this, false);
                                    recyclerView.setAdapter(attachmentRecyclerViewAdapter);

                                    JSONArray arrImg = subMenuObject.getJSONArray("AttachedImage");
                                    for (int x = 0; x < arrImg.length(); x++) {
                                        try {
                                            String attachedImg = arrImg.getString(x);
                                            if (!attachedImg.isEmpty()) {
                                                byte[] decodedString = Base64.decode(attachedImg, Base64.DEFAULT);
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                attachmentRecyclerViewAdapter.add(bitmap);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    JSONArray tmp = subMenuObject.getJSONArray("WorkflowSteps");
                                    Log.i("wfss", tmp.toString());
                                    for (int x = 0; x < tmp.length(); x++) {
                                        Log.i("wfss", "haveData");
                                        JSONObject tmpJSONObject = tmp.getJSONObject(x);
                                        mWorkflowSteps.add(new WorkflowStep(
                                                tmpJSONObject.getString("ToUserName"),
                                                tmpJSONObject.getString("PositionDescription"),
                                                tmpJSONObject.getString("ModifiedDate").substring(0, 10),
                                                tmpJSONObject.getString("ModifiedDate").substring(11),
                                                tmpJSONObject.getString("Remark")));
                                    }
                                }
                            }
                        }

                        // Formerly PostExecute
                        if (pDialog != null) {
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                        Log.i(TAG, "onPostExecute");

                        if (Balance == null || Balance.length() == 0 || Balance.equals("null")) {
                            alRemain.setText(getString(R.string.tv_remainBal, "--"));
                        } else {
                            alRemain.setText(getString(R.string.tv_remainBal, Balance));
                        }

                        if (LeaveDetail_BalanceAsOfDate == null || LeaveDetail_BalanceAsOfDate.length() == 0 || LeaveDetail_BalanceAsOfDate.equals("null")) {
                            asOfDate.setText(getString(R.string.tv_asOfDate, "--"));
                        } else {
                            asOfDate.setText(getString(R.string.tv_asOfDate, getDayOfWeekSuffixedString(LeaveApprovalActivity.this, LeaveDetail_BalanceAsOfDate)));
                        }

                        dateApply.setText(LeaveDetail_DateApplyList);

                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApprovalActivity.this);

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

                                //Testing for Approval History Records
                                dbHelper.openDB();
                                long resultDeleteApprovalHist = dbHelper.deleteApprovalHistory();
                                Log.i(TAG, "Old Approval History records been deleted");

                                //TODO: Old code is fake!!! We send ArrayList of Parcelables instead.
//                    long resultInsertApprovalHist1 = dbHelper.insertApprovalHistory(-1, Emp_Id, 1, "Chan Tai Man", "Supervisor", "2018-05-01", "16:00:30", "Temporary Approved");
//                    long resultInsertApprovalHist3 = dbHelper.insertApprovalHistory(-1, Emp_Id, 2, "Wong Chi Man", "Senior Officer", "2018-05-01", "16:10:30", "Pending for Manager Approval");
//                    long resultInsertApprovalHist4 = dbHelper.insertApprovalHistory(-1, Emp_Id, 3, "Lee On", "Manager", "2018-05-01", "16:45:30", "");
                                dbHelper.closeDB();
//                    Log.i(TAG, "New Approval History records been inserted");
//                    if (resultInsertApprovalHist1 == -1) {
//                        Log.i(TAG, "Insert Approval History record failed");
//                        approvalProgressButton.setEnabled(false);
//                        approvalProgressButton.setAlpha(.5f);
//                    } else {
//                        Log.i(TAG, "Insert Approval History record succeed");
                                approvalProgressButton.setEnabled(true);
//                        approvalProgressButton.setAlpha(1f);
//                    }
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

    // region Requests
    private void showOrHideButtons(int status) {
        approvalStatus.setText(getString(R.string.tv_approvalStatus, getStatusStringByCode(this, status)));
        if (FromMyApplication) {
            // For Applicant
            switch (status) {
                case STATUS_WAITING:
                    // Withdraw
                    more.setVisibility(View.GONE);
                    mainRejectReason.setVisibility(View.GONE);
                    auxRejectReason.setVisibility(View.GONE);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    withdrawButton.setEnabled(true);
                    withdrawButton.setAlpha(1f);
                    cancelButton.setVisibility(View.GONE);
                    withdrawButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            New_Approval_Status = STATUS_CANCELLED;
                            cancelOrWithdrawLeave();
                        }
                    });
                    break;
                case STATUS_APPROVED:
                    // Cancel
                    alRemain.setVisibility(View.GONE);
                    asOfDate.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    mainRejectReason.setVisibility(View.GONE);
                    auxRejectReason.setClickable(false);
                    auxRejectReason.setFocusable(false);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    withdrawButton.setVisibility(View.GONE);
                    cancelButton.setEnabled(true);
                    cancelButton.setAlpha(1f);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            New_Approval_Status = STATUS_CONFIRMED_CANCELLED;
                            cancelOrWithdrawLeave();
                        }
                    });
                    break;
                default:
                    // view-only
                    alRemain.setVisibility(View.GONE);
                    asOfDate.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    mainRejectReason.setVisibility(View.GONE);
                    auxRejectReason.setClickable(false);
                    auxRejectReason.setFocusable(false);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    withdrawButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    break;
            }
        } else {
            // For HR
            switch (status) {
                case STATUS_WAITING:
                    // ApproveOrReject
                    approveButton.setEnabled(true);
                    approveButton.setAlpha(1f);
                    rejectButton.setEnabled(true);
                    rejectButton.setAlpha(1f);
                    withdrawButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    approveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            New_Approval_Status = STATUS_APPROVED;
//                            Approve_By = Approver_emp_id;
                            CombinedRejectReason = String.format("%s %s", mainRejectReason.getText().toString(), auxRejectReason.getText().toString());
                            approveOrRejectLeave();
                        }
                    });
                    rejectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApprovalActivity.this, R.style.AlertDialogCustom);

                            if ((mainRejectReason.getText().toString().isEmpty() && auxRejectReason.getText().toString().isEmpty())) {
                                builder.setMessage(R.string.msg_errReject).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mainRejectReason.requestFocus();
                                    }
                                });
                                builder.create().show();
                                return;
                            }
                            New_Approval_Status = STATUS_REJECTED;
//                            Approve_By = Approver_emp_id;
                            CombinedRejectReason = String.format("%s %s", mainRejectReason.getText().toString(), auxRejectReason.getText().toString());
                            approveOrRejectLeave();
                        }
                    });
                    break;
                case STATUS_PENDING_CANCEL:
                    // ApproveOrRejectCancelRequest
                    tvHeading.setText(getString(R.string.btn_cancelleaveapproval));

                    approveButton.setEnabled(true);
                    approveButton.setAlpha(1f);
                    rejectButton.setEnabled(true);
                    rejectButton.setAlpha(1f);
                    withdrawButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    approveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            New_Approval_Status = STATUS_CONFIRMED_CANCELLED;
//                            Approve_By = Approver_emp_id;
                            CombinedRejectReason = mainRejectReason.getText().toString() + " " + auxRejectReason.getText().toString();
                            approveOrRejectCancelLeave();
                        }
                    });
                    rejectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            New_Approval_Status = STATUS_APPROVED;
//                            Approve_By = Approver_emp_id;
                            CombinedRejectReason = mainRejectReason.getText().toString() + " " + auxRejectReason.getText().toString();
                            approveOrRejectCancelLeave();
                        }
                    });
                    break;
                default:
                    // view-only
                    alRemain.setVisibility(View.GONE);
                    asOfDate.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    mainRejectReason.setVisibility(View.GONE);
                    auxRejectReason.setClickable(false);
                    auxRejectReason.setFocusable(false);
                    approveButton.setVisibility(View.GONE);
                    rejectButton.setVisibility(View.GONE);
                    withdrawButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    break;
            }
        }
//        switch (status) {
//            case STATUS_WAITING:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_pendingStatus)));
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    more.setVisibility(View.GONE);
//                    mainRejectReason.setVisibility(View.GONE);
//                    auxRejectReason.setVisibility(View.GONE);
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                    withdrawButton.setEnabled(true);
//                    withdrawButton.setAlpha(1f);
//                    cancelButton.setVisibility(View.GONE);
//                } else {
//                    Log.i(TAG, "FromLeaveApprovalRecord");
//                    approveButton.setEnabled(true);
//                    approveButton.setAlpha(1f);
//                    rejectButton.setEnabled(true);
//                    rejectButton.setAlpha(1f);
//                    withdrawButton.setVisibility(View.GONE);
//                    cancelButton.setVisibility(View.GONE);
//                }
//                break;
//            case STATUS_APPROVED:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_acceptStatus)));
//                alRemain.setVisibility(View.GONE);
//                asOfDate.setVisibility(View.GONE);
//                more.setVisibility(View.GONE);
//                mainRejectReason.setVisibility(View.GONE);
//                auxRejectReason.setClickable(false);
//                auxRejectReason.setFocusable(false);
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                    withdrawButton.setVisibility(View.GONE);
//                    cancelButton.setEnabled(true);
//                    cancelButton.setAlpha(1f);
//                } else {
//                    approveButton.setEnabled(false);
//                    approveButton.setAlpha(.5f);
//                    rejectButton.setEnabled(false);
//                    rejectButton.setAlpha(.5f);
//                    withdrawButton.setVisibility(View.GONE);
//                    cancelButton.setVisibility(View.GONE);
//                }
//                break;
//            case STATUS_REJECTED:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_rejectStatus)));
//                alRemain.setVisibility(View.GONE);
//                asOfDate.setVisibility(View.GONE);
//                more.setVisibility(View.GONE);
//                mainRejectReason.setVisibility(View.GONE);
//                auxRejectReason.setClickable(false);
//                auxRejectReason.setFocusable(false);
//                withdrawButton.setVisibility(View.GONE);
//                cancelButton.setVisibility(View.GONE);
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                } else {
//                    approveButton.setEnabled(false);
//                    approveButton.setAlpha(.5f);
//                    rejectButton.setEnabled(false);
//                    rejectButton.setAlpha(.5f);
//                }
//                break;
//            case STATUS_CANCELLED:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_cancelledStatus)));
//                alRemain.setVisibility(View.GONE);
//                asOfDate.setVisibility(View.GONE);
//                more.setVisibility(View.GONE);
//                mainRejectReason.setVisibility(View.GONE);
//                auxRejectReason.setClickable(false);
//                auxRejectReason.setClickable(false);
//                withdrawButton.setVisibility(View.GONE);
//                cancelButton.setVisibility(View.GONE);
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                } else {
//                    approveButton.setEnabled(false);
//                    approveButton.setAlpha(.5f);
//                    rejectButton.setEnabled(false);
//                    rejectButton.setAlpha(.5f);
//                }
//                // Add indicator to show that it is 'a cancel request'
//                break;
//            case STATUS_PENDING_CANCEL:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_pendingforcancelStatus)));
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    more.setVisibility(View.GONE);
//                    mainRejectReason.setVisibility(View.GONE);
//                    auxRejectReason.setClickable(false);
//                    auxRejectReason.setFocusable(false);
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                    withdrawButton.setVisibility(View.GONE);
//                    cancelButton.setVisibility(View.GONE);
//                } else {
//                    Log.i(TAG, "From Approval Page");
//                    approveButton.setEnabled(true);
//                    approveButton.setAlpha(1f);
//                    rejectButton.setEnabled(true);
//                    rejectButton.setAlpha(1f);
//                    withdrawButton.setVisibility(View.GONE);
//                    cancelButton.setVisibility(View.GONE);
//                }
//                break;
//            case STATUS_CONFIRMED_CANCELLED:
//                approvalStatus.setText(getString(R.string.tv_approvalStatus, getString(R.string.tv_confirmedCancelledStatus)));
//                alRemain.setVisibility(View.GONE);
//                asOfDate.setVisibility(View.GONE);
//                more.setVisibility(View.GONE);
//                mainRejectReason.setVisibility(View.GONE);
//                auxRejectReason.setClickable(false);
//                auxRejectReason.setFocusable(false);
//                withdrawButton.setVisibility(View.GONE);
//                cancelButton.setVisibility(View.GONE);
//                if (FromMyApplication) {
//                    Log.i(TAG, "FromStaffRetrieveOwnRecord");
//                    approveButton.setVisibility(View.GONE);
//                    rejectButton.setVisibility(View.GONE);
//                } else {
//                    approveButton.setEnabled(false);
//                    approveButton.setAlpha(.5f);
//                    rejectButton.setEnabled(false);
//                    rejectButton.setAlpha(.5f);
//                }
//                break;
//        }
    }

    private void approveOrRejectLeave() {
        try {
            pDialog = new ProgressDialog(LeaveApprovalActivity.this);
            pDialog.setMessage(getString(R.string.leave_approval_msg_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("NewApprovalStatus", New_Approval_Status);
            obj.put("RejectedReason", CombinedRejectReason);
            obj.put("RequestID", RequestId);

            JSONArray tbd = new JSONArray();
            tbd.put(obj);

            JSONObject realobj = new JSONObject();
            realobj.put("Data", tbd.toString());
            realobj.put("baseURL", baseUrl);
            realobj.put("token", mToken);

            mQueue = Volley.newRequestQueue(this);

            Log.i("realobj", realobj.toString());
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "ApproveOrReject"),
                    realobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    ArrayList<SimpleResultData> leaveApprovalReceivedList = new ArrayList<>();
//                    JSONObject item;
//                    SimpleResultData simpleResultData;
                    try {
                        int returnCode = response.getInt("d");
                        if (returnCode == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApprovalActivity.this);

                            switch (New_Approval_Status) {
                                case STATUS_APPROVED:
                                case STATUS_CONFIRMED_CANCELLED:
                                    builder.setMessage(R.string.tv_acceptStatus);
                                    break;
                                case STATUS_REJECTED:
                                    builder.setMessage(R.string.tv_rejectStatus);
                                    break;
                            }
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dbHelper.openDB();
                                    dbHelper.updateApprovalStatus(La_Id, Emp_Id, Start_Date, End_Date, New_Approval_Status, Approve_By, CombinedRejectReason);
                                    dbHelper.closeDB();

                                    Intent intent = new Intent();
                                    setResult(Activity.RESULT_OK, intent.putExtra(EXTRA_TO_MY_HISTORY, true).putExtra(EXTRA_BACK_FROM_DETAIL_CONFIRM, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, Approval_Status));
                                    finish();
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

    private void approveOrRejectCancelLeave() {
        try {
            pDialog = new ProgressDialog(LeaveApprovalActivity.this);
            pDialog.setMessage(getString(R.string.leave_withdraw_msg_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("NewApprovalStatus", New_Approval_Status);
            obj.put("RejectedReason", CombinedRejectReason);
            obj.put("RequestID", RequestId);

            JSONArray tbd = new JSONArray();
            tbd.put(obj);

            JSONObject realobj = new JSONObject();
            realobj.put("Data", tbd.toString());
            realobj.put("baseURL", baseUrl);
            realobj.put("token", mToken);

            mQueue = Volley.newRequestQueue(this);

            Log.i("realobj", realobj.toString());
            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "withdrawLeave"),
                    realobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getInt("d") == 1) {
                            ErrorCode = "";
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApprovalActivity.this)
                                    .setMessage(R.string.withdrawal_successful)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.i(TAG, "Withdraw Dialog Box");
                                            dbHelper.openDB();
                                            dbHelper.updateApprovalStatus(La_Id, Emp_Id, Start_Date, End_Date, New_Approval_Status, Approve_By, "");
                                            dbHelper.closeDB();

                                            Intent intent = new Intent();
                                            setResult(Activity.RESULT_OK, intent.putExtra(EXTRA_TO_MY_HISTORY, true).putExtra(EXTRA_BACK_FROM_DETAIL_CONFIRM, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, Approval_Status));
                                            finish();
                                        }
                                    });
                            builder.create().show();
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

    private void cancelOrWithdrawLeave() {
        try {
            pDialog = new ProgressDialog(LeaveApprovalActivity.this);
            pDialog.setMessage(getString(R.string.leave_withdraw_msg_progress));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            JSONObject obj = new JSONObject();
            obj.put("RequestID", RequestId);
            obj.put("NewApprovalStatus", New_Approval_Status);
            obj.put("baseURL", baseUrl);
            obj.put("token", mToken);

            mQueue = Volley.newRequestQueue(this);

            JsonObjectRequest req = new JsonObjectRequest(JsonObjectRequest.Method.POST, String.format("%s%s", baseUrl, "cancelOrWithdrawLeave"),
                    obj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getInt("d") == 1) {
                            ErrorCode = "";
                            AlertDialog.Builder builder = new AlertDialog.Builder(LeaveApprovalActivity.this)
                                    .setMessage(R.string.withdrawal_successful)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.i(TAG, "Withdraw Dialog Box");
                                            dbHelper.openDB();
                                            dbHelper.updateApprovalStatus(La_Id, Emp_Id, Start_Date, End_Date, New_Approval_Status, Approve_By, "");
                                            dbHelper.closeDB();

                                            Intent intent = new Intent();
                                            setResult(Activity.RESULT_OK, intent.putExtra(EXTRA_TO_MY_HISTORY, true).putExtra(EXTRA_BACK_FROM_DETAIL_CONFIRM, true).putExtra(EXTRA_BACK_FROM_LEAVE_APPROVAL_STATUS, Approval_Status));
                                            finish();
                                        }
                                    });
                            builder.create().show();
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

    // endregion

    private void registerBroadcastReceiver(NotificationBroadcastReceiver bcr) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INCOMING_NOTIFICATION);
        registerReceiver(bcr, intentFilter);
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
                    Intent itemIntent = new Intent(getBaseContext(), LeaveApprovalActivity.class);
                    itemIntent.putParcelableArrayListExtra(EXTRA_SHIM_NOTIFICATION, new ArrayList<>(mNotificationAdapter.getmArr()));
                    itemIntent.putExtra(EXTRA_SOURCE_NOTIFICATION_STATUS, mNotificationAdapter.getStatus(i));
                    itemIntent.putExtra(EXTRA_TO_MY_HISTORY, true);
                    startActivity(itemIntent);
                }
            });
        }
        return true;
    }

//    @Override
//    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
//        menu.removeItem(R.id.action_notification);
////        View v = menu.findItem(R.id.action_notification).getActionView();
////
////        View.OnClickListener onClickListener = new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////
////            }
////        };
////        AwesomeTextView awesomeTextView = v.findViewById(R.id.message_icon);
////        awesomeTextView.setOnClickListener(onClickListener);
////
////        BootstrapButton bootstrapButton = v.findViewById(R.id.message_count);
////        bootstrapButton.setOnClickListener(onClickListener);
////        bootstrapButton.setText("4");
//
//        return true;
//    }

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
            ShortcutBadger.applyCount(LeaveApprovalActivity.this, count);
        }
    }
}