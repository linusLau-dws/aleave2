import pandas as pd
import pymssql
from os.path import expanduser
from datetime import date

df = pd.read_csv('http://resource.data.one.gov.hk/mtr/data/mtr_lines_fares.csv')
conn = pymssql.connect(server='192.168.13.60', user='sa', password='sa', database='DWHRMS_DEMO')
sql = f'/******************************************************************************************************************************************************************************************/ \n-- v1.8.8 {date.today().strftime("%Y.%m.%d")} Update MTR fares \nDELETE FROM [t_MobMtrFares] \nINSERT INTO [t_MobMtrFares] ([Source ID], [Dest ID], [Adult Fare], [Student Fare], [Elderly Fare], [Disabled Fare]) \n'
for i, r in df.iterrows():
	if i != 0:
		sql += 'UNION ALL \n'
	sql += f"SELECT {int(r['SRC_STATION_ID'])},{int(r['DEST_STATION_ID'])},{r['OCT_ADT_FARE']},{r['OCT_STD_FARE']},{r['OCT_CON_ELDERLY_FARE']},{r['OCT_CON_PWD_FARE']} \n"
print(sql)
with open(expanduser(f"~/Desktop/{date.today()}.sql"), 'a') as f:
    f.write(sql)
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
conn.close()