Amazon Redshift is based on **PostgreSQL 8.0.2**. Amazon Redshift and PostgreSQL have a number of very important differences that you must be aware of as you design and develop your data warehouse applications. For more information about how Amazon Redshift SQL differs from PostgreSQL, see Amazon Redshift and PostgreSQL.

[Install the Amazon Redshift ODBC Driver on Linux Operating Systems](https://docs.aws.amazon.com/redshift/latest/mgmt/install-odbc-driver-linux.html)

No repos. No Ubuntu Package. Install .deb directly.

Systemd relies on Meson since dropping Autotools in version 234.

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
# SL
## Definition of 'Continuous SL'
`SICK_LEAVE_CONSECUTIVE_DAY_DEFINITION`: Sick Leave Consecutive Day Definition (Labour Law = 4)
不是連續4日或以上，可以Pay或No Pay (有NSL Type)，但不能扣Balance。4日或以上，可以扣。

Labour Law (double check):
Year|Threshold Days
1|2
2|4
Max|120

- Baguio
* SL: $ * 4/5
* NSL: No Pay

- Standard
* PSL: Partial Sick Leave: $ * 4/5
* SL: Follows `t_PayCodeFormula`
* FSL: Full Pay
* NSL: No Pay

#### Cases when BH and SL overlap / R and SL overlap
- `LEAVE_WAIVE_SLBALANCE_DEDUCTION_ON_BANKHOLIDAY`: Leave - Enable system to waive the deduction on SL balance when the SL application date is a bank holiday (0 = No, 1 = Yes)
- `LEAVE_WAIVE_SLBALANCE_DEDUCTION_ON_RESTDAY`: Leave - Enable system to waive the deduction on SL balance when the SL application date is a rest day (0 = No, 1 = Yes)

### Formula
- `t_PayCodeFormula`
- `t_PayCodeFormulaItem`
[Day of SH] * ([Basic Salary] / [Calendar Day]) + [Day of BH]

NOTE: [Calendar Day] means Total Days in Month

`DWHRMS_Master\Calculation\PayrollTrial.cs` Line 5288 `PayrollObject[] CalculatePayrollTrial(DAL.Model.PayrollTrialResources p_objPayrollTrialResources)`:
```c#
//v1.8.1 Fai 2018.01.30 - Support Day of Leave in in SH / BH - Begin
DateInBH = g.SelectMany(x => x.DateInBH).Distinct().ToArray(),
DateInSH = g.SelectMany(x => x.DateInSH).Distinct().ToArray(),
//v1.8.1 Fai 2018.01.30 - Support Day of Leave in in SH / BH - End

DateList = g.SelectMany(x => x.DateList).ToArray(),
```

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

#### Workflow
如員工**申請Leave的時間**在離職通知期(Notice Period)內，條workflow可以唔同。

## My
`RECALC_713_MW_PAYROLLP_LEAVEP_OTP`
```c#
if ((new BLL.CodeSettings()).GetSystemParameterInfoSingle(Common.Constant.SystemParameter.RECALC_713_MW_PAYROLLP_LEAVEP_OTP.ToString())) {
#region
#endregion
}
```

LDAP:

```c#
        /*- v1.6.9 Sun 2016-04-28: LDAP functions Begin -*/
        #region LDAP related functions
        [WebMethod]
        public bool IsLDAPAvailable(){
            BLL.CodeSettings l_objCode = new BLL.CodeSettings();
            if (l_objCode.GetSystemParameterInfoSingle(Common.Constant.SystemParameter.LDAP_IS_AVAILABLE.ToString()) == "1")
                return true;
            else
                return false;
        }

        [WebMethod]
        public bool IsValidLDAPLogin(string p_strLoginID, string p_strPassword)
        {
            BLL.CodeSettings l_objCode = new BLL.CodeSettings();
            try
            {
                if (IsLDAPAvailable())
                {
                    using (var l_objLDAPContext = new PrincipalContext(ContextType.Domain, l_objCode.GetSystemParameterInfoSingle(Common.Constant.SystemParameter.LDAP_DOMAIN.ToString()), p_strLoginID, p_strPassword))
                    {
                        return l_objLDAPContext.ValidateCredentials(p_strLoginID, p_strPassword);
                    }
                }
                else
                    return false;
            }
            catch (Exception)
            {
                return false;
            }
        }

        [WebMethod]
        public int AuthenticateUserLDAP(string p_strLoginID, string p_strPassword)
        {
            if (IsValidLDAPLogin(p_strLoginID, p_strPassword))
            {
                using (DAL.LinqToDWHRMSDataContext p_DataContext = new DAL.LinqToDWHRMSDataContext(g_strConnection))
                {
                    p_DataContext.ObjectTrackingEnabled = false;
                    //return this.AuthenticateUserDetail(p_strLoginID, p_strPassword, p_DataContext);
                    return this.AuthenticateUserDetail(p_strLoginID, p_strPassword, p_DataContext, true);
                    /*
                    var p_UserRecord = from user in p_DataContext.t_Users
                                       where
                                        user.Username == p_strLoginID 
                                        && user.Status == (int)Common.Utility.GeneralStatus.ACTIVE
                                       select user;

                    if (p_UserRecord.Count() == 1)
                        return p_UserRecord.Single().ID;
                    else
                        return -1;
                    */
                }
            }
            else
            {
                return -1;
            }
        }
        #endregion
        /*- v1.6.9 Sun 2016-04-28: LDAP functions End -*/
        ```
