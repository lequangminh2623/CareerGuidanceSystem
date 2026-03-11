document.addEventListener('DOMContentLoaded', function() {
    // --- 1. DATA PREPARATION ---
    const rawStats = SERVER_STATS_DATA || {};

    const safeGet = (obj, key) => (obj && obj[key] !== undefined) ? obj[key] : 0;

    // Data Roles
    const roleData = [
        safeGet(rawStats.byRole, 'Student'),
        safeGet(rawStats.byRole, 'Teacher'),
        safeGet(rawStats.byRole, 'Admin')
    ];

    // Data Status (ĐÃ BỔ SUNG)
    const statusData = [
        safeGet(rawStats.byStatus, 'active'),
        safeGet(rawStats.byStatus, 'deactive') // Hoặc 'inactive' tùy key trong DB của bạn
    ];

    // Data Growth
    const growthMap = rawStats.studentGrowth || {};
    const growthLabels = Object.keys(growthMap).sort();
    const growthValues = growthLabels.map(year => growthMap[year]);

    // --- 2. CHART CONFIG ---
    Chart.defaults.font.family = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif";
    Chart.defaults.color = '#8C8C8C';

    // A. ROLE CHART
    new Chart(document.getElementById('roleChart'), {
        type: 'doughnut',
        data: {
            labels: ['Học sinh', 'Giáo viên', 'Admin'],
            datasets: [{
                data: roleData,
                backgroundColor: ['#1890FF', '#13C2C2', '#F0F2F5'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '70%',
            plugins: { legend: { position: 'right', labels: { boxWidth: 12 } } }
        }
    });

    // B. STATUS CHART (ĐÃ BỔ SUNG)
    new Chart(document.getElementById('statusChart'), {
        type: 'pie', // Dùng Pie để khác biệt chút so với Doughnut ở trên
        data: {
            labels: ['Hoạt động', 'Vô hiệu hóa'],
            datasets: [{
                data: statusData,
                backgroundColor: [
                    '#52C41A', // Xanh lá (Success) - Rất hợp với nền xanh dương
                    '#FF4D4F'  // Đỏ (Error/Stop)
                ],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right', labels: { boxWidth: 12 } }
            }
        }
    });

    // C. GROWTH CHART
    const ctxGrowth = document.getElementById('growthChart').getContext('2d');
    let gradient = ctxGrowth.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(24, 144, 255, 0.2)');
    gradient.addColorStop(1, 'rgba(24, 144, 255, 0.0)');

    new Chart(ctxGrowth, {
        type: 'line',
        data: {
            labels: growthLabels,
            datasets: [{
                label: 'Học sinh mới',
                data: growthValues,
                borderColor: '#1890FF',
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointRadius: 4,
                pointBackgroundColor: '#fff',
                pointBorderColor: '#1890FF',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: true, grid: { borderDash: [5, 5] } }
            }
        }
    });
});