function showConfirm(message, title = 'Xác nhận hành động') {
    console.log('showConfirm called:', message);
    return new Promise((resolve) => {
        const modalEl = document.getElementById('confirmModal');
        if (!modalEl) {
            console.error('Confirm Modal element not found! #confirmModal is missing.');
            resolve(confirm(message));
            return;
        }

        if (typeof bootstrap === 'undefined') {
            console.error('Bootstrap is not defined! Modal cannot be shown.');
            resolve(confirm(message));
            return;
        }

        const label = document.getElementById('confirmModalLabel');
        const msg = document.getElementById('confirmModalMessage');
        const confirmBtn = document.getElementById('confirmModalBtn');

        if (label) label.innerText = title;
        if (msg) msg.innerText = message;

        const modal = new bootstrap.Modal(modalEl);
        let confirmed = false;

        const onConfirm = () => {
            confirmed = true;
            modal.hide();
        };

        const onHide = () => {
            console.log('Modal hidden, resolved with:', confirmed);
            resolve(confirmed);
            confirmBtn.removeEventListener('click', onConfirm);
            modalEl.removeEventListener('hidden.bs.modal', onHide);
            // Cleanup modal instance from DOM if possible
            const backdrop = document.querySelector('.modal-backdrop');
            if (backdrop) backdrop.remove();
        };

        confirmBtn.addEventListener('click', onConfirm);
        modalEl.addEventListener('hidden.bs.modal', onHide);

        modal.show();
    });
}

/**
 * Hiển thị một thông báo Bootstrap Alert động
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo: 'success', 'danger', 'warning', 'info'
 */
function showAlert(message, type = 'success') {
    const container = document.getElementById('alertContainer');
    if (!container) {
        console.warn('Alert container not found, falling back to alert()');
        alert(message);
        return;
    }

    const alertDiv = document.createElement('div');
    // Map 'error' to 'danger' for Bootstrap compatibility
    const bsType = type === 'error' ? 'danger' : type;
    alertDiv.className = `alert alert-${bsType} alert-dismissible fade show shadow-sm border-0 rounded-xl mb-4`;
    alertDiv.role = 'alert';

    let iconClass = 'fas fa-info-circle';
    if (bsType === 'success') iconClass = 'fas fa-check-circle';
    else if (bsType === 'danger') iconClass = 'fas fa-exclamation-triangle';
    else if (bsType === 'warning') iconClass = 'fas fa-exclamation-circle';

    alertDiv.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="${iconClass} me-2 fs-5"></i>
            <span class="fw-bold">${message}</span>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    container.prepend(alertDiv);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        if (alertDiv && alertDiv.parentNode) {
            const bsAlert = new bootstrap.Alert(alertDiv);
            bsAlert.close();
        }
    }, 5000);
}
