function deleteYear(url, id) {
    if (confirm("Bạn chắc chắn muốn xóa?")) {
        fetch(`${url}${id}`, {
            method: 'DELETE'
        }).then(res => {
            if (res.status === 204) {
                alert("Xóa thành công!");
                location.reload();
            } else {
                return res.text().then(text => {
                    alert(text);
                });
            }
        });
    }
}


