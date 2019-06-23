package hk.com.dataworld.leaveapp;

/**
 * Created by Terence on 2018/1/22.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class SQLiteHelper extends SQLiteOpenHelper {

    static final String USR_ID = "_id";
    static final String USR_Name = "name";
    static final String USR_UserID = "userid";
    static final String USR_Nickname = "nickname";
    static final String USR_EnglishName = "englishname";
    static final String USR_ChineseName = "chinesename";
    static final String USR_StaffNumber = "staffnumber";
    static final String USR_EmploymentNumber = "employmentnumber";
    static final String USR_IsLeaveAllow3Sections = "allow3sections";
    static final String USR_IsLeaveAllowHalfDay = "allowhalfday";
    static final String USR_IsLeaveAllowHourly = "allowhourly";
    static final String LA_ID = "_id";
    static final String LA_Emp_No = "emp_no";
    static final String LA_EnglishName = "englishname";
    static final String LA_ChineseName = "chinesename";
    static final String LA_Nickname = "nickname";
    static final String LA_Leave_Type = "leave_type";
    static final String LA_From_Date = "from_date";
    static final String LA_To_Date = "to_date";
    static final String LA_Number_Of_Days = "number_of_days";
    static final String LA_Date_Apply = "date_apply";
    static final String LA_Balance = "balance";
    static final String LA_BalanceAsOfDate = "balanceasofdate";
    static final String LA_Half_Day_Ind = "half_day_ind";
    static final String LA_Attached_Image = "attached_image";
    static final String LA_Approval_Status = "approval_status";
    static final String LA_Rejected_Reason = "rejected_reason";
    static final String LA_RequestID = "requestid";
    static final String LA_WorkflowTypeID = "workflowtypeid";
    static final String LA_WorkflowTaskID = "workflowtaskid";
    static final String LA_Approve_By = "approve_by";
    static final String LB_ID = "_id";
    static final String LB_Emp_No = "emp_no";
    static final String LB_Leave_Type = "leave_type";
    static final String LB_Leave_Description = "leave_description";
    static final String LB_Leave_Balance = "leave_balance";
    static final String LB_Leave_BalanceAsOfDate = "leave_balanceasofdate";
    static final String LB_Leave_IsEnforcedAttachment = "leave_isenforcedattachment";
    static final String AH_ID = "_id";
    static final String AH_Emp_No = "emp_no";
    static final String AH_Sequence_No = "sequence_no";
    static final String AH_Approver_Name = "approver_name";
    static final String AH_Position = "position";
    static final String AH_Date = "date";
    static final String AH_Time = "time";
    static final String AH_Remark = "remark";
    static final String NT_LeaveDesc = "leavedesc";
    static final String NT_LeaveFrom = "leavefrom";
    static final String NT_LeaveTo = "leaveto";
    static final String NT_ReviewerName = "reviewername";
    static final String NT_CreateDate = "createdate";
    static final String NT_Status = "status";

    private static final String TAG = SQLiteHelper.class.getSimpleName();
    private static final int VERSION = 18;

    private static final String TABLE_USER = "User";
    private static final String TABLE_LEAVEAPPLY = "LeaveApply";
    private static final String TABLE_LEAVEBALANCE = "LeaveBalance";
    private static final String TABLE_APPROVALHISTORY = "ApprovalHistory";
    private static final String TABLE_NOTIFICATIONS = "Notifications";

    private static String DATABASE_NAME = "HRMSDataBase";
    SQLiteDatabase myDB;

    SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            String CREATE_TABLE3 = "CREATE TABLE " + TABLE_USER + " ("
                    + USR_ID + " INTEGER PRIMARY KEY, "
                    + USR_Name + " VARCHAR, "
                    + USR_UserID + " INTEGER, "
                    + USR_Nickname + " VARCHAR, "
                    + USR_EnglishName + " VARCHAR, "
                    + USR_ChineseName + " VARCHAR, "
                    + USR_StaffNumber + " VARCHAR, "
                    + USR_EmploymentNumber + " VARCHAR, "
                    + USR_IsLeaveAllow3Sections + " BOOLEAN, "
                    + USR_IsLeaveAllowHalfDay + " BOOLEAN, "
                    + USR_IsLeaveAllowHourly + " BOOLEAN)";
            database.execSQL(CREATE_TABLE3);
            String CREATE_TABLE4 = "CREATE TABLE " + TABLE_LEAVEAPPLY + " ("
                    + LA_ID + " INTEGER PRIMARY KEY, "
                    + LA_Emp_No + " VARCHAR, "
                    + LA_EnglishName + " VARCHAR, "
                    + LA_ChineseName + " VARCHAR, "
                    + LA_Nickname + " VARCHAR, "
                    + LA_Leave_Type + " VARCHAR, "
                    + LA_From_Date + " VARCHAR, "
                    + LA_To_Date + " VARCHAR, "
                    + LA_Number_Of_Days + " DOUBLE, "
                    + LA_Date_Apply + " VARCHAR, "
                    + LA_Balance + " VARCHAR, "
                    + LA_BalanceAsOfDate + " VARCHAR, "
                    + LA_Half_Day_Ind + " INTEGER, "
                    + LA_Attached_Image + " VARCHAR, "
                    + LA_Approval_Status + " INTEGER, "
                    + LA_Rejected_Reason + " VARCHAR, "
                    + LA_RequestID + " VARCHAR, "
                    + LA_WorkflowTypeID + " VARCHAR, "
                    + LA_WorkflowTaskID + " VARCHAR, "
                    + LA_Approve_By + " VARCHAR)";
            database.execSQL(CREATE_TABLE4);
            String CREATE_TABLE5 = "CREATE TABLE " + TABLE_LEAVEBALANCE + " ("
                    + LB_ID + " INTEGER PRIMARY KEY, "
                    + LB_Emp_No + " VARCHAR, "
                    + LB_Leave_Type + " VARCHAR, "
                    + LB_Leave_Description + " VARCHAR, "
                    + LB_Leave_Balance + " VARCHAR, "
                    + LB_Leave_BalanceAsOfDate + " VARCHAR, "
                    + LB_Leave_IsEnforcedAttachment + " BOOLEAN)";
            database.execSQL(CREATE_TABLE5);
            String CREATE_TABLE6 = "CREATE TABLE " + TABLE_APPROVALHISTORY + " ("
                    + AH_ID + " INTEGER PRIMARY KEY, "
                    + AH_Emp_No + " VARCHAR, "
                    + AH_Sequence_No + " VARCHAR, "
                    + AH_Approver_Name + " VARCHAR, "
                    + AH_Position + " VARCHAR, "
                    + AH_Date + " VARCHAR, "
                    + AH_Time + " VARCHAR, "
                    + AH_Remark + " VARCHAR)";
            database.execSQL(CREATE_TABLE6);
            String CREATE_TABLE9 = "CREATE TABLE " + TABLE_NOTIFICATIONS + " ("
                    + NT_LeaveDesc + " VARCHAR, "
                    + NT_LeaveFrom + " VARCHAR, "
                    + NT_LeaveTo + " VARCHAR, "
                    + NT_ReviewerName + " VARCHAR, "
                    + NT_CreateDate + " VARCHAR PRIMARY KEY, "
                    + NT_Status + " INT)";
            database.execSQL(CREATE_TABLE9);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Perform DB onUpgrade");
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEAVEAPPLY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEAVEBALANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPROVALHISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);


            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void openDB() {
        Log.i(TAG, "openDB");
        try {
            myDB = getWritableDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void closeDB() {
        Log.i(TAG, "closeDB");
        if (myDB != null && myDB.isOpen()) {
            try {
                myDB.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    long insertUser(int id, String name, Integer userid, String nickname, String englishname, String chinesename, String staffnumber, String employmentnumber, Boolean allow3sections, Boolean allowhalfday, Boolean allowhourly) {
        ContentValues values = new ContentValues();
        if (id != -1)
            values.put(USR_ID, id);
        values.put(USR_Name, name);
        values.put(USR_UserID, userid);
        values.put(USR_Nickname, nickname);
        values.put(USR_EnglishName, englishname);
        values.put(USR_ChineseName, chinesename);
        values.put(USR_StaffNumber, staffnumber);
        values.put(USR_EmploymentNumber, employmentnumber);
        values.put(USR_IsLeaveAllow3Sections, allow3sections);
        values.put(USR_IsLeaveAllowHalfDay, allowhalfday);
        values.put(USR_IsLeaveAllowHourly, allowhourly);
        return myDB.insert(TABLE_USER, null, values);
    }

    long insertLeaveApply(LeaveContent leaveContent) {
        ContentValues values = new ContentValues();

        values.put(LA_Emp_No, leaveContent.getEmpNo());
        values.put(LA_EnglishName, leaveContent.getEnglishName());
        values.put(LA_ChineseName, leaveContent.getChineseName());
        values.put(LA_Nickname, leaveContent.getNickname());
        values.put(LA_Leave_Type, leaveContent.getLeaveType());
        values.put(LA_From_Date, leaveContent.getFromDate());
        values.put(LA_To_Date, leaveContent.getToDate());
        values.put(LA_Number_Of_Days, leaveContent.getNumberOfDays());
        values.put(LA_Date_Apply, leaveContent.getDateApply());
        values.put(LA_Balance, leaveContent.getBalance());
        values.put(LA_BalanceAsOfDate, leaveContent.getBalanceAsOfDate());
        values.put(LA_Half_Day_Ind, leaveContent.getHalfDayInd());
        values.put(LA_Attached_Image, leaveContent.getAttachedImage());
        values.put(LA_Approval_Status, leaveContent.getApprovalStatus());
        values.put(LA_Rejected_Reason, leaveContent.getRejectedReason());
        values.put(LA_RequestID, leaveContent.getRequestId());
        values.put(LA_WorkflowTypeID, leaveContent.getWorkflowTypeId());
        values.put(LA_WorkflowTaskID, leaveContent.getWorkflowTaskId());
        values.put(LA_Approve_By, leaveContent.getApproveBy());
        return myDB.insert(TABLE_LEAVEAPPLY, null, values);
    }

    void insertLeaveList(List<LeaveContent> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " + TABLE_LEAVEAPPLY + " (emp_no, englishname, chinesename, nickname, leave_type, from_date, to_date, number_of_days, date_apply, balance, balanceasofdate, approval_status, rejected_reason, requestid, workflowtypeid, workflowtaskid, attached_image) " + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.i(TAG, "Begin insertLeaveList Transaction");
        try {
            for (LeaveContent leave : list) {
                statement.clearBindings();
                statement.bindString(1, leave.getEmpNo());
                statement.bindString(2, leave.getEnglishName());
                statement.bindString(3, leave.getChineseName());
                statement.bindString(4, leave.getNickname());
                statement.bindString(5, leave.getLeaveType());
                statement.bindString(6, leave.getFromDate());
                statement.bindString(7, leave.getToDate());
                statement.bindDouble(8, leave.getNumberOfDays());
                statement.bindString(9, leave.getDateApply());
                statement.bindString(10, leave.getBalance());
                statement.bindString(11, leave.getBalanceAsOfDate());
                statement.bindLong(12, leave.getApprovalStatus());
                statement.bindString(13, leave.getRejectedReason());
                statement.bindLong(14, leave.getRequestId());
                statement.bindLong(15, leave.getWorkflowTypeId());
                statement.bindLong(16, leave.getWorkflowTaskId());
                statement.bindString(17, leave.getAttachedImage());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Finish insertLeaveList Transaction");
        }
    }

    long insertLeaveBalance(int id, String empnumber, String leavetype, String leavedescription, String leavebalance, String leavebalanceasofdate, boolean isenforcedattachment) {
        ContentValues values = new ContentValues();
        if (id != -1)
            values.put(LB_ID, id);
        values.put(LB_Emp_No, empnumber);
        values.put(LB_Leave_Type, leavetype);
        values.put(LB_Leave_Description, leavedescription);
        values.put(LB_Leave_Balance, leavebalance);
        values.put(LB_Leave_BalanceAsOfDate, leavebalanceasofdate);
        values.put(LB_Leave_IsEnforcedAttachment, isenforcedattachment);
        return myDB.insert(TABLE_LEAVEBALANCE, null, values);
    }

    void insertLeaveBalanceList(List<LeaveBalanceContent> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " + TABLE_LEAVEBALANCE + " (" + LB_Emp_No + ", " + LB_Leave_Type + ", " + LB_Leave_Description + ", " + LB_Leave_Balance + ", " + LB_Leave_Balance + ", " + LB_Leave_IsEnforcedAttachment + ") " + " values (?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.i(TAG, "Begin insertLeaveBalanceList Transaction");
        try {
            for (LeaveBalanceContent leavebalance : list) {
                statement.clearBindings();
                statement.bindString(1, leavebalance.getLBEmploymentNumber());
                statement.bindString(2, leavebalance.getLBLeaveType());
                statement.bindString(3, leavebalance.getLBLeaveDescription());
                statement.bindString(4, leavebalance.getLBLeaveBalance());
                statement.bindString(5, leavebalance.getLBLeaveBalanceAsOfDate());
                statement.bindString(6, Boolean.toString(leavebalance.getLBIsEnforcedAttachment()));
                statement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Finish insertLeaveBalanceList Transaction");
        }
    }

    long insertApprovalHistory(int id, String empnumber, int seqnumber, String approvername, String position, String date, String time, String remark) {
        ContentValues values = new ContentValues();
        if (id != -1)
            values.put(AH_ID, id);
        values.put(AH_Emp_No, empnumber);
        values.put(AH_Sequence_No, seqnumber);
        values.put(AH_Approver_Name, approvername);
        values.put(AH_Position, position);
        values.put(AH_Date, date);
        values.put(AH_Time, time);
        values.put(AH_Remark, remark);
        return myDB.insert(TABLE_APPROVALHISTORY, null, values);
    }

    void insertApprovalHistoryList(List<ApprovalHistoryContent> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT OR REPLACE INTO " + TABLE_APPROVALHISTORY + " (emp_no, sequence_no, approver_name, position, date, time, remark) " + " values (?,?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();
        Log.i(TAG, "Begin insertApprovalHistoryList Transaction");
        try {
            for (ApprovalHistoryContent history : list) {
                statement.clearBindings();
                statement.bindString(1, history.getEmpNo());
                statement.bindLong(2, history.getSeqNo());
                statement.bindString(3, history.getApproverName());
                statement.bindString(4, history.getPosition());
                statement.bindString(5, history.getDate());
                statement.bindString(6, history.getTime());
                statement.bindString(7, history.getRemark());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i(TAG, "Finish insertApprovalHistoryList Transaction");
        }
    }

    UserContent getUserInfo() throws SQLException {
        Log.i(TAG, "Enter getUserInfo");

        Cursor cur = myDB.query(true, TABLE_USER, new String[]{USR_Name, USR_UserID, USR_Nickname,
                USR_EnglishName, USR_ChineseName, USR_StaffNumber, USR_EmploymentNumber, USR_IsLeaveAllow3Sections, USR_IsLeaveAllowHalfDay, USR_IsLeaveAllowHourly}, null, null, null, null, null, null);

        if (cur != null && (cur.getCount() > 0)) {
            Log.i(TAG, "Number of record: " + cur.getCount());
            if (cur.moveToLast()) {
                String u_user = cur.getString(cur.getColumnIndex(USR_Name));
                Integer u_userid = cur.getInt(cur.getColumnIndex(USR_UserID));
                String u_nickname = cur.getString(cur.getColumnIndex(USR_Nickname));
                String u_englishname = cur.getString(cur.getColumnIndex(USR_EnglishName));
                String u_chinesename = cur.getString(cur.getColumnIndex(USR_ChineseName));
                String u_staffnumber = cur.getString(cur.getColumnIndex(USR_StaffNumber));
                String u_employmentnumber = cur.getString(cur.getColumnIndex(USR_EmploymentNumber));
                Boolean u_allow3section = cur.getInt(cur.getColumnIndex(USR_IsLeaveAllow3Sections)) != 0;
                Boolean u_allowhalfday = cur.getInt(cur.getColumnIndex(USR_IsLeaveAllowHalfDay)) != 0;
                Boolean u_allowhourly = cur.getInt(cur.getColumnIndex(USR_IsLeaveAllowHourly)) != 0;
                Integer no_of_record = cur.getCount();
                cur.close();
                return new UserContent(u_user, u_userid, u_nickname, u_englishname, u_chinesename, u_staffnumber, u_employmentnumber, u_allow3section, u_allowhalfday, u_allowhourly, no_of_record);
            }
        } else {
            Log.i(TAG, "Cursor has NO row of record");
            cur.close();
            return new UserContent("", 0, "", "", "", "", "", null, null, null, 0);
        }
        return null;
    }

    boolean hasAL() {
        Cursor cur = myDB.rawQuery("SELECT EXISTS(SELECT 1 FROM " + TABLE_LEAVEBALANCE + " WHERE " + LB_Leave_Type + " LIKE ?);", new String[]{"AL%"});
        cur.moveToFirst();
        int tmp = cur.getInt(0);
        cur.close();
        return tmp != 0;
    }

    boolean hasSL() {
        Cursor cur = myDB.rawQuery("SELECT EXISTS(SELECT 1 FROM " + TABLE_LEAVEBALANCE + " WHERE " + LB_Leave_Type + " LIKE ?);", new String[]{"SL%"});
        cur.moveToFirst();
        int tmp = cur.getInt(0);
        cur.close();
        return tmp != 0;
    }


    LeaveContent getLeaveApply(int location) throws SQLException {
        Log.i(TAG, "Enter getLeaveApply");

        Cursor cur = myDB.query(true, TABLE_LEAVEAPPLY, new String[]{LA_ID, LA_Emp_No, LA_EnglishName, LA_ChineseName, LA_Nickname, LA_Leave_Type,
                LA_From_Date, LA_To_Date, LA_Number_Of_Days, LA_Date_Apply, LA_Balance, LA_BalanceAsOfDate, LA_Half_Day_Ind, LA_Attached_Image, LA_Approval_Status, LA_Rejected_Reason, LA_RequestID, LA_WorkflowTypeID, LA_WorkflowTaskID, LA_Approve_By}, null, null, null, null, null, null);
        Log.i(TAG, "Create cursor and get OK");
        Log.i(TAG, "Number of record: " + cur.getCount());


        Log.i(TAG, "Location fetch is: " + location);
        if (cur.moveToPosition(location)) {
            Integer la_id = cur.getInt(cur.getColumnIndex(LA_ID));
            String la_empno = cur.getString(cur.getColumnIndex(LA_Emp_No));
            String la_englishname = cur.getString(cur.getColumnIndex(LA_EnglishName));
            String la_chinesename = cur.getString(cur.getColumnIndex(LA_ChineseName));
            String la_nickname = cur.getString(cur.getColumnIndex(LA_Nickname));
            String la_leavetype = cur.getString(cur.getColumnIndex(LA_Leave_Type));
            Log.i("heisenbug", la_leavetype);
            String la_fromdate = cur.getString(cur.getColumnIndex(LA_From_Date));
            String la_todate = cur.getString(cur.getColumnIndex(LA_To_Date));
            Double la_numberofdays = cur.getDouble(cur.getColumnIndex(LA_Number_Of_Days));
            String la_dateapply = cur.getString(cur.getColumnIndex(LA_Date_Apply));
            String la_balance = cur.getString(cur.getColumnIndex(LA_Balance));
            String la_balanceasofdate = cur.getString(cur.getColumnIndex(LA_BalanceAsOfDate));
            Integer la_halfdayind = cur.getInt(cur.getColumnIndex(LA_Half_Day_Ind));
            String la_attachedimage = cur.getString(cur.getColumnIndex(LA_Attached_Image));
            Integer la_approvalstatus = cur.getInt(cur.getColumnIndex(LA_Approval_Status));
            String la_rejectedreason = cur.getString(cur.getColumnIndex(LA_Rejected_Reason));
            Integer la_requestid = cur.getInt(cur.getColumnIndex(LA_RequestID));
            Integer la_workflowtypeid = cur.getInt(cur.getColumnIndex(LA_WorkflowTypeID));
            Integer la_workflowtaskid = cur.getInt(cur.getColumnIndex(LA_WorkflowTaskID));
            String la_approveby = cur.getString(cur.getColumnIndex(LA_Approve_By));
            Integer no_of_record = cur.getCount();
            cur.close();
            return new LeaveContent(la_id, la_empno, la_englishname, la_chinesename, la_nickname, la_leavetype, la_fromdate, la_todate, la_numberofdays, la_dateapply, la_balance, la_balanceasofdate, la_halfdayind, la_attachedimage, la_approvalstatus, la_rejectedreason, la_requestid, la_workflowtypeid, la_workflowtaskid, la_approveby, no_of_record);
        } else {
            Log.i(TAG, "Cursor has NO row of record");
            cur.close();
            return new LeaveContent(0, "", "", "", "", "", "", "", 0.0, "", null, "", 0, "", 0, "", 0, 0, 0, "", 0);
        }
    }

    LeaveBalanceContent getLeaveBalanceByLeaveType(String LeaveType) throws SQLException {
        Log.i(TAG, "Enter getLeaveBalanceByLeaveType");

        String where = "leave_type=?";
        String[] args = {LeaveType};

        Cursor cur = myDB.query(true, TABLE_LEAVEBALANCE, new String[]{LB_Emp_No, LB_Leave_Type, LB_Leave_Description, LB_Leave_Balance, LB_Leave_BalanceAsOfDate, LB_Leave_IsEnforcedAttachment}, where, args, null, null, null, null);

        if (cur != null && (cur.getCount() > 0)) {
            Log.i(TAG, "Number of record: " + cur.getCount());
            if (cur.moveToLast()) {
                String lb_empno = cur.getString(cur.getColumnIndex(LB_Emp_No));
                String lb_leavetype = cur.getString(cur.getColumnIndex(LB_Leave_Type));
                String lb_leavedescription = cur.getString(cur.getColumnIndex(LB_Leave_Description));
                String lb_leavebalance = cur.getString(cur.getColumnIndex(LB_Leave_Balance));
                String lb_leavebalanceasofdate = cur.getString(cur.getColumnIndex(LB_Leave_BalanceAsOfDate));
                boolean lb_leaveisenforcedattachment = !(cur.getInt(cur.getColumnIndex(LB_Leave_IsEnforcedAttachment)) == 0);
                Integer no_of_record = cur.getCount();
                cur.close();
                return new LeaveBalanceContent(lb_empno, lb_leavetype, lb_leavedescription, lb_leavebalance, lb_leavebalanceasofdate, lb_leaveisenforcedattachment, no_of_record);
            }
        } else {
            Log.i(TAG, "Cursor has NO row of record");
            cur.close();
            return new LeaveBalanceContent("", "", "", null, "", false, 0);
        }
        return null;
    }

    LeaveBalanceContent getLeaveBalanceByLeaveDescription(String LeaveDescription) throws SQLException {
        Log.i(TAG, "Enter getLeaveBalanceByLeaveDescription");

        String where = "leave_description=?";
        String[] args = {LeaveDescription};

        Cursor cur = myDB.query(true, TABLE_LEAVEBALANCE, new String[]{LB_Emp_No, LB_Leave_Type, LB_Leave_Description, LB_Leave_Balance, LB_Leave_BalanceAsOfDate, LB_Leave_IsEnforcedAttachment}, where, args, null, null, null, null);

        if (cur != null && (cur.getCount() > 0)) {
            Log.i(TAG, "Number of record: " + cur.getCount());
            if (cur.moveToLast()) {
                String lb_empno = cur.getString(cur.getColumnIndex(LB_Emp_No));
                String lb_leavetype = cur.getString(cur.getColumnIndex(LB_Leave_Type));
                String lb_leavedesc = cur.getString(cur.getColumnIndex(LB_Leave_Description));
                String lb_leavebalance = cur.getString(cur.getColumnIndex(LB_Leave_Balance));
                String lb_leavebalanceasofdate = cur.getString(cur.getColumnIndex(LB_Leave_BalanceAsOfDate));
                boolean lb_leaveisenforcedattachment = !(cur.getInt(cur.getColumnIndex(LB_Leave_IsEnforcedAttachment)) == 0);
                Integer no_of_record = cur.getCount();
                cur.close();
                return new LeaveBalanceContent(lb_empno, lb_leavetype, lb_leavedesc, lb_leavebalance, lb_leavebalanceasofdate, lb_leaveisenforcedattachment, no_of_record);
            }
        } else {
            Log.i(TAG, "Cursor has NO row of record");
            cur.close();
            return new LeaveBalanceContent("", "", "", null, "", false, 0);
        }
        return null;
    }

    ArrayList<String> getLeaveDescription(String EmploymentNumber) {
        Log.i(TAG, "Enter getLeaveDescription");
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        try {
            String selectQuery = "SELECT leave_description FROM " + TABLE_LEAVEBALANCE + " WHERE emp_no = ? and leave_type <> ? and leave_type <> ?";
            Cursor cur = db.rawQuery(selectQuery, new String[]{EmploymentNumber, "AL", "SL"});
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String LeaveDesc = cur.getString(cur.getColumnIndex(LB_Leave_Description));
                    list.add(LeaveDesc);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return list;
    }

    void updateApprovalStatus(Integer id, String emp_no, String from_date, String to_date, Integer new_approval_status, String new_approve_by, String new_rejected_reason) {
        Log.i(TAG, "Enter updateApprovalStatus");
        Log.i(TAG, "ID is: " + id);
        Log.i(TAG, "Emp No. is: " + emp_no);
        Log.i(TAG, "From Date is: " + from_date);
        Log.i(TAG, "To Date is: " + to_date);
        Log.i(TAG, "New Approval Status is: " + "<" + new_approval_status + ">");
        Log.i(TAG, "New Approval By is: " + "<" + new_approve_by + ">");
        Log.i(TAG, "New Rejected Reason is: " + "<" + new_rejected_reason + ">");
        this.getWritableDatabase().execSQL("UPDATE LeaveApply SET approval_status = '" + new_approval_status + "', approve_by = '" + new_approve_by + "', rejected_reason = '" + new_rejected_reason + "' WHERE _id = ' " + id + " ' ");
    }

    void updateLeaveBalance(String emp_number, String leave_type_input, String new_leave_balance, String new_leave_balanceasofdate) {
        Log.i(TAG, "Enter updateLeaveBalance");
        Log.i(TAG, "Emp No. is: " + emp_number);
        Log.i(TAG, "Leave Type is: " + leave_type_input);
        Log.i(TAG, "Leave Balance is: " + new_leave_balance);
        Log.i(TAG, "Leave Balance As Of Date is: " + new_leave_balanceasofdate);
        this.getWritableDatabase().execSQL("UPDATE LeaveBalance SET leave_balance = '" + new_leave_balance + "' WHERE emp_no = ' " + emp_number + " ' AND leave_type = '" + leave_type_input + "' ");
    }

    long deleteUser() {
        return myDB.delete(TABLE_USER, null, null);
    }

    long deleteLeaveBalance() {
        return myDB.delete(TABLE_LEAVEBALANCE, null, null);
    }

    long deleteLeaveApply() {
        return myDB.delete(TABLE_LEAVEAPPLY, null, null);
    }

    long deleteApprovalHistory() {
        return myDB.delete(TABLE_APPROVALHISTORY, null, null);
    }
}