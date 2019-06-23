package hk.com.dataworld.leaveapp;

public class LeaveBalanceContent {
    private String lb_emp_no;
    private String lb_leave_type;
    private String lb_leave_description;
    private String lb_leave_balance;
    private String lb_leave_balanceasofdate;
    private boolean lb_leave_isenforcedattachment;
    private Integer count;

    LeaveBalanceContent(String e, String t, String ld, String b, String bd, boolean att, Integer x) {
        lb_emp_no = e;
        lb_leave_type = t;
        lb_leave_description = ld;
        lb_leave_balance = b;
        lb_leave_balanceasofdate = bd;
        lb_leave_isenforcedattachment = att;
        count = x;
    }

    String getLBEmploymentNumber() {
        return lb_emp_no;
    }

    String getLBLeaveType() {
        return lb_leave_type;
    }

    String getLBLeaveDescription() {
        return lb_leave_description;
    }

    String getLBLeaveBalance() {
        return lb_leave_balance;
    }

    String getLBLeaveBalanceAsOfDate() {
        return lb_leave_balanceasofdate;
    }

    boolean getLBIsEnforcedAttachment() {
        return lb_leave_isenforcedattachment;
    }

    Integer getCount() {
        return count;
    }
}