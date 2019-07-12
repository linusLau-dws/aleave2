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

201906  ($9000) NPL*3 $9000(3)/30 = 900; $9000-900 = $8100; *27days

201907  ($12000)  NPL*3 $9000(3)/30 = 900 (201906); $12000-900 = 11100; * **28 wrong**

201908 ($14000) NPL*1 $12000(3)/31  = 900 (201907)

$11600 30 **CORRECT**

### Back Pay Mode
PayItem 的 PayrollMonth 是 713 month。
PayrollMonth|ActualRateFrom|ActualRateTo
201901|2019-03-01|2019-03-31
201902|
1. BackPay (R.I. - Relevant Income)
2. Gratuity Income
3. BackPay (I.R. - Irrelevant Income)
好似PayItem，but update BackPay, Wages as well。最後清F99。

`GenerateBackPayID` For Report. Value will never be 99 <- temp only. Means there is back pay.

Checkboxes: - Terminate (當月) != Quit (不理何月Quit，總知那月已Quit)。

### Include in B.S. (Basic Salary)?
NPL 12000/30 分開計仲好。NPL = 10000/30 = 333.33. Allowance = 2000/30 = 66.67.
碧 -> Fixed Pipeline, staight-forward: 計糧 -> 713
Others -> 如果7月未計糧，6月錯。

SELECT DISTINCT TABLE_NAME 
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE COLUMN_NAME IN ('SubtotalExcludeTermination')

	t_Leave.IsAdjustInNextMonth
	t_PayItemType
t_PayrollTrial_Snapshot_Leave
t_PayrollTrial_Snapshot_PayItem
t_PayrollTrial_Snapshot_SH
t_StatutoryHolidayCalculation

t_PayrollTrial.SubtotalExcludeTermination
t_PayrollTrial_BackPay.SubtotalExcludeTermination
t_PayrollTrial_CPA.SubtotalExcludeTermination
