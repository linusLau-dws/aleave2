package hk.com.dataworld.leaveapp;

/**
 * Created by Terence on 2018/2/28.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hk.com.dataworld.leaveapp.DAL.WorkflowStep;

import static hk.com.dataworld.leaveapp.Utility.getDayOfWeekSuffixedString;

//import android.graphics.Color;

public class SQLiteHistoryListAdapter extends BaseAdapter {

    private static final String TAG = SQLiteHistoryListAdapter.class.getSimpleName();

    SQLiteDatabase SQLITEDATABASE;
    SQLiteHelper dbHelper;

    Context mContext;
    List<WorkflowStep> mWorkflowSteps;
//    ArrayList<String> ApprovalHistory_Id;
//    ArrayList<String> ApprovalHistory_SeqNo;
//    ArrayList<String> ApprovalHistory_UserName;
//    ArrayList<String> ApprovalHistory_Position;
//    ArrayList<String> ApprovalHistory_Date;
//    ArrayList<String> ApprovalHistory_Time;
//    ArrayList<String> ApprovalHistory_Remark;


    public SQLiteHistoryListAdapter(Context context, ArrayList<WorkflowStep> workflowSteps) {
        mContext = context;
        mWorkflowSteps = workflowSteps;
    }

    public int getCount() {
        Log.i("TESTTEST", String.valueOf(mWorkflowSteps.size()));
        return mWorkflowSteps.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View child, ViewGroup parent) {

        final Holder holder;

        LayoutInflater layoutInflater;

        if (child == null) {
            layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            child = layoutInflater.inflate(R.layout.historylistviewdatalayout, null);

            holder = new Holder();

            holder.textviewid = (TextView) child.findViewById(R.id.tvId);
            holder.textviewseqno = (TextView) child.findViewById(R.id.tvSeqNo);
            holder.textviewusername = (TextView) child.findViewById(R.id.tvUserName);
            holder.textviewposition = (TextView) child.findViewById(R.id.tvPosition);
            holder.textviewdatetime = (TextView) child.findViewById(R.id.contentStartDate);
            holder.textviewremark = (TextView) child.findViewById(R.id.tvRemark);

            child.setTag(holder);

        } else {

            holder = (Holder) child.getTag();
        }

        //holder.textviewid.setText(ApprovalHistory_Id.get(position));
        holder.textviewid.setVisibility(View.GONE);
        holder.textviewseqno.setText(mContext.getString(R.string.tv_seqno, position + 1));
        holder.textviewusername.setText(mContext.getString(R.string.tv_username, mWorkflowSteps.get(position).getName()));
        holder.textviewposition.setText(mContext.getString(R.string.tv_position, mWorkflowSteps.get(position).getPosition()));
        //getDayOfWeekSuffixedString(mContext, mWorkflowSteps.get(position).getApproveDate())
        holder.textviewdatetime.setText(mContext.getString(R.string.tv_datetime, getDayOfWeekSuffixedString(mContext, mWorkflowSteps.get(position).getApproveDate()), mWorkflowSteps.get(position).getApproveTime()));
        holder.textviewremark.setText(mContext.getString(R.string.tv_remark, mWorkflowSteps.get(position).getNotes().equals("null") ? "--" : mWorkflowSteps.get(position).getNotes()));
        return child;
    }

    public class Holder {
        TextView textviewid;
        TextView textviewseqno;
        TextView textviewusername;
        TextView textviewposition;
        TextView textviewdatetime;
        TextView textviewremark;
    }

}
