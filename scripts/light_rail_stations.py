import pandas as pd
import pymssql
from os.path import expanduser
from datetime import date

df = pd.read_csv('http://resource.data.one.gov.hk/mtr/data/light_rail_routes_and_stops.csv')
df.dropna(inplace=True)
df.drop_duplicates(subset=['Line Code', 'Stop ID'], keep='first', inplace=True)
conn = pymssql.connect(server='192.168.13.60', user='sa', password='sa', database='DWHRMS_DEMO')
sql = f'/******************************************************************************************************************************************************************************************/ \n-- v1.8.8 {date.today().strftime("%Y.%m.%d")} Update MTR stations \nDELETE FROM [t_MobLightRailStations] \nINSERT INTO [t_MobLightRailStations] ([Line Code], [Station ID], [Chinese Name], [English Name]) \n'
for i, r in df.iterrows():
	if i != 0:
		sql += 'UNION ALL \n'
	sql += "SELECT '{}', {}, N'{}', '{}' \n".format(r['Line Code'], int(r['Stop ID']), r['Chinese Name'], r['English Name'])
print(sql)
with open(expanduser(f"~/Desktop/{date.today()}.sql"), 'a', encoding="utf-8") as f:
    f.write(sql)
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
conn.close()