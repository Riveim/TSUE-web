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

const homework = document.getElementById('homework-button');
const presentations = document.getElementById('presentations-button');
const notes = document.getElementById('notes-button');
const subjects = document.getElementById('subjects-button');
const schedule = document.getElementById('schedule-button');

const folderButtons = [homework, presentations, notes, subjects, schedule];

folderButtons.forEach((button) => {
    button.addEventListener('click', (e) => {
        e.stopPropagation();
        document.querySelectorAll('.data-section table').forEach((table) => {
            table.style.display = 'none';
        });

        folderButtons.forEach((btn) => {
            btn.classList.remove('active');
        });

        const tableId = button.id.replace('-button', '-table');
        const table = document.getElementById(tableId);

        if (table) {
            table.style.display = 'table';
            button.classList.add('active');
        }
    });
});

const addModal = document.getElementById('add-modal');
const addForm = document.getElementById('add-form');

const addButton = document.querySelector('.add-btn');
const cancelAdd = document.getElementById('cancel-add');

const addHomeworkType = document.getElementById('add-homework-type');
const addPresentationType = document.getElementById('add-presentation-type');
const addNotesType = document.getElementById('add-notes-type');

const subjectInput = document.getElementById('subject');
const descriptionInput = document.getElementById('description');
const topicInput = document.getElementById('topic');
const dueDateInput = document.getElementById('due-date');

const descriptionField = document.getElementById('description-field');
const topicField = document.getElementById('topic-field');
const dueDateField = document.getElementById('due-date-field');

let currentType = 'homework';

addButton.addEventListener('click', () => {
    addModal.showModal();
});

cancelAdd.addEventListener('click', () => {
    addModal.close();
});

function selectType(type) {

    currentType = type;

    addHomeworkType.classList.remove('active');
    addPresentationType.classList.remove('active');
    addNotesType.classList.remove('active');

    descriptionField.style.display = 'none';
    topicField.style.display = 'none';
    dueDateField.style.display = 'none';

    if (type === 'homework') {

        addHomeworkType.classList.add('active');

        descriptionField.style.display = 'block';
        dueDateField.style.display = 'block';

        descriptionInput.required = true;
        topicInput.required = false;
        dueDateInput.required = true;
    }

    if (type === 'presentation') {

        addPresentationType.classList.add('active');

        descriptionField.style.display = 'block';
        dueDateField.style.display = 'block';

        descriptionInput.required = true;
        topicInput.required = false;
        dueDateInput.required = true;
    }

    if (type === 'notes') {

        addNotesType.classList.add('active');

        topicField.style.display = 'block';

        descriptionInput.required = false;
        topicInput.required = true;
        dueDateInput.required = false;
    }
}

addHomeworkType.addEventListener('click', () => {
    selectType('homework');
});

addPresentationType.addEventListener('click', () => {
    selectType('presentation');
});

addNotesType.addEventListener('click', () => {
    selectType('notes');
});

addForm.addEventListener('submit', (e) => {

    e.preventDefault();

    const subject = subjectInput.value;
    const description = descriptionInput.value;
    const topic = topicInput.value;
    const dueDate = dueDateInput.value;

    if (currentType === 'homework') {

        const tbody = document.getElementById('homework-tbody');

        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${subject}</td>
            <td>${description}</td>
            <td>${dueDate}</td>
        `;

        tbody.appendChild(row);
    }

    if (currentType === 'presentation') {

        const tbody = document.getElementById('presentations-tbody');

        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${subject}</td>
            <td>${description}</td>
            <td>${dueDate}</td>
        `;

        tbody.appendChild(row);
    }

    if (currentType === 'notes') {

        const tbody = document.getElementById('notes-tbody');

        const row = document.createElement('tr');

        row.innerHTML = `
            <td>${subject}</td>
            <td>${topic}</td>
        `;

        tbody.appendChild(row);
    }

    addForm.reset();

    selectType('homework');

    addModal.close();
});