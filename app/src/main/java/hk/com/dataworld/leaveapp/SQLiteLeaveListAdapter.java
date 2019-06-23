package hk.com.dataworld.leaveapp;

/**
 * Created by Terence on 2018/2/28.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import static hk.com.dataworld.leaveapp.Utility.getDayOfWeekSuffixedString;

//import android.graphics.Color;

public class SQLiteLeaveListAdapter extends BaseAdapter {

    private static final String TAG = SQLiteLeaveListAdapter.class.getSimpleName();

    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper dbHelper;

    Context mContext;
    ArrayList<String> User_Id;
    ArrayList<String> User_Approval_Status;
    ArrayList<String> User_Name;
    ArrayList<String> User_Leave_Type;
    ArrayList<String> User_Leave_Start_Date;
    ArrayList<String> User_Leave_End_Date;
    ArrayList<String> User_Total_Leave_Days;
    ArrayList<String> User_Remain_Leave_Days;


    public SQLiteLeaveListAdapter(
            Context context2,
            ArrayList<String> id,
            ArrayList<String> approval_status,
            ArrayList<String> name,
            ArrayList<String> leave_type,
            ArrayList<String> leave_start_date,
            ArrayList<String> leave_end_date,
            ArrayList<String> total_leave_days,
            ArrayList<String> remain_leave_days
    ) {

        this.mContext = context2;
        this.User_Id = id;
        this.User_Approval_Status = approval_status;
        this.User_Name = name;
        this.User_Leave_Type = leave_type;
        this.User_Leave_Start_Date = leave_start_date;
        this.User_Leave_End_Date = leave_end_date;
        this.User_Total_Leave_Days = total_leave_days;
        this.User_Remain_Leave_Days = remain_leave_days;
    }

    @Override
    public int getCount() {
        return User_Id.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View child, ViewGroup parent) {
        final Holder holder;
//        Button approveButton;

        LayoutInflater layoutInflater;

        if (child == null) {
            layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            child = layoutInflater.inflate(R.layout.leavelistviewdatalayout, null);

            holder = new Holder();

            holder.textviewid = (TextView) child.findViewById(R.id.tvId);
            holder.textviewstatus = (TextView) child.findViewById(R.id.tvApprovalStatus);
            holder.textviewname = (TextView) child.findViewById(R.id.tvSeqNo);
            holder.textviewleavetype = (TextView) child.findViewById(R.id.tvUserName);
            holder.textviewstartdate = (TextView) child.findViewById(R.id.contentStartDate);
            holder.textviewenddate = (TextView) child.findViewById(R.id.contentEndDate);
            holder.textviewtotalleavedays = (TextView) child.findViewById(R.id.tvRemark);
            holder.textView9 = (TextView) child.findViewById(R.id.textView9);
            holder.textviewremainleavedays = (TextView) child.findViewById(R.id.tvRemainDays);

            child.setTag(holder);

        } else {

            holder = (Holder) child.getTag();
        }
/*
        approveButton = (Button) child.findViewById(R.id.btnApprove);
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "La_id is: " + User_Id);
                Log.i(TAG, "La_id is: " + holder.textviewid.getText());
                Integer userid = (Integer.parseInt(holder.textviewid.getText().toString())) - 1;
                Log.i(TAG, "userid is: " + userid);
                Log.i(TAG, "OLD Approval Status is: " + "<" + User_Approval_Status + ">");
                Log.i(TAG, "OLD Approval Status is: " + "<" + holder.textviewstatus.getText() + ">");

                Intent intent = new Intent(mContext,LeaveApprovalActivity.class);
                intent.putExtra("position", userid);
                Log.i(TAG, "Will go to LeaveApprovalActivity");
                mContext.startActivity(intent);
            }
        });
*/
        holder.textviewid.setText(User_Id.get(position));
        holder.textviewid.setVisibility(View.GONE);
        holder.textviewstatus.setText(User_Approval_Status.get(position));
        holder.textviewstatus.setVisibility(View.GONE);
        holder.textviewname.setText(User_Name.get(position));
        holder.textviewleavetype.setText(User_Leave_Type.get(position));
        holder.textviewstartdate.setText(getDayOfWeekSuffixedString(mContext, User_Leave_Start_Date.get(position)));
        holder.textviewenddate.setText(getDayOfWeekSuffixedString(mContext, User_Leave_End_Date.get(position)));
        holder.textviewtotalleavedays.setText(User_Total_Leave_Days.get(position));
        holder.textView9.setVisibility(View.GONE);
        holder.textviewremainleavedays.setText(User_Remain_Leave_Days.get(position));
        holder.textviewremainleavedays.setVisibility(View.GONE);
//        Log.i(TAG, "Approval Status for holder is: " + User_Approval_Status.get(position));

//        approveButton.setAlpha(0f);
//        approveButton.setClickable(false);

/*
        Log.i(TAG, "Approval Status for constant value: " + (String.valueOf(R.string.tv_rejectStatus).toString()));
        if ((User_Approval_Status.get(position) == 3 || (User_Approval_Status.get(position).equals("申請己被拒絕")))) {
            holder.textviewstatus.setBackgroundColor(Color.RED);
            approveButton.setAlpha(0f);
            approveButton.setClickable(false);
        } else if ((User_Approval_Status.get(position).equals("Accepted") || (User_Approval_Status.get(position).equals("申請己被接納")))) {
            holder.textviewstatus.setBackgroundColor(Color.GREEN);
            approveButton.setAlpha(0f);
            approveButton.setClickable(false);
        } else {
            holder.textviewstatus.setBackgroundColor(Color.YELLOW);
            approveButton.setAlpha(0f);
            approveButton.setClickable(false);
        }
*/
        return child;
    }

    public class Holder {
        TextView textviewid;
        TextView textviewname;
        TextView textviewstatus;
        TextView textviewleavetype;
        TextView textviewstartdate;
        TextView textviewenddate;
        TextView textviewtotalleavedays;
        TextView textView9;
        TextView textviewremainleavedays;
    }

}
