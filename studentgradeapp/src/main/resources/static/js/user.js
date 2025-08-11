/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */
function deleteUser(endpoint, id) {
    if (confirm("Bạn chắc chắn xóa không?")) {
        fetch(endpoint + id, {
            method: "delete"
        }).then(res => {
            if (res.status === 204) {
                alert("Xóa thành công!");
                location.reload();
            } else
                alert("Có lỗi xảy ra!");
        });
    }
}

function toggleStudentCode() {
    const roleSelect = document.getElementById("role");
    const studentCodeGroup = document.getElementById("studentCodeGroup");
    const studentCodeInput = document.getElementById("studentCode");

    if (roleSelect.value === "ROLE_STUDENT") {
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
