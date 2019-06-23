package hk.com.dataworld.leaveapp.DAL;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkflowStep implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WorkflowStep> CREATOR = new Parcelable.Creator<WorkflowStep>() {
        @Override
        public WorkflowStep createFromParcel(Parcel in) {
            return new WorkflowStep(in);
        }

        @Override
        public WorkflowStep[] newArray(int size) {
            return new WorkflowStep[size];
        }
    };
    private String Name;
    private String Position;
    private String ApproveDate;
    private String ApproveTime;
    private String Notes;

    public WorkflowStep(String name, String position, String approveDate, String approveTime, String notes) {
        Name = name;
        Position = position;
        ApproveDate = approveDate;
        ApproveTime = approveTime;
        Notes = notes;
    }

    protected WorkflowStep(Parcel in) {
        Name = in.readString();
        Position = in.readString();
        ApproveDate = in.readString();
        ApproveTime = in.readString();
        Notes = in.readString();
    }

    public String getName() {
        return Name;
    }

    public String getPosition() {
        return Position;
    }

    public String getApproveDate() {
        return ApproveDate;
    }

    public String getApproveTime() {
        return ApproveTime;
    }

    public String getNotes() {
        return Notes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeString(Position);
        dest.writeString(ApproveDate);
        dest.writeString(ApproveTime);
        dest.writeString(Notes);
    }
}
