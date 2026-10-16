from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
count=0
for file in (root/'app/src').rglob('*.xml'):
    ET.parse(file);count+=1
text=(root/'app/src/main/java/com/example/nexventory/MainActivity.java').read_text(encoding='utf-8')
for field in ['categoryInput','locationInput','importCsvButton']:
    assert field in text
    assert field in (root/'app/src/main/res/layout/activity_main.xml').read_text(encoding='utf-8')
assert 'allowMainThreadQueries' not in (root/'app/src/main/java/com/example/nexventory/NexventoryDatabase.java').read_text()
assert 'fallbackToDestructiveMigration' not in (root/'app/src/main/java/com/example/nexventory/NexventoryDatabase.java').read_text()
print(f'PASS: {count} XML files parsed; controls and production database configuration checked.')
