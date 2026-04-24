/**
 * WebSocket STOMP Client cho Admin (Thymeleaf).
 * Kết nối đến attendance-service WebSocket qua API Gateway.
 * Subscribe /topic/devices và /topic/attendances cho real-time notifications.
 *
 * Requires: SockJS + STOMP.js (included via CDN in base.html)
 */

const WS_CONFIG = {
    // URL WebSocket endpoint qua attendance-service (chạy trực tiếp, không qua gateway)
    // Trong Docker: attendance-service là internal, admin browser kết nối qua gateway
    wsUrl: '/ws',  // SockJS endpoint relative to current page origin (admin-service)
    reconnectDelay: 5000,
    maxReconnectAttempts: 10,
};

let stompClient = null;
let reconnectAttempts = 0;
let reconnectTimer = null;

/**
 * Khởi tạo WebSocket connection.
 * @param {string} attendanceWsUrl - URL đến WebSocket endpoint attendance-service
 * @param {string} token - JWT Token cho xác thực
 */
function initWebSocket(attendanceWsUrl, token) {
    if (stompClient && stompClient.connected) {
        return; // Đã kết nối rồi
    }

    const socket = new SockJS(attendanceWsUrl);
    stompClient = Stomp.over(socket);

    // Tắt debug log trong production
    stompClient.debug = function () { };

    const headers = token ? { Authorization: 'Bearer ' + token } : {};

    stompClient.connect(headers, function (frame) {
        console.log('[WS] Connected:', frame);
        reconnectAttempts = 0;

        // Subscribe /topic/devices
        stompClient.subscribe('/topic/devices', function (message) {
            const event = JSON.parse(message.body);
            handleDeviceEvent(event);
        });

        // Subscribe /topic/attendances
        stompClient.subscribe('/topic/attendances', function (message) {
            const event = JSON.parse(message.body);
            handleAttendanceEvent(event);
        });

    }, function (error) {
        console.error('[WS] Error:', error);
        scheduleReconnect(attendanceWsUrl, token);
    });
}

/**
 * Xử lý sự kiện Device Discover.
 */
function handleDeviceEvent(event) {
    if (event.eventType !== 'DEVICE_DISCOVERED') return;

    const data = event.data;

    // Hiển thị toast thông báo
    if (data.isNew) {
        showWsToast(
            `Phát hiện thiết bị mới: ${data.chipId}`,
            'info'
        );
    } else {
        showWsToast(
            `Thiết bị ${data.chipId} đã kết nối lại`,
            'info'
        );
    }

    // Nếu đang ở trang device/list → thêm row mới vào bảng
    const deviceTableBody = document.getElementById('deviceTableBody');
    if (deviceTableBody && data.isNew) {
        insertDeviceRow(deviceTableBody, data);
    }
}

/**
 * Xử lý sự kiện Attendance Recorded.
 */
function handleAttendanceEvent(event) {
    if (event.eventType !== 'ATTENDANCE_RECORDED') return;

    const data = event.data;

    // Lấy classroomId hiện đang xem (nếu có)
    const currentClassroomId = document.querySelector('[data-current-classroom-id]')?.getAttribute('data-current-classroom-id');

    // Chỉ hiển thị nếu đang xem trang attendance của cùng lớp
    if (currentClassroomId && currentClassroomId === data.classroomId) {
        showWsToast(
            `Học sinh ${data.studentName} vừa điểm danh lúc ${data.checkInTime?.substring(0, 5)}`,
            'success'
        );

        // Tìm table body của trang attendance
        const attendanceTableBody = document.getElementById('attendanceTableBody');
        if (attendanceTableBody) {
            // Thêm row hoặc highlight row hiện có
            insertOrUpdateAttendanceRow(attendanceTableBody, data);
        }
    }
}

/**
 * Thêm row mới vào bảng điểm danh hoặc cập nhật row hiện có.
 */
function insertOrUpdateAttendanceRow(tbody, data) {
    // Tìm row bằng data-student-id
    let row = tbody.querySelector(`[data-student-id="${data.studentId}"]`);

    if (!row) {
        // Xóa row "trống dữ liệu" nếu có
        const emptyRow = tbody.querySelector('.empty-state-row');
        if (emptyRow) emptyRow.remove();

        const rowCount = tbody.querySelectorAll('tr').length;
        row = document.createElement('tr');
        row.setAttribute('data-student-id', data.studentId);
        row.className = 'transition-all duration-200 ws-attendance-highlight';
        row.innerHTML = `
            <input type="hidden" name="attendances[${rowCount}].studentId" value="${data.studentId}" />
            <td class="text-center text-secondary fw-bold">${rowCount + 1}</td>
            <td>
                <span class="badge bg-indigo-soft text-indigo fw-black px-3 py-2 rounded-pill fs-12 uppercase tracking-tighter">
                    ${data.studentCode}
                </span>
            </td>
            <td class="fw-bold text-dark">${data.studentName}</td>
            <td class="text-center">
                <label class="custom-radio present" title="Vân tay: ${data.checkInTime.substring(0, 5)}">
                    <input type="radio" name="attendances[${rowCount}].status" value="Present" ${data.status === 'Present' ? 'checked' : ''}>
                    <div class="radio-mark"><i class="fas fa-check"></i></div>
                </label>
            </td>
            <td class="text-center">
                <label class="custom-radio late" title="Vân tay: ${data.checkInTime.substring(0, 5)}">
                    <input type="radio" name="attendances[${rowCount}].status" value="Late" ${data.status === 'Late' ? 'checked' : ''}>
                    <div class="radio-mark"><i class="fas fa-check"></i></div>
                </label>
            </td>
            <td class="text-center">
                <label class="custom-radio absent">
                    <input type="radio" name="attendances[${rowCount}].status" value="Absent" ${data.status === 'Absent' ? 'checked' : ''}>
                    <div class="radio-mark"><i class="fas fa-times"></i></div>
                </label>
            </td>
        `;
        tbody.appendChild(row);
    } else {
        // Highlight row hiện có và cập nhật trạng thái
        row.classList.add('ws-attendance-highlight');
        const radio = row.querySelector(`input[value="${data.status}"]`);
        if (radio) {
            radio.checked = true;
            radio.dispatchEvent(new Event('change'));
        }
    }

    setTimeout(() => row.classList.remove('ws-attendance-highlight'), 5000);
}

/**
 * Thêm row mới vào bảng thiết bị (trang device/list).
 */
function insertDeviceRow(tbody, data) {
    // Kiểm tra xem thiết bị đã có trong bảng chưa
    const existingRow = tbody.querySelector(`[data-device-id="${data.chipId}"]`);
    if (existingRow) return;

    // Xóa row "trống dữ liệu" nếu có
    const emptyRow = tbody.querySelector('.empty-state-row');
    if (emptyRow) emptyRow.remove();

    const row = document.createElement('tr');
    row.setAttribute('data-device-id', data.chipId);
    row.className = 'transition-all duration-200 ws-new-row';
    row.innerHTML = `
        <td class="ps-4">
            <div class="d-flex align-items-center">
                <div class="bg-indigo-soft text-indigo p-3 rounded-4 line-height-1 shadow-sm">
                    <i class="fa-solid fa-microchip fs-5"></i>
                </div>
                <span class="fw-black text-dark fs-14 font-monospace letter-spacing-tight ms-3">${data.chipId}</span>
            </div>
        </td>
        <td>
            <div class="d-inline-flex align-items-center gap-2 px-3 py-1.5 rounded-pill bg-success-subtle text-success">
                <i class="fas fa-circle" style="font-size: 6px;"></i>
                <span class="fw-black text-uppercase tracking-widest" style="font-size: 10px;">Đang bật</span>
            </div>
        </td>
        <td>
            <span class="fw-bold fs-14 text-danger opacity-50">Chưa được gán</span>
        </td>
        <td class="text-end pe-4">
            <div class="d-flex justify-content-end gap-1">
                <form action="/devices/${data.chipId}" method="post" class="d-inline">
                    <input type="hidden" name="_method" value="PATCH">
                    <input type="hidden" name="active" value="false">
                    <button type="submit" class="btn-icon text-warning" title="Tạm tắt thiết bị">
                        <i class="fas fa-power-off"></i>
                    </button>
                </form>

                <button type="button" class="btn-icon delete" data-url="/devices/"
                    data-id="${data.chipId}" onclick="deleteHandler(this)" title="Xóa thiết bị">
                    <i class="fas fa-trash-can"></i>
                </button>

                </span>
            </div>
        </td>
    `;

    // Thêm vào đầu bảng
    tbody.insertBefore(row, tbody.firstChild);

    // Animation highlight
    setTimeout(() => row.classList.remove('ws-new-row'), 3000);
}

/**
 * Hiển thị toast notification (kiểu Bootstrap 5).
 */
function showWsToast(message, type) {
    // Xóa toast cũ nếu có quá 5 cái
    const existingToasts = document.querySelectorAll('.ws-toast');
    if (existingToasts.length >= 5) {
        existingToasts[0].remove();
    }

    const bgClass = {
        'success': 'bg-success',
        'info': 'bg-primary',
        'warning': 'bg-warning',
        'error': 'bg-danger'
    }[type] || 'bg-primary';

    const icon = {
        'success': 'fa-check-circle',
        'info': 'fa-broadcast-tower',
        'warning': 'fa-exclamation-triangle',
        'error': 'fa-times-circle'
    }[type] || 'fa-info-circle';

    const toastEl = document.createElement('div');
    toastEl.className = `toast ws-toast align-items-center text-white ${bgClass} border-0 position-fixed bottom-0 end-0 m-3 shadow-lg`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.style.zIndex = '9999';
    toastEl.style.minWidth = '300px';
    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body fw-bold">
                <i class="fas ${icon} me-2"></i>${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    // Stack toasts vertically
    const offset = document.querySelectorAll('.ws-toast').length * 60;
    toastEl.style.bottom = (12 + offset) + 'px';

    document.body.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl, { delay: 5000 });
    toast.show();

    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

/**
 * Reconnect logic.
 */
function scheduleReconnect(wsUrl, token) {
    if (reconnectAttempts >= WS_CONFIG.maxReconnectAttempts) {
        console.warn('[WS] Max reconnect attempts reached');
        return;
    }

    if (reconnectTimer) clearTimeout(reconnectTimer);

    reconnectAttempts++;
    const delay = WS_CONFIG.reconnectDelay * Math.min(reconnectAttempts, 5);
    console.log(`[WS] Reconnecting in ${delay}ms (attempt ${reconnectAttempts})`);

    reconnectTimer = setTimeout(function () {
        initWebSocket(wsUrl, token);
    }, delay);
}

/**
 * Disconnect WebSocket.
 */
function disconnectWebSocket() {
    if (stompClient) {
        stompClient.disconnect(function () {
            console.log('[WS] Disconnected');
        });
        stompClient = null;
    }
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
}
