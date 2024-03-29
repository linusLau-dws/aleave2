package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapDropDown;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import hk.com.dataworld.leaveapp.DAL.LeaveModel;

import static hk.com.dataworld.leaveapp.Utility.getSectionString;

public class LeaveRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<LeaveModel> mLeavesToApply = new ArrayList<>();
    private List<String> mListSections;
    private Context mContext;
    private TextView mDayCount;

    public LeaveRecyclerAdapter(Context mContext, TextView mDayCount) {
        this.mContext = mContext;
        this.mDayCount = mDayCount;
        mDayCount.setText(this.mContext.getString(R.string.daycount, 0));
        SQLiteHelper dbHelper;
        dbHelper = new SQLiteHelper(this.mContext);
        dbHelper.openDB();
        UserContent userContent = dbHelper.getUserInfo();
        mListSections = new ArrayList<>();
        if (userContent.getAllow3Sections()) {
            mListSections.add(0, this.mContext.getString(R.string.rb_section));
        }
        if (userContent.getAllowHalfDay()) {
            mListSections.add(0, this.mContext.getString(R.string.rb_pm));
            mListSections.add(0, this.mContext.getString(R.string.rb_am));
        }
        mListSections.add(0, this.mContext.getString(R.string.rb_fullday));
        dbHelper.closeDB();
    }

    public void addLeave(LeaveModel leaveModel) {
        mLeavesToApply.add(leaveModel);
        mDayCount.setText(this.mContext.getString(R.string.daycount, mLeavesToApply.size()));
        notifyDataSetChanged();
        // Refresh
    }

    public List<LeaveModel> getLeaves() {
        return mLeavesToApply;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_leave_apply_row, parent, false);

        return new LeaveRowHolder(mListSections, layout);
    }

    @Override
    public int getItemCount() {
        return mLeavesToApply.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull RecyclerView.ViewHolder holder) {
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (position == 0) {
            ((LeaveRowHolder) holder).mContainer.setBackgroundResource(R.drawable.gradient_header);
            ((LeaveRowHolder) holder).mRemoveBtn.setVisibility(View.INVISIBLE);
            ((LeaveRowHolder) holder).mDateView.setText(R.string.leave_entry_date);
            ((LeaveRowHolder) holder).mDateView.setTextSize(15);
            ((LeaveRowHolder) holder).mDateView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            ((LeaveRowHolder) holder).mTypeView.setText(R.string.leave_entry_type);
            ((LeaveRowHolder) holder).mTypeView.setTextSize(15);
            ((LeaveRowHolder) holder).mTypeView.setTextColor(mContext.getResources().getColor(android.R.color.white));
            ((LeaveRowHolder) holder).mSectionHeader.setText(R.string.leave_entry_date);
            ((LeaveRowHolder) holder).mSectionHeader.setTextSize(15);
            ((LeaveRowHolder) holder).mSectionHeader.setTextColor(mContext.getResources().getColor(android.R.color.white));
            ((LeaveRowHolder) holder).mSectionHeader.setVisibility(View.VISIBLE);
            ((LeaveRowHolder) holder).mSectionControl.setVisibility(View.INVISIBLE);

        } else if (position % 2 == 0) {
            ((LeaveRowHolder) holder).mContainer.setBackgroundColor(mContext.getResources().getColor(R.color.bootstrap_gray_lighter));
            ((LeaveRowHolder) holder).mRemoveBtn.setVisibility(View.VISIBLE);
            ((LeaveRowHolder) holder).mDateView.setText(mLeavesToApply.get(position - 1).getDate());
            ((LeaveRowHolder) holder).mTypeView.setText(mLeavesToApply.get(position - 1).getLeaveDescription());
            ((LeaveRowHolder) holder).mSectionHeader.setText(getSectionString(mContext, mLeavesToApply.get(position - 1).getSection()));
            ((LeaveRowHolder) holder).mSectionHeader.setVisibility(View.INVISIBLE);
            ((LeaveRowHolder) holder).mSectionControl.setVisibility(View.VISIBLE);

        } else {
            ((LeaveRowHolder) holder).mRemoveBtn.setVisibility(View.VISIBLE);
            ((LeaveRowHolder) holder).mDateView.setText(mLeavesToApply.get(position - 1).getDate());
            ((LeaveRowHolder) holder).mTypeView.setText(mLeavesToApply.get(position - 1).getLeaveDescription());
            ((LeaveRowHolder) holder).mSectionHeader.setText(getSectionString(mContext, mLeavesToApply.get(position - 1).getSection()));
            ((LeaveRowHolder) holder).mSectionHeader.setVisibility(View.INVISIBLE);
            ((LeaveRowHolder) holder).mSectionControl.setVisibility(View.VISIBLE);
        }
        ((LeaveRowHolder) holder).mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLeavesToApply.remove(position - 1);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }


    class LeaveRowHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mContainer;
        ImageView mRemoveBtn;
        TextView mDateView;
        TextView mTypeView;
        TextView mSectionHeader;
        BootstrapDropDown mSectionControl;

        LeaveRowHolder(final List<String> list, View layout) {
            super(layout);
            mContainer = layout.findViewById(R.id.row_container);
            mRemoveBtn = layout.findViewById(R.id.row0);
            mDateView = layout.findViewById(R.id.row1);
            mTypeView = layout.findViewById(R.id.row2);
            mSectionHeader = layout.findViewById(R.id.row3header);
            mSectionControl = layout.findViewById(R.id.row3dropdown);
            mSectionControl.setDropdownData(list.toArray(new String[0]));
            mSectionControl.setOnDropDownItemClickListener(new BootstrapDropDown.OnDropDownItemClickListener() {
                @Override
                public void onItemClick(ViewGroup parent, View v, int id) {
                    mSectionControl.setText(list.get(id));
                }
            });
        }
    }
}
