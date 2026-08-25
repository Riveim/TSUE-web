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

navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();

        // подсветка активного пункта меню
        navLinks.forEach(l => l.classList.remove('active-tab'));
        link.classList.add('active-tab');

        const endpoint = link.dataset.endpoint;
        loadSection(endpoint, link.textContent.trim());
    });
});

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

    const itemsHtml = items.map(item => `
        <div class="data-item">
            <div class="data-item-title">${escapeHtml(item.title ?? item.name ?? 'Без названия')}</div>
            ${item.description ? `<div class="data-item-desc">${escapeHtml(item.description)}</div>` : ''}
        </div>
    `).join('');

    dataPanel.innerHTML = `
        <div class="panel-title">${title}</div>
        ${itemsHtml}
    `;
}

// защита от XSS при выводе текста с сервера
function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
