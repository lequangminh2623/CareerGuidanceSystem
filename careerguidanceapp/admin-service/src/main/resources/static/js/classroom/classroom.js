document.addEventListener('DOMContentLoaded', function () {
    const listAvailable = document.getElementById('listAvailable');
    const listSelected = document.getElementById('studentIds');
    const searchAvailable = document.getElementById('searchAvailable');
    const filterSelected = document.getElementById('filterSelected');
    const loadingText = document.getElementById('loadingText');
    const selectedCount = document.getElementById('selectedCount');
    const form = document.getElementById('classroomForm');

    // Hàm cập nhật số lượng
    function updateCount() {
        selectedCount.textContent = listSelected.options.length;
    }

    updateCount();

    // 1. DI CHUYỂN ITEM GIỮA 2 CỘT
    function moveItems(source, destination, moveAll = false) {
        const options = Array.from(source.options);
        options.forEach(opt => {
            if (moveAll || opt.selected) {
                opt.selected = false; // Bỏ highlight
                destination.appendChild(opt);
            }
        });
        updateCount();
    }

    document.getElementById('btnMoveRight').addEventListener('click', () => moveItems(listAvailable, listSelected));
    document.getElementById('btnMoveAllRight').addEventListener('click', () => moveItems(listAvailable, listSelected, true));
    document.getElementById('btnMoveLeft').addEventListener('click', () => moveItems(listSelected, listAvailable));
    document.getElementById('btnMoveAllLeft').addEventListener('click', () => moveItems(listSelected, listAvailable, true));

    // Double click để chuyển nhanh
    listAvailable.addEventListener('dblclick', () => moveItems(listAvailable, listSelected));
    listSelected.addEventListener('dblclick', () => moveItems(listSelected, listAvailable));

    // 2. LỌC DANH SÁCH BÊN PHẢI (Client-side)
    filterSelected.addEventListener('input', function () {
        const term = this.value.toLowerCase();
        Array.from(listSelected.options).forEach(opt => {
            const text = opt.text.toLowerCase();
            opt.style.display = text.includes(term) ? '' : 'none';
        });
    });

    // 3. TÌM KIẾM AJAX & LOAD MORE CHO CỘT TRÁI
    let typingTimer;
    let currentPage = 1;
    let isLoading = false;
    let hasMoreData = true;

    // Hàm hiển thị "Đang tải thêm..." ở đáy danh sách
    function showLoadingOption() {
        if (!document.getElementById('loadingOption')) {
            const opt = document.createElement('option');
            opt.id = 'loadingOption';
            opt.disabled = true;
            opt.text = '⏳ Đang tải thêm...';
            opt.style.textAlign = 'center';
            opt.style.fontStyle = 'italic';
            opt.style.color = '#6c757d'; // Màu xám nhạt
            listAvailable.appendChild(opt);

            // Tự động cuộn xuống đáy để người dùng thấy dòng này
            listAvailable.scrollTop = listAvailable.scrollHeight;
        }
    }

    // Hàm xóa dòng "Đang tải thêm..."
    function removeLoadingOption() {
        const opt = document.getElementById('loadingOption');
        if (opt) opt.remove();
    }

    // Hàm gọi API lấy dữ liệu
    function fetchStudents(term, page, isAppend = false) {
        if (isLoading || (!hasMoreData && isAppend)) return;

        isLoading = true;

        // Nếu là tìm kiếm mới (trang 0), hiện text loading ở ngoài box
        // Nếu là cuộn thêm (isAppend), hiện option loading ở trong box
        if (!isAppend) {
            loadingText.style.display = 'block';
            listAvailable.innerHTML = '';
        } else {
            showLoadingOption();
        }

        fetch(`/classrooms/students/search?term=${encodeURIComponent(term)}&page=${page}`)
            .then(response => response.json())
            .then(data => {
                loadingText.style.display = 'none';
                removeLoadingOption(); // Xóa thẻ loading option nếu có

                isLoading = false;
                hasMoreData = data.hasMore; // Cập nhật cờ từ backend
                currentPage = page;

                const selectedIds = Array.from(listSelected.options).map(o => o.value);

                data.results.forEach(student => {
                    if (!selectedIds.includes(student.id)) {
                        const opt = document.createElement('option');
                        opt.value = student.id;
                        opt.text = student.text;
                        listAvailable.appendChild(opt);
                    }
                });
            })
            .catch(err => {
                loadingText.style.display = 'none';
                removeLoadingOption();
                isLoading = false;
                console.error('Lỗi khi tải dữ liệu sinh viên', err);
            });
    }

    // Bắt sự kiện gõ tìm kiếm
    searchAvailable.addEventListener('input', function () {
        clearTimeout(typingTimer);
        const term = this.value.trim();

        if (term.length < 2) {
            listAvailable.innerHTML = '';
            hasMoreData = false;
            return;
        }

        typingTimer = setTimeout(() => {
            hasMoreData = true; // Reset cờ khi tìm kiếm mới
            fetchStudents(term, 1, false);
        }, 500);
    });

    // Bắt sự kiện cuộn (Scroll) trên danh sách
    listAvailable.addEventListener('scroll', function() {
        // Kiểm tra xem thanh cuộn đã chạm đáy chưa
        // Thêm sai số 5px để bắt sự kiện nhạy hơn trên một số màn hình
        const isAtBottom = this.scrollTop + this.clientHeight >= this.scrollHeight - 5;

        if (isAtBottom) {
            const term = searchAvailable.value.trim();
            // Nếu đủ điều kiện thì gọi load thêm trang tiếp theo (isAppend = true)
            if (term.length >= 2 && hasMoreData && !isLoading) {
                fetchStudents(term, currentPage + 1, true);
            }
        }
    });

    if (form && listSelected) {
        form.addEventListener('submit', function() {
            // Bôi đen tất cả các option ở cột phải để trình duyệt gửi toàn bộ mảng studentIds đi
            Array.from(listSelected.options).forEach(opt => {
                opt.selected = true;
            });
        });
    }
});

