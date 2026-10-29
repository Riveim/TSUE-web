import requests
import json
import re
from datetime import date, timedelta

BASE_URL = "https://tsue.edupage.org"

session = requests.Session()

# Получаем публичную страницу и сессионные cookies.
page = session.get(
    f"{BASE_URL}/timetable/",
    timeout=20
)
page.raise_for_status()

# Извлекаем gsechash из HTML.
match = re.search(
    r'ASC\.gsechash\s*=\s*"([^"]+)"',
    page.text
)

if not match:
    raise RuntimeError("Не удалось найти ASC.gsechash")

gsh = match.group(1)

# Период на ближайшую неделю.
today = date.today()
monday = today - timedelta(days=today.weekday())
sunday = monday + timedelta(days=6)

url = (
    f"{BASE_URL}/timetable/server/"
    "currenttt.js?__func=curentttGetData"
)

# Пока проверяем запрос на условном идентификаторе.
# Реальный ID группы нужно получить из данных сайта.
payload = {
    "__args": [
        None,
        {
            "year": 2026,
            "datefrom": monday.isoformat(),
            "dateto": sunday.isoformat(),
            "table": "classes",
            "id": "REPLACE_WITH_CLASS_ID",
            "showColors": True,
            "showIgroupsInClasses": True,
            "showOrig": True,
            "log_module": "CurrentTTView"
        }
    ],
    "__gsh": gsh
}

print("gsh:", gsh)
print("Период:", monday, "—", sunday)

# Запрос намеренно не выполняется, пока не установлен ID группы.
print("Сначала найди настоящий ID группы в справочнике EduPage.")