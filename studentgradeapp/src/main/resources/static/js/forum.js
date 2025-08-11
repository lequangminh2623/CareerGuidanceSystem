function truncateSelectOptions(selectId, maxLength = 10) {
    const select = document.getElementById(selectId);
    if (!select)
        return;

    for (let i = 0; i < select.options.length; i++) {
        const option = select.options[i];
        const fullText = option.text;

        option.title = fullText;

        const parts = fullText.split(' - ');
        if (parts.length < 2)
            continue;

        const prefix = parts[0];
        const content = parts[1];

        if (content.length > maxLength) {
            option.text = `${prefix} - ${content.substring(0, maxLength - 3)}...`;
        }
    }
}

truncateSelectOptions("postSelect", 50);
truncateSelectOptions("replySelect", 50);

const userSelect = document.querySelector('#userSelect');
if (userSelect) {
    new TomSelect(userSelect, {
        placeholder: 'Tìm theo người đăng...',
        allowEmptyOption: false
    });
}

const classroomSelect = document.querySelector('#classroomSelect');
if (classroomSelect) {
    new TomSelect(classroomSelect, {
        placeholder: 'Tìm theo lớp học...',
        allowEmptyOption: false
    });
}


const replySelect = document.querySelector('#replySelect');
if (replySelect) {
    new TomSelect(replySelect, {
        placeholder: 'Tìm theo phản hồi...',
        allowEmptyOption: false
    });
}

const postSelect = document.querySelector('#postSelect');
if (postSelect) {
    new TomSelect(postSelect, {
        placeholder: 'Tìm theo bài đăng...',
        allowEmptyOption: false
    });
}

function deleteForumPost(url, id) {
    if (confirm("Bạn chắc chắn xóa không?")) {
        fetch(`${url}${id}`, {
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

function deleteReply(url, id) {
    if (confirm("Bạn chắc chắn xóa không?")) {
        fetch(`${url}${id}`, {
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

const contextPath = document.body.dataset.contextPath;

function getChildReplies(btn, url, id, depth = 1, path = "/child-replies") {
    const icon = btn.querySelector('i');
    const row = btn.closest('tr');

    if (icon.classList.contains('fa-chevron-up')) {
        let next = row.nextElementSibling;
        while (next && next.classList.contains('reply-row')) {
            if (parseInt(next.dataset.parentId) === id || parseInt(next.dataset.depth) > depth) {
                const toRemove = next;
                next = next.nextElementSibling;
                toRemove.remove();
            } else
                break;
        }
    }

    icon.classList.toggle('fa-chevron-down');
    icon.classList.toggle('fa-chevron-up');

    if (icon.classList.contains('fa-chevron-down'))
        return;

    fetch(`${url}${id}${path}`)
            .then(res => res.json())
            .then(data => {
                if (data.length === 0) {
                    const tr = document.createElement('tr');
                    tr.classList.add('reply-row');
                    tr.dataset.parentId = id;
                    tr.dataset.depth = depth;

                    const td = document.createElement('td');
                    td.colSpan = 6;
                    td.innerHTML = `<i>Không có phản hồi nào.</i>`;
                    td.style.paddingLeft = `${depth * 20}px`;
                    tr.appendChild(td);
                    row.parentNode.insertBefore(tr, row.nextSibling);
                } else {
                    data.reverse().forEach(reply => {
                        const tr = document.createElement('tr');
                        tr.classList.add('reply-row');
                        tr.dataset.parentId = id;
                        tr.dataset.depth = depth;

                        tr.innerHTML = `
                        <td style="padding-left: ${depth * 20}px;">
                            <i class="fa-solid fa-arrow-right fa-lg"></i> ${reply.id}
                        </td>
                        <td style="padding-left: ${depth * 20}px;">${reply.content}</td>
                        <td style="padding-left: ${depth * 20}px;">
                            ${reply.image ? `<a href="${reply.image}" target="_blank">
                                <img src="${reply.image}" class="mt-2" style="max-width: 150px;" />
                            </a>` : ''}
                        </td>
                        <td style="padding-left: ${depth * 20}px;">${reply.user}</td>
                        <td style="padding-left: ${depth * 20}px;">
                            <a href="/forums/${reply.id}" class="btn btn-warning">✏️</a>
                            <button class="btn btn-danger" onclick="deleteReply('${contextPath}/replies/', ${reply.id})">🗑️</button>
                        </td>
                        <td style="padding-left: ${depth * 20}px;">
                            <button class="btn btn-ouline text-info" onclick="getChildReplies(this, '${url}', ${reply.id}, ${depth + 1})">
                                Xem thêm <i class="fa-solid fa-chevron-down fa-lg"></i>
                            </button>
                        </td>
                    `;

                        row.parentNode.insertBefore(tr, row.nextSibling);
                    });
                }
            })
            .catch(err => console.error("Lỗi khi lấy phản hồi:", err));
}

