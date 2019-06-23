package hk.com.dataworld.leaveapp;

public class ApprovalHistoryContent {
    private String ah_empno;
    private Integer ah_seqno;
    private String ah_approvername;
    private String ah_position;
    private String ah_date;
    private String ah_time;
    private String ah_remark;
    private Integer count;


    //public WorkerContent(String n, String h, String cd, String b, String l, String c, Bitmap p) {
    public ApprovalHistoryContent(String en, Integer sn, String an, String pos, String da, String ti, String rm, Integer x) {
        ah_empno = en;
        ah_seqno = sn;
        ah_approvername = an;
        ah_position = pos;
        ah_date = da;
        ah_time = ti;
        ah_remark = rm;
        count = x;
    }

    public String getEmpNo() {
        return ah_empno;
    }

    public Integer getSeqNo() {
        return ah_seqno;
    }

    public String getApproverName() {
        return ah_approvername;
    }

    public String getPosition() {
        return ah_position;
    }

    public String getDate() {
        return ah_date;
    }

    public String getTime() {
        return ah_time;
    }

    public String getRemark() {
        return ah_remark;
    }

    public Integer getCount() {
        return count;
    }
}
