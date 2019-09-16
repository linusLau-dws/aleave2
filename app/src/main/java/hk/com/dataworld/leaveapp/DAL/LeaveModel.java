package hk.com.dataworld.leaveapp.DAL;

public class LeaveModel {
    private String date;
    private String leaveCode;
    private String leaveDescription;
    private int section;

    public LeaveModel(String date, String leaveCode, String leaveDescription, int section) {
        this.date = date;
        this.leaveCode = leaveCode;
        this.leaveDescription = leaveDescription;
        this.section = section;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLeaveCode() {
        return leaveCode;
    }

    public void setLeaveCode(String leaveCode) {
        this.leaveCode = leaveCode;
    }

    public String getLeaveDescription() {
        return leaveDescription;
    }

    public void setLeaveDescription(String leaveDescription) {
        this.leaveDescription = leaveDescription;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }
}
