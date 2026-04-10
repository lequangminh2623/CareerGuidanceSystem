/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */
function toggleStudentCode() {
    const roleSelect = document.getElementById("role");
    const studentCodeGroup = document.getElementById("codeGroup");
    const studentCodeInput = document.getElementById("code");

    if (roleSelect.value === "Student") {
        studentCodeGroup.style.display = "block";
        studentCodeInput.disabled = false;
    } else {
        studentCodeGroup.style.display = "none";
        studentCodeInput.disabled = true;
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const roleSelect = document.getElementById("role");
    roleSelect.addEventListener("change", toggleStudentCode);
    toggleStudentCode();
});

document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('fileInput');
    const preview = document.getElementById('avatarPreview');

    // Kiểm tra element tồn tại để tránh lỗi null
    if (input && preview) {
        input.addEventListener('change', function (event) {
            const file = event.target.files[0];

            if (file) {
                // KIỂM TRA LOẠI FILE (Validate)
                if (!file.type.startsWith('image/')) {
                    showAlert('Vui lòng chỉ chọn file hình ảnh!', 'warning');
                    input.value = ''; // Reset input
                    return;
                }

                // HIỂN THỊ ẢNH
                const reader = new FileReader();
                reader.onload = function (e) {
                    preview.src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
});
