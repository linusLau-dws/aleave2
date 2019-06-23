package hk.com.dataworld.leaveapp;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapDropDown;
import com.beardedhen.androidbootstrap.api.defaults.ExpandDirection;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class MtrDropDown extends BootstrapDropDown {
    private LinkedHashMap<String, String> mMapLines = null;
    private LinkedHashMap<String, Integer> mMapStations = null;
    private Object mCurrent;

    private int mOffset = 0; // May not be used by most dropdowns

    public MtrDropDown(Context context) {
        super(context);
    }

    public MtrDropDown(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MtrDropDown(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setCurrent(int id) {
        mCurrent = mMapLines.values().toArray()[id];
    }

    public void setUpString(LinkedHashMap<String, String> map) {
        setEnabled(true);
        mMapLines = map;
        setDropdownData(mMapLines.keySet().toArray(new String[0]));
    }

    public void setUpInt(LinkedHashMap<String, Integer> map) {
        setEnabled(true);
        mMapStations = map;
        String[] strings = mMapStations.keySet().toArray(new String[0]);
        setDropdownData(strings);
        mCurrent = mMapStations.values().toArray()[0];
        setText(strings[0]);
    }

    public void setUpIntOnClickContent(int id) {
        mCurrent = mMapStations.values().toArray()[id];
        setText((String) mMapStations.keySet().toArray()[id]);
    }

    public void setUpOtherIntOnClickContentConfine(int id) {
        mCurrent = mMapStations.values().toArray()[id + mOffset];
        setText((String) mMapStations.keySet().toArray()[id + mOffset]);
    }

    public String getmCurrentText(int id) {
        return (String) mMapLines.keySet().toArray()[id];
    }

    public String getmCurrent(int id) {
        return (String) mMapLines.values().toArray()[id];
    }

    public Object getmCurrent() {
        return mCurrent;
    }

    public void confineRangeInt(int position) {
        mOffset = position + 1;
        String[] strs = Arrays.copyOfRange(mMapStations.keySet().toArray(new String[0]), mOffset, mMapStations.size());
        setDropdownData(strs);
        setText((String) mMapStations.keySet().toArray()[mOffset]);
        mCurrent = mMapStations.values().toArray()[mOffset];
    }

    public void dropLast() {
        String[] strs = Arrays.copyOfRange(mMapStations.keySet().toArray(new String[0]), 0, mMapStations.size() - 1);
        setDropdownData(strs);
    }

    @Override
    public void setOnDropDownItemClickListener(OnDropDownItemClickListener onDropDownItemClickListener) {
        super.setOnDropDownItemClickListener(onDropDownItemClickListener);
    }

    @Override
    public boolean isShowOutline() {
        return super.isShowOutline();
    }

    @Override
    public void setShowOutline(boolean showOutline) {
        super.setShowOutline(showOutline);
    }

    @Override
    public boolean isRounded() {
        return super.isRounded();
    }

    @Override
    public void setRounded(boolean rounded) {
        super.setRounded(rounded);
    }

    @Override
    public ExpandDirection getExpandDirection() {
        return super.getExpandDirection();
    }

    @Override
    public void setExpandDirection(ExpandDirection expandDirection) {
        super.setExpandDirection(expandDirection);
    }

    @Override
    public String[] getDropdownData() {
        return super.getDropdownData();
    }

    @Override
    public void setDropdownData(String[] dropdownData) {
        super.setDropdownData(dropdownData);
    }

    @Override
    public void onDismiss() {
        super.onDismiss();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setOnClickListener(OnClickListener clickListener) {
        super.setOnClickListener(clickListener);
    }
}
