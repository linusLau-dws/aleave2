package hk.com.dataworld.leaveapp.DAL;

import android.os.Parcel;
import android.os.Parcelable;

public class MtrStation implements Parcelable {
    private String line, name;
    private int ID;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(line);
        dest.writeString(name);
        dest.writeInt(ID);
    }

    public static final Parcelable.Creator<MtrStation> CREATOR
            = new Parcelable.Creator<MtrStation>() {
        public MtrStation createFromParcel(Parcel in) {
            return new MtrStation(in);
        }

        public MtrStation[] newArray(int size) {
            return new MtrStation[size];
        }
    };

    private MtrStation(Parcel in) {
        line = in.readString();
        name = in.readString();
        ID = in.readInt();
    }
}
