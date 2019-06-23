import xml.etree.cElementTree as et
import pandas as pd
from os.path import expanduser
import pymssql
from hanziconv import HanziConv
from re import sub

conn = pymssql.connect(server=r'192.168.13.60', user='sa', password='sa', database='DWIHR_RESOURCES')
sql = 'DELETE FROM [t_Language_Mobile]'
cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()

sql = f'INSERT INTO [t_Language_Mobile] ([Name], [en-us_Android], [en-us_iOS], [zh-hk_Android], [zh-hk_iOS], [zh-cn_Android], [zh-cn_iOS], [Set], [ModifiedDate], [CreateDate]) \n'
xml = et.parse(expanduser(r'~\Desktop\New folder (3)\LeaveApp\app\src\main\res\values\strings.xml'))

df = pd.DataFrame(columns=['Name','en-us_Android','zh-cn_Android'])
df2 = pd.DataFrame(columns=['Name','zh-hk_Android'])

for node in xml.getroot():
	if node.text.isspace():
		continue
	df = df.append({'Name': node.attrib.get("name"), 'en-us_Android': node.text}, ignore_index=True)

xml = et.parse(expanduser(r'~\Desktop\New folder (3)\LeaveApp\app\src\main\res\values-zh\strings.xml'))
for node in xml.getroot():
	if node.text.isspace():
		continue
	df2 = df2.append({'Name': node.attrib.get("name"), 'zh-hk_Android': node.text}, ignore_index=True)

df = df.merge(df2, how='outer')

androidSyntax = "%[0-9]\\$s"
androidSyntax2 = "{[a-z_-]*} "
androidSyntax3 = "%[0-9]\\$\\.?[0-9]?d"
androidSyntax4 = "%[0-9]\\$\\.?[0-9]?f"
iOSSyntax = "%@"
iOSSyntax2 = ""
iOSSyntax3 = "%d"
iOSSyntax4 = "%f"

for i, r in df.iterrows():
	if (len(r['en-us_Android']) > 1000):
		continue
	r['zh-cn_Android'] = HanziConv.toSimplified(r['zh-hk_Android'])
	if i != 0:
		sql += 'UNION ALL \n'
	sql += f'SELECT N\'{r["Name"]}\', N\'{r["en-us_Android"]}\', N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["en-us_Android"]))))}\', N\'{r["zh-hk_Android"]}\', N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["zh-hk_Android"]))))}\', N\'{r["zh-cn_Android"]}\' \n, N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["zh-cn_Android"]))))}\', \'LeaveManager\', getdate(), getdate()'

cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()

# --------------------------------------

sql = f'INSERT INTO [t_Language_Mobile] ([Name], [en-us_Android], [en-us_iOS], [zh-hk_Android], [zh-hk_iOS], [zh-cn_Android], [zh-cn_iOS], [Set], [ModifiedDate], [CreateDate]) \n'
xml = et.parse(expanduser(r'~\Desktop\New folder (3)\iAttendance\app\src\main\res\values\strings.xml'))

df = pd.DataFrame(columns=['Name','en-us_Android','zh-cn_Android'])
df2 = pd.DataFrame(columns=['Name','zh-hk_Android'])

for node in xml.getroot():
	if node.text.isspace():
		continue
	df = df.append({'Name': node.attrib.get("name"), 'en-us_Android': node.text}, ignore_index=True)

xml = et.parse(expanduser(r'~\Desktop\New folder (3)\iAttendance\app\src\main\res\values-zh\strings.xml'))
for node in xml.getroot():
	if node.text.isspace():
		continue
	df2 = df2.append({'Name': node.attrib.get("name"), 'zh-hk_Android': node.text}, ignore_index=True)

df = df.merge(df2, how='outer')

androidSyntax = "%[0-9]\\$s"
androidSyntax2 = "{[a-z_-]*} "
androidSyntax3 = "%[0-9]\\$\\.?[0-9]?d"
androidSyntax4 = "%[0-9]\\$\\.?[0-9]?f"
iOSSyntax = "%@"
iOSSyntax2 = ""
iOSSyntax3 = "%d"
iOSSyntax4 = "%f"

for i, r in df.iterrows():
	if (len(r['en-us_Android']) > 1000):
		continue
	r['zh-cn_Android'] = HanziConv.toSimplified(r['zh-hk_Android'])
	if i != 0:
		sql += 'UNION ALL \n'
	sql += f'SELECT N\'{r["Name"]}\', N\'{r["en-us_Android"]}\', N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["en-us_Android"]))))}\', N\'{r["zh-hk_Android"]}\', N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["zh-hk_Android"]))))}\', N\'{r["zh-cn_Android"]}\' \n, N\'{sub(androidSyntax4, iOSSyntax4, sub(androidSyntax3, iOSSyntax3, sub(androidSyntax2, iOSSyntax2, sub(androidSyntax,iOSSyntax,r["zh-cn_Android"]))))}\', \'iAttendance\', getdate(), getdate()'

cursor = conn.cursor()  
cursor.execute(sql)
conn.commit()
conn.close()