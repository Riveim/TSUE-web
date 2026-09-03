// ==== Меню-папка (как было) ====

const folderMenu = document.getElementById('folder-menu');

folderMenu.addEventListener('click', (e) => {
    folderMenu.classList.toggle('active');
    e.stopPropagation();
});

document.addEventListener('click', (e) => {
    if (!folderMenu.contains(e.target)) {
        folderMenu.classList.remove('active');
    }
});

// ==== Подключение к бэкенду (Spring Boot) ====

// Адрес вашего бэкенда. Поменяйте на реальный при деплое.
const API_BASE = 'http://localhost:8080/api';

const dataPanel = document.getElementById('data-panel');
const navLinks = document.querySelectorAll('.web-navigation a[data-endpoint]');
const uploadForm = document.getElementById('upload-form');
const uploadTitle = document.getElementById('upload-title');
const uploadDescription = document.getElementById('upload-description');
const uploadFile = document.getElementById('upload-file');

const schedulePanel = document.getElementById('schedule-panel');
const scheduleContent = document.getElementById('schedule-content');
const classSelect = document.getElementById('class-select');

let currentEndpoint = null;
let currentTitle = null;

navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();

        // подсветка активного пункта меню
        navLinks.forEach(l => l.classList.remove('active-tab'));
        link.classList.add('active-tab');

        currentEndpoint = link.dataset.endpoint;
        currentTitle = link.textContent.trim();

        if (currentEndpoint === 'schedule') {
            // расписание — отдельная логика (реальный парсер EduPage)
            dataPanel.classList.remove('visible');
            uploadForm.style.display = 'none';
            schedulePanel.style.display = 'flex';
            loadClassList();
        } else {
            schedulePanel.style.display = 'none';
            uploadForm.style.display = 'flex';
            loadSection(currentEndpoint, currentTitle);
        }
    });
});

// ==== Обычные разделы (Предметы, Домашняя работа и т.д.) ====

async function loadSection(endpoint, title) {
    showLoading(title);

    try {
        const res = await fetch(`${API_BASE}/${endpoint}`);

        if (!res.ok) {
            throw new Error(`Сервер ответил ${res.status}`);
        }

        const items = await res.json();
        renderItems(title, items);

    } catch (err) {
        showError(title, err.message);
    }
}

function showLoading(title) {
    dataPanel.classList.add('visible');
    dataPanel.innerHTML = `
        <div class="panel-title">${title}</div>
        <div class="panel-status">Загрузка...</div>
    `;
}

function showError(title, message) {
    dataPanel.innerHTML = `
        <div class="panel-title">${title}</div>
        <div class="panel-status error">Не удалось загрузить данные: ${message}</div>
    `;
}

function renderItems(title, items) {
    if (!items || items.length === 0) {
        dataPanel.innerHTML = `
            <div class="panel-title">${title}</div>
            <div class="panel-status">Пока пусто</div>
        `;
        return;
    }

    const itemsHtml = items.map(item => {
        const description = item.description
            ? `<div class="data-item-desc">${escapeHtml(item.description)}</div>`
            : '';

        // если у элемента есть прикреплённый файл — показываем ссылку на скачивание
        const download = item.downloadUrl
            ? `<a class="data-item-download" href="${API_BASE.replace('/api', '')}${item.downloadUrl}" target="_blank">
                   <i class="fa-solid fa-download"></i> Скачать файл
               </a>`
            : '';

        return `
            <div class="data-item">
                <div class="data-item-title">${escapeHtml(item.title ?? item.name ?? 'Без названия')}</div>
                ${description}
                ${download}
            </div>
        `;
    }).join('');

    dataPanel.innerHTML = `
        <div class="panel-title">${title}</div>
        ${itemsHtml}
    `;
}

// ==== Загрузка файла (для обычных разделов) ====

uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!currentEndpoint) {
        alert('Сначала выберите раздел (например, «Предметы»)');
        return;
    }

    const formData = new FormData();
    formData.append('title', uploadTitle.value);
    formData.append('description', uploadDescription.value);
    formData.append('file', uploadFile.files[0]);

    try {
        const res = await fetch(`${API_BASE}/${currentEndpoint}/upload`, {
            method: 'POST',
            body: formData
            // Content-Type НЕ указываем вручную — браузер сам поставит
            // multipart/form-data с правильным boundary
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || `Сервер ответил ${res.status}`);
        }

        // очищаем форму и обновляем список
        uploadForm.reset();
        loadSection(currentEndpoint, currentTitle);

    } catch (err) {
        alert('Не удалось загрузить файл: ' + err.message);
    }
});

// ==== Расписание (реальные данные EduPage через /api/timetable) ====

async function loadClassList() {
    scheduleContent.innerHTML = `<div class="panel-status">Загрузка списка групп...</div>`;

    try {
        const res = await fetch(`${API_BASE}/timetable/classes`);
        if (!res.ok) {
            throw new Error(`Сервер ответил ${res.status}`);
        }

        const classes = await res.json();

        classSelect.innerHTML = '<option value="">Выберите группу...</option>' +
            classes.map(name => `<option value="${escapeHtml(name)}">${escapeHtml(name)}</option>`).join('');

        scheduleContent.innerHTML = `<div class="panel-status">Выберите группу выше, чтобы увидеть расписание</div>`;

    } catch (err) {
        scheduleContent.innerHTML = `
            <div class="panel-status error">Не удалось загрузить список групп: ${escapeHtml(err.message)}</div>
        `;
    }
}

classSelect.addEventListener('change', () => {
    const className = classSelect.value;
    if (className) {
        loadTimetable(className);
    } else {
        scheduleContent.innerHTML = `<div class="panel-status">Выберите группу выше, чтобы увидеть расписание</div>`;
    }
});

async function loadTimetable(className) {
    scheduleContent.innerHTML = `<div class="panel-status">Загрузка расписания...</div>`;

    try {
        const res = await fetch(`${API_BASE}/timetable/${encodeURIComponent(className)}`);

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.error || `Сервер ответил ${res.status}`);
        }

        const entries = await res.json();
        renderTimetable(entries);

    } catch (err) {
        scheduleContent.innerHTML = `
            <div class="panel-status error">Не удалось загрузить расписание: ${escapeHtml(err.message)}</div>
        `;
    }
}

const DAY_ORDER = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'];

function renderTimetable(entries) {
    if (!entries || entries.length === 0) {
        scheduleContent.innerHTML = `<div class="panel-status">У этой группы пока нет занятий</div>`;
        return;
    }

    // группируем записи по дням недели в правильном порядке
    const byDay = {};
    entries.forEach(entry => {
        if (!byDay[entry.day]) {
            byDay[entry.day] = [];
        }
        byDay[entry.day].push(entry);
    });

    const daysHtml = DAY_ORDER
        .filter(day => byDay[day])
        .map(day => {
            const lessonsHtml = byDay[day]
                .sort((a, b) => a.period - b.period)
                .map(entry => `
                    <div class="schedule-lesson">
                        <div class="schedule-lesson-period">${entry.period} пара<br>${escapeHtml(entry.time)}</div>
                        <div class="schedule-lesson-info">
                            <div class="subject">${escapeHtml(entry.subject)}</div>
                            <div class="meta">${escapeHtml(entry.teachers)} · ${escapeHtml(entry.room)}</div>
                        </div>
                    </div>
                `).join('');

            return `
                <div class="schedule-day">
                    <div class="schedule-day-title">${escapeHtml(day)}</div>
                    ${lessonsHtml}
                </div>
            `;
        }).join('');

    scheduleContent.innerHTML = daysHtml;
}

// защита от XSS при выводе текста с сервера
function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
