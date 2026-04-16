$(document).ready(function () {
    const classroomId = window.CLASSROOM_ID;
    let rowCount = $('#section-table-body tr').not('#emptyRow').length;

    const select2Config = {
        placeholder: 'Tìm và chọn giáo viên...',
        allowClear: true,
        language: {
            inputTooShort: () => "Nhập để tìm...",
            searching: () => "Đang tìm...",
            noResults: () => "Không tìm thấy"
        },
        ajax: {
            url: `/classrooms/${classroomId}/sections/teachers/search`,
            dataType: 'json',
            delay: 300,
            data: (params) => ({ term: params.term || '', page: params.page ? params.page : 1 }),
            processResults: (data, params) => {
                params.page = params.page || 1;
                return { results: data.results, pagination: { more: data.hasMore } };
            },
            cache: true
        }
    };

    // Init cho các dòng đã có sẵn
    $('.teacher-select2').select2(select2Config);

    // Logic Thêm dòng mới CÓ LỌC MÔN HỌC
    $('#btnAddRow').click(function () {

        // 1. Quét toàn bộ ID môn học đang có mặt trên màn hình
        let usedCurriculumIds = [];

        // Lấy từ các môn đã lưu trong DB (dòng cũ)
        $('.existing-curriculum').each(function () {
            if ($(this).val()) usedCurriculumIds.push($(this).val());
        });

        // Lấy từ các môn đã chọn ở các dòng vừa thêm mới (chưa lưu)
        $('.new-curriculum').each(function () {
            if ($(this).val()) usedCurriculumIds.push($(this).val());
        });

        // 2. Clone template HTML
        let templateHtml = $('#newRowTemplate').html().replace(/__INDEX__/g, rowCount);
        let $newRow = $(templateHtml); // Chuyển thành jQuery object để dễ can thiệp

        // 3. Lọc bỏ các option đã bị sử dụng
        $newRow.find('.new-curriculum option').each(function () {
            let optionValue = $(this).val();
            if (optionValue && usedCurriculumIds.includes(optionValue)) {
                $(this).remove(); // Xóa hẳn môn này khỏi danh sách thả xuống
            }
        });

        // Kiểm tra xem có còn môn nào để chọn không (Nếu chỉ còn 1 option rỗng đầu tiên)
        if ($newRow.find('.new-curriculum option').length <= 1) {
            showAlert("Toàn bộ môn học trong khối đã được phân công cho lớp này!", "warning");
            return; // Dừng lại, không thêm dòng nữa
        }

        // Nếu còn môn, tiến hành thêm vào bảng
        $('#emptyRow').remove();
        rowCount++;
        $('#section-table-body').prepend($newRow);

        // 4. Init Select2 trực tiếp lên phần tử con của dòng mới (An toàn 100%)
        $newRow.find('.teacher-select2-dynamic').select2(select2Config);
        $('#btnAddRow').prop('disabled', true);
    });

    // Xóa dòng chưa lưu (UI)
    $(document).on('click', '.btn-remove-row', function () {
        $(this).closest('tr').remove();
    });

    // Thêm riêng biệt phân công
    $(document).on('click', '.btn-add-single-section', function () {
        const $btn = $(this);
        const $row = $btn.closest('tr');

        const curriculumId = $row.find('.new-curriculum').val();
        let teacherId = $row.find('.teacher-select2-dynamic').val();

        if (!curriculumId) {
            showAlert('Vui lòng chọn môn học!', 'warning');
            return;
        }

        // Backend coi null teacherId nếu là ""
        if (!teacherId || teacherId.trim() === "") teacherId = null;

        $btn.prop('disabled', true);

        const requestData = {
            classroomId: classroomId,
            curriculumId: curriculumId,
            teacherId: teacherId,
            scoreStatus: "Draft"
        };

        $.ajax({
            url: `/classrooms/${classroomId}/sections/single`,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(requestData),
            success: function () {
                window.location.reload();
            },
            error: function (err) {
                showAlert('Có lỗi xảy ra khi thêm phân công!', 'danger');
                $btn.prop('disabled', false);
                $('#btnAddRow').prop('disabled', false);
            }
        });
    });

    // Bắt sự kiện submit form lưu phân công hàng loạt
    $('form').on('submit', function (e) {
        let changedCount = 0;
        let validIndex = 0;

        $(this).find('tr:not(#emptyRow)').each(function () {
            // Find the teacher select
            let $select = $(this).find('select[name$=".teacherId"]');
            
            if ($select.length > 0) {
                let original = $select.data('original-teacher') || "";
                let current = $select.val() || "";
                
                // Mới tạo chưa lưu sẽ không có class '.existing-curriculum'
                let isNewRow = $(this).find('.existing-curriculum').length === 0;

                if (!isNewRow && original == current) {
                    // Nếu giáo viên không đổi -> Không gửi lên server
                    $(this).find('input, select').prop('disabled', true);
                } else {
                    changedCount++;
                    // Ghi đè lại index để tránh array rỗng (null) ở backend
                    $(this).find('input, select').each(function () {
                        if ($(this).attr('name')) {
                            let newName = $(this).attr('name').replace(/sections\[\d+\]/, `sections[${validIndex}]`);
                            $(this).attr('name', newName);
                        }
                    });
                    validIndex++;
                }
            }
        });

        // Nếu không có gì thay đổi
        if (changedCount === 0) {
            e.preventDefault();
            showAlert('Không có sự thay đổi giáo viên nào để lưu!', 'warning');
            $(this).find('input, select').prop('disabled', false); // re-enable form items
        }
    });
});