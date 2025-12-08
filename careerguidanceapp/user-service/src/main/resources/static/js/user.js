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
