/**
 * 60 câu hỏi trắc nghiệm Holland (RIASEC) — dựa trên O*NET Interest Profiler.
 * Mỗi nhóm RIASEC có 10 câu hỏi. Thang điểm Likert 1–5.
 */

export type HollandCategory = 'R' | 'I' | 'A' | 'S' | 'E' | 'C';

export interface HollandQuestion {
    id: number;
    text: string;
    category: HollandCategory;
}

export const HOLLAND_CATEGORY_LABELS: Record<HollandCategory, { vi: string; en: string; color: string }> = {
    R: { vi: 'Thực tế', en: 'Realistic', color: '#ef4444' },
    I: { vi: 'Nghiên cứu', en: 'Investigative', color: '#3b82f6' },
    A: { vi: 'Nghệ thuật', en: 'Artistic', color: '#a855f7' },
    S: { vi: 'Xã hội', en: 'Social', color: '#10b981' },
    E: { vi: 'Quản lý', en: 'Enterprising', color: '#f59e0b' },
    C: { vi: 'Nghiệp vụ', en: 'Conventional', color: '#06b6d4' },
};

export const LIKERT_OPTIONS = [
    { value: 1, label: 'Rất không thích' },
    { value: 2, label: 'Không thích' },
    { value: 3, label: 'Không chắc' },
    { value: 4, label: 'Thích' },
    { value: 5, label: 'Rất thích' },
];

export const HOLLAND_QUESTIONS: HollandQuestion[] = [
    // ═══ R — Realistic (Thực tế) ═══
    { id: 1, text: 'Sửa chữa đồ điện trong nhà', category: 'R' },
    { id: 2, text: 'Lắp ráp hoặc sửa máy tính', category: 'R' },
    { id: 3, text: 'Lái xe máy hoặc ô tô đường dài', category: 'R' },
    { id: 4, text: 'Trồng cây hoặc chăm sóc vườn', category: 'R' },
    { id: 5, text: 'Vận hành máy móc hoặc thiết bị công nghiệp', category: 'R' },
    { id: 6, text: 'Sửa xe đạp hoặc xe máy', category: 'R' },
    { id: 7, text: 'Làm mộc hoặc thủ công mỹ nghệ', category: 'R' },
    { id: 8, text: 'Xây dựng hoặc lắp đặt công trình', category: 'R' },
    { id: 9, text: 'Làm việc ngoài trời (nông nghiệp, thủy sản)', category: 'R' },
    { id: 10, text: 'Sử dụng dụng cụ cơ khí, hàn, tiện', category: 'R' },

    // ═══ I — Investigative (Nghiên cứu) ═══
    { id: 11, text: 'Nghiên cứu một đề tài khoa học', category: 'I' },
    { id: 12, text: 'Giải các bài toán phức tạp', category: 'I' },
    { id: 13, text: 'Đọc sách hoặc tài liệu khoa học', category: 'I' },
    { id: 14, text: 'Phân tích dữ liệu và tìm ra quy luật', category: 'I' },
    { id: 15, text: 'Thực hiện thí nghiệm trong phòng lab', category: 'I' },
    { id: 16, text: 'Tìm hiểu cách hoạt động của cơ thể người', category: 'I' },
    { id: 17, text: 'Viết chương trình máy tính hoặc lập trình', category: 'I' },
    { id: 18, text: 'Nghiên cứu về vũ trụ và thiên văn học', category: 'I' },
    { id: 19, text: 'Điều tra nguyên nhân của một vấn đề', category: 'I' },
    { id: 20, text: 'Thiết kế thí nghiệm để kiểm chứng giả thuyết', category: 'I' },

    // ═══ A — Artistic (Nghệ thuật) ═══
    { id: 21, text: 'Vẽ tranh hoặc thiết kế đồ họa', category: 'A' },
    { id: 22, text: 'Chơi nhạc cụ hoặc hát', category: 'A' },
    { id: 23, text: 'Viết truyện ngắn hoặc thơ', category: 'A' },
    { id: 24, text: 'Chụp ảnh nghệ thuật hoặc quay phim', category: 'A' },
    { id: 25, text: 'Thiết kế trang phục hoặc thời trang', category: 'A' },
    { id: 26, text: 'Diễn kịch hoặc đóng phim', category: 'A' },
    { id: 27, text: 'Trang trí nội thất hoặc sắp đặt không gian', category: 'A' },
    { id: 28, text: 'Sáng tác nhạc hoặc viết lời bài hát', category: 'A' },
    { id: 29, text: 'Làm video sáng tạo cho mạng xã hội', category: 'A' },
    { id: 30, text: 'Thiết kế poster, logo hoặc website', category: 'A' },

    // ═══ S — Social (Xã hội) ═══
    { id: 31, text: 'Giúp đỡ bạn bè giải quyết vấn đề cá nhân', category: 'S' },
    { id: 32, text: 'Dạy kèm hoặc hướng dẫn người khác học', category: 'S' },
    { id: 33, text: 'Tham gia hoạt động tình nguyện', category: 'S' },
    { id: 34, text: 'Chăm sóc người ốm hoặc người già', category: 'S' },
    { id: 35, text: 'Tổ chức hoạt động nhóm hoặc team building', category: 'S' },
    { id: 36, text: 'Lắng nghe và tư vấn cho người khác', category: 'S' },
    { id: 37, text: 'Làm việc trong lĩnh vực y tế, sức khỏe', category: 'S' },
    { id: 38, text: 'Hòa giải mâu thuẫn giữa mọi người', category: 'S' },
    { id: 39, text: 'Tham gia các hoạt động cộng đồng', category: 'S' },
    { id: 40, text: 'Truyền đạt kiến thức cho người khác', category: 'S' },

    // ═══ E — Enterprising (Quản lý) ═══
    { id: 41, text: 'Thuyết phục người khác mua sản phẩm', category: 'E' },
    { id: 42, text: 'Lãnh đạo một nhóm hoặc dự án', category: 'E' },
    { id: 43, text: 'Khởi nghiệp hoặc mở cửa hàng riêng', category: 'E' },
    { id: 44, text: 'Thuyết trình trước đám đông', category: 'E' },
    { id: 45, text: 'Đàm phán và thương lượng hợp đồng', category: 'E' },
    { id: 46, text: 'Lập kế hoạch kinh doanh', category: 'E' },
    { id: 47, text: 'Quản lý ngân sách và tài chính cá nhân', category: 'E' },
    { id: 48, text: 'Tham gia các cuộc thi hùng biện', category: 'E' },
    { id: 49, text: 'Xây dựng mối quan hệ đối tác', category: 'E' },
    { id: 50, text: 'Ra quyết định nhanh trong tình huống áp lực', category: 'E' },

    // ═══ C — Conventional (Nghiệp vụ) ═══
    { id: 51, text: 'Sắp xếp tài liệu và hồ sơ gọn gàng', category: 'C' },
    { id: 52, text: 'Nhập liệu hoặc làm việc với bảng tính Excel', category: 'C' },
    { id: 53, text: 'Lập kế hoạch chi tiết cho công việc', category: 'C' },
    { id: 54, text: 'Kiểm tra lỗi chính tả và ngữ pháp', category: 'C' },
    { id: 55, text: 'Làm sổ sách kế toán, ghi chép thu chi', category: 'C' },
    { id: 56, text: 'Tuân thủ quy trình và hướng dẫn cụ thể', category: 'C' },
    { id: 57, text: 'Quản lý thời gian biểu hàng ngày', category: 'C' },
    { id: 58, text: 'Phân loại và lưu trữ thông tin có hệ thống', category: 'C' },
    { id: 59, text: 'Làm báo cáo hoặc tổng hợp số liệu', category: 'C' },
    { id: 60, text: 'Xử lý thủ tục hành chính, giấy tờ', category: 'C' },
];

/** Calculate RIASEC scores from user answers. Returns { R, I, A, S, E, C } */
export function calculateRIASEC(answers: Record<number, number>): Record<HollandCategory, number> {
    const scores: Record<HollandCategory, number> = { R: 0, I: 0, A: 0, S: 0, E: 0, C: 0 };
    for (const q of HOLLAND_QUESTIONS) {
        if (answers[q.id]) {
            scores[q.category] += answers[q.id];
        }
    }
    return scores;
}

/** Get top 3 Holland codes sorted by score descending */
export function getTop3Codes(scores: Record<HollandCategory, number>): [HollandCategory, HollandCategory, HollandCategory] {
    const sorted = (Object.entries(scores) as [HollandCategory, number][])
        .sort((a, b) => b[1] - a[1]);
    return [sorted[0][0], sorted[1][0], sorted[2][0]];
}
