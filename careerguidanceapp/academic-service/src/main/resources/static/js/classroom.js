/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */

new TomSelect('#gradeSelect', {
    placeholder: 'Tìm Khối lớp...',
    allowEmptyOption: false
});

new TomSelect('#studentSelect', {
    plugins: ['remove_button'],
    placeholder: 'Tìm và chọn sinh viên...'
});

function removeStudentFromClassroom(btn) {
    const url = btn.dataset.url;
    const classroomId = btn.dataset.classroomId;
    const studentId = btn.dataset.studentId;
    if (confirm("Bạn có chắc chắn muốn xóa sinh viên và điểm khỏi lớp học này không?")) {
        fetch(`${url}${classroomId}/students/${studentId}`, {
            method: "DELETE"
        }).then(res => {
            if (res.status === 204) {
                alert("Xóa thành công!");
                location.reload();
            } else {
                return res.message().then(text => {
                    alert(text);
                });
            }
        });
    }
}

