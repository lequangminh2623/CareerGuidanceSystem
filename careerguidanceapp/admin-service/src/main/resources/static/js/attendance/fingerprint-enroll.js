let enrollModalInstance;
let enrollEventSource;
let isEnrollSuccessOrError = false;
let currentClassroomId = null;

document.addEventListener("DOMContentLoaded", function () {
    // Khởi tạo Modal Bootstrap 5
    const modalElement = document.getElementById('enrollModal');
    enrollModalInstance = new bootstrap.Modal(modalElement);

    // Đảm bảo đóng EventSource và gửi lệnh Cancel nếu đóng thủ công
    modalElement.addEventListener('hidden.bs.modal', function () {
        closeEnrollEventSource();
        if (!isEnrollSuccessOrError && currentClassroomId) {
            cancelEnroll(currentClassroomId);
        }
    });
});

function handleEnroll(btn) {
    const classroomId = btn.getAttribute('data-classroom-id');
    const studentId = btn.getAttribute('data-student-id');
    const studentName = btn.getAttribute('data-student-name');

    startEnroll(classroomId, studentId, studentName);
}

function startEnroll(classroomId, studentId, studentName) {
    // Reset flags & vars
    isEnrollSuccessOrError = false;
    currentClassroomId = classroomId;

    // Reset trạng thái modal về trạng thái "đang chờ" (tạo lại DOM bên trong)
    setModalWaiting();

    // Cập nhật tên học sinh lên Modal (sau setModalWaiting vì innerHTML đã được reset)
    document.getElementById('studentNameDisplay').innerText = studentName;

    // Hiển thị Modal
    enrollModalInstance.show();

    // Subscribe SSE để nhận kết quả trước khi POST (tránh race condition)
    openEnrollEventSource(classroomId);

    const qs = `?studentId=${studentId}&studentName=${encodeURIComponent(studentName)}`;
    
    // Gọi API yêu cầu thiết bị mở chế độ Enroll
    fetch(`/classrooms/${classroomId}/fingerprints/enroll${qs}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                let errMsg = 'Không thể kết nối tới server';
                try {
                    const errObj = JSON.parse(text);
                    errMsg = errObj.message || text;
                } catch (e) {
                    errMsg = text;
                }
                throw new Error(errMsg);
            });
        }
        // OK: ESP32 đã nhận tín hiệu. Thiết bị sẽ publish kết quả qua MQTT.
        console.log("Đã gửi lệnh đăng ký vân tay cho:", studentName);

    }).catch(error => {
        console.error('Error:', error);
        closeEnrollEventSource();
        enrollModalInstance.hide();
        showToast('Lỗi: ' + error.message, false);
    });
}

function openEnrollEventSource(classroomId) {
    // Đóng EventSource cũ nếu có
    closeEnrollEventSource();

    enrollEventSource = new EventSource(`/classrooms/${classroomId}/fingerprints/enroll/sse`);

    enrollEventSource.addEventListener('enroll-result', function (event) {
        const result = JSON.parse(event.data);
        isEnrollSuccessOrError = true; // Flag báo không gửi thẻ cancel khi hide modal
        closeEnrollEventSource();
        enrollModalInstance.hide();

        if (result.success) {
            showToast(result.message || 'Đăng ký vân tay thành công!', true);
        } else {
            showToast(result.message || 'Đăng ký vân tay thất bại. Vui lòng thử lại.', false);
        }
    });

    enrollEventSource.onerror = function (err) {
        console.error('SSE error:', err);
        closeEnrollEventSource();
    };
}

function closeEnrollEventSource() {
    if (enrollEventSource) {
        enrollEventSource.close();
        enrollEventSource = null;
    }
}

function cancelEnroll(classroomId) {
    fetch(`/classrooms/${classroomId}/fingerprints/cancel`, {
        method: 'GET'
    }).then(response => {
        if (!response.ok) {
            console.error("Gửi lệnh Cancel thất bại");
        } else {
            console.log("Đã gửi lệnh Cancel quá trình Enroll vân tay");
        }
    }).catch(error => {
        console.error('Lỗi khi gửi lệnh Cancel:', error);
    });
}

function setModalWaiting() {
    const body = document.getElementById('enrollModalBody');
    if (body) {
        body.innerHTML = `
            <div class="mb-4">
                <i class="fas fa-fingerprint icon-scanning"></i>
            </div>
            <h4 class="fw-bold text-dark mb-3">Đang chờ thiết bị...</h4>
            <p class="text-muted mb-0" style="font-size: 15px;">
                Vui lòng yêu cầu học sinh <strong id="studentNameDisplay" class="text-primary fs-5 d-block my-2"></strong>
                đặt tay lên cảm biến ESP32 và làm theo hướng dẫn trên màn hình thiết bị.
            </p>
            <div class="mt-4">
                <div class="spinner-border spinner-border-sm text-muted opacity-50 me-2" role="status"></div>
                <span class="text-muted small">Hệ thống đang lắng nghe tín hiệu MQTT...</span>
            </div>
        `;
    }
}

function showToast(message, success) {
    // Xóa toast cũ nếu có
    const existingToast = document.getElementById('enrollToast');
    if (existingToast) existingToast.remove();

    const bgClass = success ? 'bg-success' : 'bg-danger';
    const icon = success ? 'fa-check-circle' : 'fa-times-circle';

    const toastEl = document.createElement('div');
    toastEl.id = 'enrollToast';
    toastEl.className = `toast align-items-center text-white ${bgClass} border-0 position-fixed bottom-0 end-0 m-4`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.style.zIndex = '9999';
    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="fas ${icon} me-2"></i>${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    document.body.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();

    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}