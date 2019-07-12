Amazon Redshift is based on **PostgreSQL 8.0.2**. Amazon Redshift and PostgreSQL have a number of very important differences that you must be aware of as you design and develop your data warehouse applications. For more information about how Amazon Redshift SQL differs from PostgreSQL, see Amazon Redshift and PostgreSQL.

Rietveld / Launchpad / Rietveld 2

# RosterManagement 713=='12-month basic salary' (平均工資)
`PayrollMonth = p.PayrollMonth;`
- B.S.: Basic Salary
- R.I.: Regular Income
7月計6月OT
- t_Leave.IsAdjustInNextMonth
- t_PayItemType.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_Leave.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_PayItem.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_SH.IsAdjustInNextMonth
- t_StatutoryHolidayCalculation.IsAdjustInNextMonth

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

Others -> 如果7月未計糧，6月錯。(旮旯月分)
#### IsAdjustInNextMonth
- t_Leave.IsAdjustInNextMonth.IsAdjustInNextMonth
- t_PayItemType.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_Leave.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_PayItem.IsAdjustInNextMonth
- t_PayrollTrial_Snapshot_SH.IsAdjustInNextMonth
- t_StatutoryHolidayCalculation

## Termination
#### SubtotalExcludeTermination
- t_PayrollTrial.SubtotalExcludeTermination
- t_PayrollTrial_BackPay.SubtotalExcludeTermination
- t_PayrollTrial_CPA.SubtotalExcludeTermination

Subtotal = CalculateAL (未放AL) + CalculateST (未放ST) + CaculateGratuity (如 End of notice != (Terminate Effective Date - 1day))，就一定要給代通知金，只是看誰給 (負數：員工給)。要run多次MPF Calculation: 

- t_StaffTermination_Detail.CalculatePaymentInLieu
- t_StaffTermination_Detail.CalculateServancePayment
- t_StaffTermination_Detail.CalculateLongServicePayment

未放AL  未放ST

# Web.config
NextGen 用 SQLService，所以no provilege to backup (make .bak)，Use the tool `SQLAzureMW (SQL Server Database Migration Wizard)`, now replaced by `Data Migration Assistant (DMA)`.

# IsCL
t_Employment.IsCL means Is Casual Labour 散工 (Standard+碧). (Baguio has no IsPartTime checkbox). It's possible to be part to be part-time but not casual labour. (Kitty's handover 有個表。)
Boss: 每個禮拜做5日，rest點。
Baguio:
- All leaves hard-coded.
- All leaves: 1 day, NO half day.
月中轉Post，就要Renew employment。
RosterManagement.asmx Line 13929 `void ImportPlannedRosterCentralizeMethod(int p_intPayrollPeriodID, DAL.Model.PlannedRoster[] pPlannedRoster, string psUploadMode, int piTotalNoOfPlannedToImportEmployment, int piUserID, bool boolImportToShiftSection)`

Baguio: does not have Portal. Everything is obtained from t_Roster, never t_Staff_Leave. Roster should be in sync w/ Leave. Roster is hard code. UI->Delete Staff Leave->Need to sync w/ Roster.

Standard: t_Staff_Leave

補 Positional Difference (P.D.)

# AL
0.01 per day
## Tricky: Adjustment vs Override
Balance of 2019-07-10: 3.76 Days

Adjustment on 2019-07-11: +0.5; Balance on 2019-07-11: 3.76 + 0.5 **+0.01**

But if Override on 2019-07-11: Result = 2 (**IGNORE ALL ADJUSTMENTS**)

The label `Adjustment Date` is actually `Adjust/Override Day`

- Join Date: 2019-01-01
- Adjustment Date: 2019-01-01
- Scheme Start Calc Date: 2020-01-01
- 入 company 已享第二年adjustment

Common Leave Year Law:

1st year|2019-05-01|2019-12-31|But some companies: 1st year

*still* 1st year|2020-01-01|2020-12-31|But some companies: 2nd year

`OT_PUR` (stands for OT Purchase, formally 'Pay in lieu of OT') is a payitem type. 買OT Balance。
