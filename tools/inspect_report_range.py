from pathlib import Path
from docx import Document

doc = Document(Path(r"C:\Users\marya\Documents\х\курсач\Курсовая работа_TaskPlanner_без титульного листа.docx"))
for i in range(160, 172):
    p = doc.paragraphs[i]
    print(i, p.style.name, repr(p.text), "drawings", len(p._element.xpath(".//w:drawing")))
