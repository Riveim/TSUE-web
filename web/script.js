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