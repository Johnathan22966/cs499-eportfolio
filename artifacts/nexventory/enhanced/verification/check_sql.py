"""Exercise SQL extracted from DatabaseHelper in desktop SQLite, not Android."""
from pathlib import Path
import re
import sqlite3
import xml.etree.ElementTree as ET

root=Path(__file__).resolve().parents[1]
source=(root/'app/src/main/java/com/example/nexventory/DatabaseHelper.java').read_text()
def sql_expression(expr):
    # All execSQL expressions here are string literals and two constant table names.
    expr=expr.replace('TABLE_USERS','"users"').replace('TABLE_INVENTORY','"inventory"')
    return ''.join(re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"',expr))
statements=[sql_expression(x) for x in re.findall(r'db\.execSQL\((.*?)\);',source,re.S)]
create_users,create_inventory,upgrade=statements
db=sqlite3.connect(':memory:')
db.execute(create_users)
db.execute('CREATE TABLE inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, sku TEXT UNIQUE NOT NULL, quantity INTEGER NOT NULL CHECK(quantity >= 0))')
db.execute("INSERT INTO users(username,password) VALUES ('migration-test','unchanged-hash')")
db.execute("INSERT INTO inventory(name,sku,quantity) VALUES ('Existing cable','OLD',5)")
db.execute(upgrade)
assert db.execute('SELECT id,name,sku,quantity,minimum_stock FROM inventory').fetchone()==(1,'Existing cable','OLD',5,5)
assert db.execute('SELECT password FROM users').fetchone()==('unchanged-hash',)
for connection in [db,sqlite3.connect(':memory:')]:
    if connection is not db: connection.execute(create_inventory)
    for value in [-1,None]:
        try:
            connection.execute('INSERT INTO inventory(name,sku,quantity,minimum_stock) VALUES (?,?,?,?)',('Invalid',str(value),1,value))
        except sqlite3.IntegrityError: pass
        else: raise AssertionError('Minimum constraint missing')
    connection.execute('INSERT INTO inventory(name,sku,quantity,minimum_stock) VALUES (?,?,?,?)',('Adapter','A',0,4))
    connection.execute('INSERT INTO inventory(name,sku,quantity,minimum_stock) VALUES (?,?,?,?)',('Patch Cable','C',3,10))
    connection.execute('INSERT INTO inventory(name,sku,quantity,minimum_stock) VALUES (?,?,?,?)',('No target','N',0,0))
    connection.execute('INSERT INTO inventory(name,sku,quantity,minimum_stock) VALUES (?,?,?,?)',('At target','T',5,5))
    assert connection.execute('SELECT COUNT(*) FROM inventory WHERE quantity < minimum_stock').fetchone()[0]==2
    assert connection.execute('SELECT COUNT(*) FROM inventory WHERE quantity > 0 AND quantity < minimum_stock').fetchone()[0]==1
    assert connection.execute('SELECT COUNT(*) FROM inventory WHERE quantity = 0').fetchone()[0]==2
    connection.execute('UPDATE inventory SET minimum_stock=0 WHERE sku="C"')
    assert connection.execute('SELECT COUNT(*) FROM inventory WHERE quantity < minimum_stock').fetchone()[0]==1
    connection.close()
count=0
for xml in (root/'app/src').rglob('*.xml'):
    ET.parse(xml); count+=1
print(f'PASS: desktop SQLite {sqlite3.sqlite_version} checked exact CREATE/ALTER statements from source, v2 preservation, fresh schema, constraints, counts and target edits; {count} XML resources parsed.')
print('Android SQLiteOpenHelper and emulator workflows require the separate instrumentation/manual checks.')
