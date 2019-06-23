package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import hk.com.dataworld.leaveapp.DAL.WorkflowStep;

import static hk.com.dataworld.leaveapp.Constants.EXTRA_WORKFLOW_STEPS;

public class HistoryMasterRecordActivity extends BaseActivity {

    private static final String TAG = LeaveMasterRecordActivity.class.getSimpleName();

    SQLiteHelper SQLITEHELPER;
    SQLiteDatabase SQLITEDATABASE;
    Cursor cursor;
    SQLiteHistoryListAdapter ListAdapter;
    SQLiteHelper dbHelper;

    ArrayList<String> ID_ArrayList = new ArrayList<>();
    ArrayList<String> SEQNO_ArrayList = new ArrayList<>();
    ArrayList<String> USERNAME_ArrayList = new ArrayList<>();
    ArrayList<String> POSITION_ArrayList = new ArrayList<>();
    ArrayList<String> DATE_ArrayList = new ArrayList<>();
    ArrayList<String> TIME_ArrayList = new ArrayList<>();
    ArrayList<String> REMARK_ArrayList = new ArrayList<>();
    ListView LISTVIEW;
    TextView Heading;
    Button CloseButton;

    String TempEmploymentNo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_master_record);

        Log.i(TAG, "Enter onCreateHistoryMasterRecordActivity");

        if (getIntent().hasExtra("GetApprovalHist")) {
            Log.i(TAG, "Enter from GetApprovalHist");
            TempEmploymentNo = getIntent().getStringExtra("GetApprovalHist");
            Log.i(TAG, "TempEmploymentNo is: " + TempEmploymentNo);
        }

        Heading = (TextView) findViewById(R.id.tvHeadingTitle);

        CloseButton = (Button) findViewById(R.id.btnCloseScreen);

        CloseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.i(TAG, "Will back to previous page");
                finish();
            }
        });

        LISTVIEW = (ListView) findViewById(R.id.lvProgress);

        SQLITEHELPER = new SQLiteHelper(this);

        dbHelper = new SQLiteHelper(HistoryMasterRecordActivity.this);

        LISTVIEW.setAdapter(ListAdapter);

        ShowSQLiteDBdata(TempEmploymentNo);
    }

    public void DBCreate() {

        dbHelper.openDB();

        SQLITEDATABASE = openOrCreateDatabase("HRMSDataBase", Context.MODE_PRIVATE, null);

        SQLITEDATABASE.execSQL("CREATE TABLE IF NOT EXISTS ApprovalHistory(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, emp_no VARCHAR, sequence_no VARCHAR, approver_name VARCHAR, position VARCHAR, date VARCHAR, time VARCHAR, remark VARCHAR);");

        dbHelper.closeDB();
    }

    private void ShowSQLiteDBdata(String empno) {

        Log.i(TAG, "Enter ShowSQLiteDBdata");

        SQLITEDATABASE = SQLITEHELPER.getWritableDatabase();

        dbHelper.openDB();

        String[] args = {empno};

        cursor = SQLITEDATABASE.rawQuery(
                "SELECT _id, sequence_no, approver_name, position, date, time, remark FROM ApprovalHistory " +
                        " WHERE emp_no = ? " +
                        " ORDER BY sequence_no ASC ",
                args, null);

        Log.i(TAG, "Set Cursor for ApprovalHistory");

        ID_ArrayList.clear();
        SEQNO_ArrayList.clear();
        USERNAME_ArrayList.clear();
        POSITION_ArrayList.clear();
        DATE_ArrayList.clear();
        TIME_ArrayList.clear();
        REMARK_ArrayList.clear();

        Log.i(TAG, "Set Array List");

        if (cursor.moveToFirst()) {
            do {
                ID_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.LA_ID)));
                SEQNO_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Sequence_No)));
                USERNAME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Approver_Name)));
                POSITION_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Position)));
                DATE_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Date)));
                TIME_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Time)));
                REMARK_ArrayList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.AH_Remark)));
            } while (cursor.moveToNext());
        }

        ArrayList<WorkflowStep> wfs = getIntent().getParcelableArrayListExtra(EXTRA_WORKFLOW_STEPS);
        Log.i("wfs", String.valueOf(wfs.size()));
        ListAdapter = new SQLiteHistoryListAdapter(HistoryMasterRecordActivity.this,
                wfs);

        LISTVIEW.setAdapter(ListAdapter);

        cursor.close();

        dbHelper.closeDB();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Enter onResume");
        super.onResume();
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        menu.removeItem(R.id.action_notification);
        return true;
    }
}
