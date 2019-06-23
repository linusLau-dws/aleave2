import pandas as pd
import pandas_access as mdb
import pymssql
from os.path import expanduser
from os import remove
from datetime import date
from urllib.request import urlopen, URLError

attempts = 0

while attempts < 3:
	try:
		response = urlopen('http://static.data.gov.hk/td/routes-and-fares/ROUTE_GMB.mdb', timeout=10)
		content = response.read()
		with open(expanduser('~/Desktop/ROUTE_GMB.mdb'), 'wb') as f:
			f.write(content)
		break
	except URLError as e:
		if attempts == 2:
			exit(1)
		attempts += 1

df = mdb.read_table(expanduser('~/Desktop/ROUTE_GMB.mdb'), mdb.list_tables(expanduser('~/Desktop/ROUTE_GMB.mdb'))[0])
#df.drop_duplicates(subset=['ROUTE_ID', 'ROUTE_SEQ', 'STOP_SEQ'], keep='last', inplace=True)
#df.fillna(-1, inplace=True)
conn = pymssql.connect(server=r'192.168.9.75\sql2012', user='sa', password='sa', database='DWHRMS_DEMO')
last = len(df)-1
sql = 'DELETE FROM [t_MobMinibusFares]'
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
for i, r in df.iterrows():
	if i % 1000 == 0:
		sql = f'INSERT INTO [t_MobMinibusFares] ([Route ID], [District], [Route Name], [Source zh-hk], [Source zh-cn], [Source en-us], [Dest zh-hk], [Dest zh-cn], [Dest en-us], [Fare]) \n'
	else:
		sql += 'UNION ALL \n'
	sql += "SELECT {},'{}','{}',N'{}',N'{}','{}',N'{}',N'{}','{}',{} \n".format(r["ROUTE_ID"], r["DISTRICT"], r["ROUTE_NAMEE"], r["LOC_START_NAMEC"], r["LOC_START_NAMES"], r["LOC_START_NAMEE"].replace("'","''"), r["LOC_END_NAMEC"], r["LOC_END_NAMES"], r["LOC_END_NAMEE"].replace("'","''"), r["FULL_FARE"])
	if i % 1000 == 999 or i == last:
		cursor = conn.cursor()  
		cursor.execute(sql)
		conn.commit()
conn.close()
remove(expanduser('~/Desktop/ROUTE_GMB.mdb'))