import pandas as pd
import pandas_access as mdb
import pymssql
from os.path import expanduser, exists
from os import remove, mkdir
from datetime import date
from urllib.request import urlopen, URLError
from getpass import getpass

attempts = 0

s = input('Enter server address [192.168.13.60]: ')
if s == '':
	s = '192.168.13.60'
d = input('Enter database name [DWHRMS_DEMO]: ')
if d == '':
	d = 'DWHRMS_DEMO'
u = input('Enter username: ') 
if u == '':
	u = 'sa'
p = getpass('Enter password: ')
if p == '':
	p = 'sa'

while attempts < 3:
	try:
		response = urlopen('http://static.data.gov.hk/td/routes-and-fares/FARE_BUS.mdb', timeout=10)
		content = response.read()
		with open(expanduser('~/Desktop/FARE_BUS.mdb'), 'wb') as f:
			f.write(content)
		break
	except URLError as e:
		if attempts == 2:
			exit(1)
		attempts += 1

df = mdb.read_table(expanduser('~/Desktop/FARE_BUS.mdb'), mdb.list_tables(expanduser('~/Desktop/FARE_BUS.mdb'))[0])
df.drop_duplicates(subset=['ROUTE_ID', 'ROUTE_SEQ', 'ON_SEQ', 'OFF_SEQ'], keep='last', inplace=True)
#df.fillna(-1, inplace=True)
conn = pymssql.connect(server='192.168.13.60', user='sa', password='sa', database='DWHRMS_DEMO')
last = len(df)-1
sql = 'DELETE FROM [t_MobBusFaresSection]'
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()

folder = expanduser('~/Desktop/fares')
if not exists(folder):
	mkdir(folder)

for i, r in df.iterrows():
	if i % 1000 == 0:
		sql = f'INSERT INTO [t_MobBusFaresSection] ([Route ID], [Route Seq], [On Seq], [Off Seq], [Fare]) \n'
	else:
		sql += 'UNION ALL \n'
	sql += "SELECT {},{},{},{},{} \n".format(r["ROUTE_ID"], r["ROUTE_SEQ"], r["ON_SEQ"], r["OFF_SEQ"], r["PRICE"])
	if i % 1000 == 999 or i == last:
		with open(f"{folder}/{date.today()}_fare_{i // 1000}.sql", 'a', encoding="utf-8") as f:
			f.write(sql)
		cursor = conn.cursor()  
		cursor.execute(sql)
		conn.commit()
conn.close()
with open(f"{folder}/install.bat", 'w', encoding="utf-8") as f:
	f.write(f'for %%G in (*.sql) do sqlcmd /S {s} /d {d} -U {u} -P "{p}" -i"%%G"\npause')

remove(expanduser('~/Desktop/FARE_BUS.mdb'))