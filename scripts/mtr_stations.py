import pandas as pd
import pymssql
from os.path import expanduser
from datetime import date

df = pd.read_csv('http://resource.data.one.gov.hk/mtr/data/mtr_lines_and_stations.csv')
df.dropna(inplace=True)
df.drop_duplicates(subset=['Line Code', 'Station ID'], keep='first', inplace=True)
conn = pymssql.connect(server='192.168.13.60', user='sa', password='sa', database='DWHRMS_DEMO')
sql = f'/******************************************************************************************************************************************************************************************/ \n-- v1.8.8 {date.today().strftime("%Y.%m.%d")} Update MTR stations \nDELETE FROM [t_MobMtrStations] \nINSERT INTO [t_MobMtrStations] ([Line Code], [Station ID], [Chinese Name], [English Name]) \n'
for i, r in df.iterrows():
	if i != 0:
		sql += 'UNION ALL \n'
	sql += "SELECT '{}', {}, N'{}', '{}' \n".format(r['Line Code'], int(r['Station ID']), r['Chinese Name'], r['English Name'])
print(sql)
with open(expanduser(f"~/Desktop/{date.today()}.sql"), 'a', encoding="utf-8") as f:
    f.write(sql)
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
conn.close()