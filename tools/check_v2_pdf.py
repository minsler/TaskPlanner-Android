from pathlib import Path
import re
import sys

from pypdf import PdfReader

sys.stdout.reconfigure(encoding="utf-8")
path = Path(r"C:\Users\marya\Documents\х\курсач\Курсовая работа_TaskPlanner_сокращенная_v2.pdf")
reader = PdfReader(str(path))
texts = [page.extract_text() or "" for page in reader.pages]
app_pages = [
    index
    for index, text in enumerate(texts, start=1)
    if re.search(r"ПРИЛОЖЕНИЯ\s*\nПриложение А", text.upper())
]
print("pages", len(reader.pages))
print("appendix_start", app_pages[:5])
print("first_page")
print(texts[0][:3000])
if app_pages:
    print("appendix_page_text")
    print(texts[app_pages[0] - 1][:800])
