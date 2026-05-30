from __future__ import annotations

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

OUT = Path(r"C:\Users\marya\AndroidStudioProjects\TaskPlanner\reports\diagrams_final")
OUT.mkdir(parents=True, exist_ok=True)


def get_font(size: int, bold: bool = False):
    candidates = [
        Path(r"C:\Windows\Fonts\timesbd.ttf" if bold else r"C:\Windows\Fonts\times.ttf"),
        Path(r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf"),
    ]
    for path in candidates:
        if path.exists():
            return ImageFont.truetype(str(path), size)
    return ImageFont.load_default()


FONT = get_font(28)
FONT_SMALL = get_font(24)
FONT_TINY = get_font(20)
FONT_BOLD = get_font(30, bold=True)
FONT_TITLE = get_font(28, bold=True)


def text_center(draw, box, text, font, fill=(45, 45, 45), line_gap=8):
    x1, y1, x2, y2 = box
    words = text.split()
    lines = []
    line = ""
    max_w = x2 - x1 - 30
    for word in words:
        test = (line + " " + word).strip()
        if draw.textbbox((0, 0), test, font=font)[2] <= max_w:
            line = test
        else:
            if line:
                lines.append(line)
            line = word
    if line:
        lines.append(line)
    total_h = sum(draw.textbbox((0, 0), ln, font=font)[3] for ln in lines) + line_gap * (len(lines) - 1)
    y = y1 + (y2 - y1 - total_h) / 2
    for ln in lines:
        bbox = draw.textbbox((0, 0), ln, font=font)
        draw.text((x1 + (x2 - x1 - (bbox[2] - bbox[0])) / 2, y), ln, font=font, fill=fill)
        y += (bbox[3] - bbox[1]) + line_gap


def arrow(draw, start, end, fill=(80, 95, 120), width=3, dashed=False):
    x1, y1 = start
    x2, y2 = end
    if dashed:
        steps = 24
        for i in range(steps):
            if i % 2 == 0:
                sx = x1 + (x2 - x1) * i / steps
                sy = y1 + (y2 - y1) * i / steps
                ex = x1 + (x2 - x1) * (i + 1) / steps
                ey = y1 + (y2 - y1) * (i + 1) / steps
                draw.line((sx, sy, ex, ey), fill=fill, width=width)
    else:
        draw.line((x1, y1, x2, y2), fill=fill, width=width)
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    size = 18
    pts = [
        (x2, y2),
        (x2 - size * math.cos(angle - 0.45), y2 - size * math.sin(angle - 0.45)),
        (x2 - size * math.cos(angle + 0.45), y2 - size * math.sin(angle + 0.45)),
    ]
    draw.polygon(pts, fill=fill)


def draw_actor(draw, x, y, label):
    stroke = (130, 105, 105)
    skin = (255, 239, 210)
    draw.ellipse((x - 25, y - 85, x + 25, y - 35), outline=stroke, width=3, fill=skin)
    draw.line((x, y - 35, x, y + 55), fill=stroke, width=4)
    draw.line((x - 55, y, x + 55, y), fill=stroke, width=4)
    draw.line((x, y + 55, x - 42, y + 120), fill=stroke, width=4)
    draw.line((x, y + 55, x + 42, y + 120), fill=stroke, width=4)
    bbox = draw.textbbox((0, 0), label, font=FONT_BOLD)
    draw.text((x - (bbox[2] - bbox[0]) / 2, y + 130), label, font=FONT_BOLD, fill=(75, 75, 75))


def oval(draw, box, text):
    shadow = (218, 218, 218)
    draw.ellipse((box[0] + 8, box[1] + 8, box[2] + 8, box[3] + 8), fill=shadow)
    draw.ellipse(box, outline=(145, 125, 125), width=4, fill=(225, 242, 255))
    text_center(draw, box, text, FONT_BOLD)


def use_case():
    img = Image.new("RGB", (1800, 1250), "white")
    d = ImageDraw.Draw(img)
    d.rectangle((1, 1, 1798, 1248), outline=(130, 130, 130), width=2)
    d.polygon([(0, 0), (315, 0), (315, 30), (280, 55), (0, 55)], outline=(130, 130, 130), fill=(250, 250, 250))
    d.text((20, 14), "uc TaskPlanner Use Case Model", font=FONT_TITLE, fill=(20, 20, 20))

    actor = (220, 525)
    draw_actor(d, *actor, "Пользователь")

    cases = {
        "Регистрация": (545, 130, 845, 250),
        "Авторизация": (555, 300, 865, 420),
        "Просмотр задач": (575, 500, 890, 620),
        "Создание задачи": (540, 705, 860, 825),
        "Переключение темы": (560, 940, 940, 1065),
        "Редактирование задачи": (1100, 150, 1480, 270),
        "Удаление задачи": (1130, 340, 1450, 460),
        "Поиск и история": (1130, 540, 1450, 660),
        "Смена статуса": (1130, 745, 1450, 865),
    }
    for box in cases.values():
        pass
    for name, box in cases.items():
        oval(d, box, name)

    for name in ["Регистрация", "Авторизация", "Просмотр задач", "Создание задачи", "Переключение темы"]:
        bx = cases[name]
        arrow(d, (actor[0] + 82, actor[1] - 15), (bx[0], (bx[1] + bx[3]) // 2), width=3)

    arrow(d, (890, 560), (1100, 210), dashed=True)
    d.text((935, 345), "«extend»", font=FONT_SMALL, fill=(70, 70, 70))
    arrow(d, (890, 560), (1130, 400), dashed=True)
    d.text((960, 455), "«extend»", font=FONT_SMALL, fill=(70, 70, 70))
    arrow(d, (890, 560), (1130, 600), dashed=True)
    d.text((960, 585), "«extend»", font=FONT_SMALL, fill=(70, 70, 70))
    arrow(d, (890, 560), (1130, 805), dashed=True)
    d.text((955, 700), "«include»", font=FONT_SMALL, fill=(70, 70, 70))
    arrow(d, (700, 705), (700, 620), dashed=True)
    d.text((720, 650), "«include»", font=FONT_SMALL, fill=(70, 70, 70))

    img.save(OUT / "use_case_taskplanner.png")


def class_box(draw, x, y, w, title, attrs, methods):
    row_h = 54
    h = row_h + max(70, 36 * len(attrs) + 20) + max(55, 36 * len(methods) + 20)
    draw.rectangle((x, y, x + w, y + h), outline=(40, 40, 40), width=3, fill=(246, 246, 246))
    draw.rectangle((x, y, x + w, y + row_h), outline=(40, 40, 40), width=3, fill=(244, 244, 244))
    draw.text((x + 12, y + 13), "▣", font=FONT_TINY, fill=(100, 115, 130))
    bbox = draw.textbbox((0, 0), title, font=FONT_BOLD)
    draw.text((x + (w - (bbox[2] - bbox[0])) / 2, y + 12), title, font=FONT_BOLD, fill=(20, 20, 20))
    ay = y + row_h
    ah = max(70, 36 * len(attrs) + 20)
    draw.line((x, ay + ah, x + w, ay + ah), fill=(40, 40, 40), width=3)
    ty = ay + 16
    for attr in attrs:
        draw.text((x + 18, ty), attr, font=FONT_SMALL, fill=(20, 20, 20))
        ty += 36
    ty = ay + ah + 16
    for method in methods:
        draw.text((x + 18, ty), method, font=FONT_SMALL, fill=(20, 20, 20))
        ty += 36
    return (x, y, x + w, y + h)


def connector(draw, a, b, diamond=False, hollow_arrow=False):
    x1, y1, x2, y2 = a
    u1, v1, u2, v2 = b
    p1 = (x2, (y1 + y2) // 2)
    p2 = (u1, (v1 + v2) // 2)
    mid = ((p1[0] + p2[0]) // 2, p1[1])
    draw.line((p1[0], p1[1], mid[0], mid[1], mid[0], p2[1], p2[0], p2[1]), fill=(40, 40, 40), width=3)
    if diamond:
        x, y = p1
        pts = [(x, y), (x + 12, y - 10), (x + 24, y), (x + 12, y + 10)]
        draw.polygon(pts, outline=(40, 40, 40), fill="white")
    if hollow_arrow:
        x, y = p2
        pts = [(x, y), (x - 20, y - 12), (x - 20, y + 12)]
        draw.polygon(pts, outline=(40, 40, 40), fill="white")


def class_diagram():
    img = Image.new("RGB", (2200, 1550), (238, 238, 238))
    d = ImageDraw.Draw(img)
    boxes = {}
    specs = {
        "MainActivity": (70, 90, 430, "MainActivity", ["preferencesManager", "isDarkThemeEnabled"], ["onCreate(): void"]),
        "AppNavigation": (700, 70, 480, "AppNavigation", ["navController", "startDestination"], ["navigate(): void"]),
        "AuthViewModel": (1460, 80, 510, "AuthViewModel", ["state: AuthState", "authApi: AuthApi"], ["authenticate(): void", "toggleAuthMode(): void"]),
        "TasksViewModel": (1450, 430, 550, "TasksViewModel", ["state: TasksState", "repository: TaskRepository"], ["loadTasks(): void", "performSearch(): void", "setDarkThemeEnabled(): void"]),
        "TaskRepository": (720, 460, 500, "TaskRepository", ["interface"], ["getTasks(): List<Task>", "createTask(): void", "updateTask(): void", "deleteTask(): void"]),
        "TaskRepositoryImpl": (730, 900, 500, "TaskRepositoryImpl", ["api: TasksApi"], ["getTasks(): List<Task>", "updateTask(): void"]),
        "Apis": (80, 920, 520, "AuthApi / TasksApi", ["BASE_URL: string", "token: string"], ["login(): string", "getTasks(): List<TaskDto>"]),
        "Prefs": (1490, 910, 520, "TokenManager / DataStore", ["jwt_token: string", "darkTheme: bool", "searchHistory: Set"], ["saveToken(): void", "saveSearchHistory(): void"]),
        "Task": (820, 1250, 430, "Task", ["id: int", "title: string", "description: string", "isCompleted: bool"], []),
    }
    for key, spec in specs.items():
        boxes[key] = class_box(d, *spec)

    connector(d, boxes["MainActivity"], boxes["AppNavigation"], hollow_arrow=True)
    connector(d, boxes["AppNavigation"], boxes["AuthViewModel"], hollow_arrow=True)
    connector(d, boxes["AppNavigation"], boxes["TasksViewModel"], hollow_arrow=True)
    connector(d, boxes["TasksViewModel"], boxes["TaskRepository"], hollow_arrow=True)
    connector(d, boxes["TaskRepository"], boxes["TaskRepositoryImpl"], hollow_arrow=True)
    connector(d, boxes["Apis"], boxes["TaskRepositoryImpl"], hollow_arrow=True)
    connector(d, boxes["TasksViewModel"], boxes["Prefs"], diamond=True)
    connector(d, boxes["TaskRepositoryImpl"], boxes["Task"], diamond=True)

    # Redraw classes over connector interiors so association lines do not cross text.
    for spec in specs.values():
        class_box(d, *spec)

    img.save(OUT / "class_diagram_taskplanner.png")


def idef0():
    img = Image.new("RGB", (1600, 950), "white")
    d = ImageDraw.Draw(img)
    box = (560, 310, 1040, 600)
    d.rectangle(box, outline="black", width=4, fill=(151, 216, 243))
    text_center(d, box, "Планирование и управление задачами", FONT, fill=(20, 20, 20))
    d.text((1000, 565), "0", font=FONT_SMALL, fill="black")
    d.text((1015, 620), "A0", font=FONT_SMALL, fill="black")

    arrow(d, (30, 365), (560, 365), fill="black", width=2)
    d.text((60, 330), "Учетные данные", font=FONT_SMALL, fill="black")
    arrow(d, (30, 510), (560, 510), fill="black", width=2)
    d.text((60, 475), "Параметры задачи", font=FONT_SMALL, fill="black")

    arrow(d, (650, 30), (650, 310), fill="black", width=2)
    d.text((500, 35), "Правила авторизации", font=FONT_SMALL, fill="black")
    arrow(d, (910, 30), (910, 310), fill="black", width=2)
    d.text((930, 35), "Требования методички", font=FONT_SMALL, fill="black")

    arrow(d, (800, 900), (800, 600), fill="black", width=2)
    d.text((820, 850), "Android, Ktor, PostgreSQL", font=FONT_SMALL, fill="black")

    arrow(d, (1040, 455), (1570, 455), fill="black", width=2)
    d.text((1070, 375), "Список задач, результаты поиска,\nобновленный статус", font=FONT_SMALL, fill="black")

    img.save(OUT / "idef0_taskplanner.png")


if __name__ == "__main__":
    use_case()
    class_diagram()
    idef0()
    print(OUT)
