# RosterManagement 713
`PayrollMonth = p.PayrollMonth;`
- B.S.: Basic Salary
- R.I.: Regular Income
7月計6月OT
t_Leave.IsAdjustInNextMonth
t_PayItemType.IsAdjustInNextMonth
t_PayrollTrial_Snapshot_Leave.IsAdjustInNextMonth
t_PayrollTrial_Snapshot_PayItem.IsAdjustInNextMonth
t_PayrollTrial_Snapshot_SH.IsAdjustInNextMonth
t_StatutoryHolidayCalculation.IsAdjustInNextMonth

問題：Leave Period月頭至月頭，Pay Period月中至月中。
- `RosterManagement.asmx.cs` Line 520 `bool Calculate12MonthForAllStaff(int PayrollPeriodID, int ContractID, int UserID, DAL.Model.SystemParameterInfo[] p_objSystemParameter, bool p_IsTrial)`
- Line 2426 `bool CalculatePayrollTrialByEmploymentIDArray(int PayrollPeriodID, int[] EmploymentIDArray, int UserID, string TimeTokenString, int p_intPayMethod, bool p_bolIsUpdateSnapshotOnly, bool p_bolIsBackPayMode)` `p_bolIsUpdateSnapshotOnly` Pay Period 一定是當月 Update 713 only snapshot -> Not support 月中至月中

-L.S.P.: Long-service pension
201906  NPL*3 $8100  27days
201908  NPL*1 $8700 <- minimum wages

NotePrint玩法=Snapshot玩法
201906撥去201907，201907自己不計，八月重計七月。
201906  ($9000) NPL*3 9000(3)/30 = 900; 9000-900 = 8100; *27days
