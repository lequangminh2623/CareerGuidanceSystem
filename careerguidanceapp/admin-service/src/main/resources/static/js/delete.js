async function deleteHandler(btn) {
    const url = btn.dataset.url;
    const id = btn.dataset.id;
    const confirmed = await showConfirm("Bạn chắc chắn muốn xóa?");
    if (confirmed) {
        fetch(`${url}${id}`, {
            method: 'DELETE'
        }).then(res => {
            if (res.ok) {
                showAlert("Xóa thành công!", "success");
                setTimeout(() => location.reload(), 1500);
            } else {
                return res.text().then(text => {
                    try {
                        const json = JSON.parse(text);
                        showAlert(json.message || "Có lỗi xảy ra khi xóa!", "danger");
                    } catch (e) {
                        showAlert(text || "Có lỗi xảy ra khi xóa!", "danger");
                    }
                });
            }
        });
    }
}