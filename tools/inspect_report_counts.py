from collections import Counter
from pathlib import Path

from docx import Document

path = Path(r"C:\Users\marya\Documents\х\курсач\Курсовая работа_TaskPlanner_без титульного листа.docx")
doc = Document(path)
prefixes = (
    "1.1.", "1.2.", "1.3.",
    "2.1.", "2.2.", "2.3.1.", "2.3.2.",
    "3.1.", "3.2.1.", "3.2.2.", "3.2.3.", "3.3.",
)
current = None
counts = Counter()
for paragraph in doc.paragraphs:
    text = paragraph.text.strip()
    style = paragraph.style.name
    if style.startswith("Heading"):
        if text.upper() in ("ВВЕДЕНИЕ", "ЗАКЛЮЧЕНИЕ"):
            current = text.upper()
        for prefix in prefixes:
            if text.startswith(prefix):
                current = prefix
        if text.upper() == "ПРИЛОЖЕНИЯ":
            break
    elif (
        current
        and paragraph.style.name == "Normal"
        and text
        and not text.startswith("Таблица")
        and not text.startswith("Рисунок")
    ):
        counts[current] += 1
print(counts)
