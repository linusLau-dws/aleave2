import pandas as pd
import pymssql
from os.path import expanduser
from datetime import date

df = pd.read_csv('https://api.data.gov.hk/v1/filter?q=%7B%22resource%22%3A%22http%3A%2F%2Fstatic.data.gov.hk%2Ftd%2Froutes-and-fares%2FROUTE_BUS.mdb%22%2C%22section%22%3A1%2C%22format%22%3A%22csv%22%7D')
#df.fillna(-1, inplace=True)
df.drop_duplicates(subset=['ROUTE_ID', 'COMPANY_CODE', 'LOC_START_NAMEE', 'LOC_END_NAMEE'], keep='first', inplace=True)
conn = pymssql.connect(server='192.168.13.60', user='sa', password='sa', database='DWHRMS_DEMO')
sql = f'/******************************************************************************************************************************************************************************************/ \n-- v1.8.8 {date.today().strftime("%Y.%m.%d")} Update Bus fares \nDELETE FROM [t_MobBusFares] \nINSERT INTO [t_MobBusFares] ([Route ID],[Bus Company],[Route Name],[Source zh-hk],[Source zh-cn],[Source en-us],[Dest zh-hk],[Dest zh-cn],[Dest en-us],[Full Fare]) \n'
for i, r in df.iterrows():
	if i != 0:
		sql += 'UNION ALL \n'
	sql += "SELECT {}, '{}','{}',N'{}',N'{}','{}',N'{}',N'{}','{}',{} \n".format(r["ROUTE_ID"], r["COMPANY_CODE"], r["ROUTE_NAMEE"], r["LOC_START_NAMEC"], r["LOC_START_NAMES"], r["LOC_START_NAMEE"].replace("'", "''"), r["LOC_END_NAMEC"], r["LOC_END_NAMES"], r["LOC_END_NAMEE"].replace("'", "''"), r["FULL_FARE"][1:])
print(sql)
with open(expanduser(f"~/Desktop/{date.today()}.sql"), 'a', encoding="utf-8") as f:
    f.write(sql)
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
conn.close()