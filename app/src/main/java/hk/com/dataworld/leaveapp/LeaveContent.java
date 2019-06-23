package hk.com.dataworld.leaveapp;

/**
 * Created by Terence on 2018/2/5.
 */


public class LeaveContent {
    private Integer la_id;
    private String la_emp_no;
    private String la_english_name;
    private String la_chinese_name;
    private String la_nickname;
    private String la_leave_type;
    private String la_from_date;
    private String la_to_date;
    private Double la_number_of_days;
    private String la_date_apply;
    private String la_balance;
    private String la_balance_as_of_date;
    private Integer la_half_day_ind;
    private String la_attached_image;
    private Integer la_approval_status;
    private String la_rejected_reason;
    private Integer la_requestid;
    private Integer la_workflowtypeid;
    private Integer la_workflowtaskid;
    private String la_approve_by;
    private Integer count;

    public LeaveContent(Integer i, String e, String en, String cn, String nn, String l, String f, String t, Double n, String da, String lb, String lbd, Integer h, String s, Integer as, String r, Integer rid, Integer wtype, Integer wtask, String ab, Integer x) {
        la_id = i;
        la_emp_no = e;
        la_english_name = en;
        la_chinese_name = cn;
        la_nickname = nn;
        la_leave_type = l;
        la_from_date = f;
        la_to_date = t;
        la_number_of_days = n;
        la_date_apply = da;
        la_balance = lb;
        la_balance_as_of_date = lbd;
        la_half_day_ind = h;
        la_attached_image = s;
        la_approval_status = as;
        la_rejected_reason = r;
        la_requestid = rid;
        la_workflowtypeid = wtype;
        la_workflowtaskid = wtask;
        la_approve_by = ab;
        count = x;
    }

    public Integer getId() {
        return la_id;
    }

    public String getEmpNo() {
        return la_emp_no;
    }

    public String getEnglishName() {
        return la_english_name;
    }

    public String getChineseName() {
        return la_chinese_name;
    }

    public String getNickname() {
        return la_nickname;
    }

    public String getLeaveType() {
        return la_leave_type;
    }

    public String getFromDate() {
        return la_from_date;
    }

    public String getToDate() {
        return la_to_date;
    }

    public Double getNumberOfDays() {
        return la_number_of_days;
    }

    public String getDateApply() {
        return la_date_apply;
    }

    public String getBalance() {
        return la_balance;
    }

    public String getBalanceAsOfDate() {
        return la_balance_as_of_date;
    }

    public Integer getHalfDayInd() {
        return la_half_day_ind;
    }

    public String getAttachedImage() {
        return la_attached_image;
    }

    public Integer getApprovalStatus() {
        return la_approval_status;
    }

    public String getRejectedReason() {
        return la_rejected_reason;
    }

    public Integer getRequestId() {
        return la_requestid;
    }

    public Integer getWorkflowTypeId() {
        return la_workflowtypeid;
    }

    public Integer getWorkflowTaskId() {
        return la_workflowtaskid;
    }

    public String getApproveBy() {
        return la_approve_by;
    }

    public Integer getCount() {
        return count;
    }

}
