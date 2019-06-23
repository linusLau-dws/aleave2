package hk.com.dataworld.leaveapp.DAL;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable {
    private String LeaveDesc;
    private String LeaveFrom;
    private String LeaveTo;
    private String ReviewerName;
    private String CreateDate;
    private int Status;

    public Notification(String leaveDesc, String leaveFrom, String leaveTo, String reviewerName, String createDate, int status){
        this.LeaveDesc = leaveDesc;
        this.LeaveFrom = leaveFrom;
        this.LeaveTo = leaveTo;
        this.ReviewerName = reviewerName;
        this.CreateDate = createDate;
        this.Status = status;
    }

    public String getLeaveDesc() {
        return LeaveDesc;
    }

    public String getLeaveFrom() {
        return LeaveFrom;
    }

    public String getLeaveTo() {
        return LeaveTo;
    }

    public String getReviewerName() {
        return ReviewerName;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public int getStatus() {
        return Status;
    }

    protected Notification(Parcel in) {
        LeaveDesc = in.readString();
        LeaveFrom = in.readString();
        LeaveTo = in.readString();
        ReviewerName = in.readString();
        CreateDate = in.readString();
        Status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(LeaveDesc);
        dest.writeString(LeaveFrom);
        dest.writeString(LeaveTo);
        dest.writeString(ReviewerName);
        dest.writeString(CreateDate);
        dest.writeInt(Status);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Notification> CREATOR = new Parcelable.Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}