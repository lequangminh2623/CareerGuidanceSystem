const MAX_EXTRA_COLUMNS = 3;

function addExtraColumn() {
    const headerRow = document.querySelector("#extra-header-buttons");
    const anyCell = document.querySelector(".extra-score-cells");
    if (!anyCell) return;

    // Tính số lượng cột bổ sung hiện tại
    const currentCount = anyCell.querySelectorAll("input").length;
    if (currentCount >= MAX_EXTRA_COLUMNS) {
        showAlert(`Chỉ được phép tối đa ${MAX_EXTRA_COLUMNS} cột điểm thường xuyên.`, "warning");
        return;
    }

    // 1. Thêm input vào mỗi hàng
    document.querySelectorAll(".extra-score-cells").forEach((cell) => {
        const studentIndex = cell.getAttribute("data-index");
        const input = document.createElement("input");
        input.type = "number";
        input.className = "form-control score-input";
        input.name = `scores[${studentIndex}].extraScores[${currentCount}]`;
        input.min = 0; input.max = 10; input.step = 0.1;
        cell.appendChild(input);
    });

    // 2. Thêm icon Xóa vào Header
    const removeIcon = document.createElement("i");
    removeIcon.className = "fas fa-times-circle btn-remove-col";
    removeIcon.title = `Xóa cột ${currentCount + 1}`;
    removeIcon.setAttribute("onclick", `removeExtraColumn(${currentCount})`);
    headerRow.appendChild(removeIcon);
}

function reindexExtraColumns() {
    // Cập nhật name cho tất cả các input để Spring map chuẩn xác
    document.querySelectorAll(".extra-score-cells").forEach(cell => {
        const studentIndex = cell.getAttribute("data-index");
        const inputs = cell.querySelectorAll("input");
        inputs.forEach((input, newIndex) => {
            input.name = `scores[${studentIndex}].extraScores[${newIndex}]`;
        });
    });

    // Cập nhật lại logic onlick cho các nút xóa ở Header
    const headerButtons = document.querySelectorAll(".btn-remove-col");
    headerButtons.forEach((btn, newIndex) => {
        btn.setAttribute("onclick", `removeExtraColumn(${newIndex})`);
        btn.title = `Xóa cột ${newIndex + 1}`;
    });
}

async function removeExtraColumn(index) {
    const confirmed = await showConfirm(`Bạn có chắc muốn xoá cột điểm thường xuyên thứ ${index + 1}?`);
    if (confirmed) {
        // Xóa input ở các hàng
        document.querySelectorAll(".extra-score-cells").forEach(cell => {
            const inputs = cell.querySelectorAll("input");
            if (inputs.length > index) {
                inputs[index].remove();
            }
        });

        // Xóa icon trên Header
        const headerButtons = document.querySelectorAll(".btn-remove-col");
        if (headerButtons.length > index) {
            headerButtons[index].remove();
        }

        // Đánh lại Index cho DTO
        reindexExtraColumns();
    }
}

async function changeTranscriptStatus(selectElement) {
    const sectionId = selectElement.getAttribute('data-section-id');
    const newStatus = selectElement.value;
    const oldStatus = selectElement.getAttribute('data-current-status');

    // Tùy chọn: Hiển thị trạng thái đang xử lý
    selectElement.disabled = true;

    try {
        const response = await fetch(`/transcripts/${sectionId}/change-status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus })
        });

        if (response.ok) {
            // Thành công: Cập nhật lại trạng thái hiện tại
            selectElement.setAttribute('data-current-status', newStatus);

            // Xử lý UI: Khóa/Mở khóa các ô nhập điểm tùy theo trạng thái
            toggleInputFields(newStatus === 'Locked');

            showAlert('Đã thay đổi trạng thái bảng điểm thành ' + newStatus, 'success');
        } else {
            // Thất bại: Trả lại giá trị cũ
            selectElement.value = oldStatus;
            showAlert('Có lỗi xảy ra khi đổi trạng thái. Vui lòng thử lại!', 'danger');
        }
    } catch (error) {
        console.error('Error changing status:', error);
        selectElement.value = oldStatus;
        showAlert('Lỗi kết nối đến máy chủ!', 'danger');
    } finally {
        selectElement.disabled = false;
    }
}

// Hàm phụ trợ để Khóa/Mở khóa form khi bảng điểm bị Lock
function toggleInputFields(isLocked) {
    // Tìm tất cả các ô input điểm, nút thêm/xóa cột và nút Lưu
    const inputs = document.querySelectorAll('.score-input');
    const buttons = document.querySelectorAll('.btn-add-col, .btn-remove-col, button[type="submit"]');

    inputs.forEach(input => {
        input.readOnly = isLocked;
        if (isLocked) {
            input.classList.add('bg-light');
        } else {
            input.classList.remove('bg-light');
        }
    });

    buttons.forEach(btn => {
        btn.disabled = isLocked;
    });
}

// Chạy thử hàm toggle 1 lần lúc mới load trang để khóa form nếu trạng thái ban đầu đã là LOCKED
document.addEventListener("DOMContentLoaded", function () {
    const statusSelect = document.getElementById('transcriptStatus');
    if (statusSelect && statusSelect.value === 'Locked') {
        toggleInputFields(true);
    }
});