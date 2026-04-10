import json
import random
import os
import re
from collections import defaultdict

cwd = "/home/lequangminh/Documents/Thesis/CareerGuidanceSystem/careerorientation/CrawlData"

knowledge_files = ["interest.jsonl", "occupation.jsonl", "ksa_details.jsonl", "Q&A.jsonl"]
score_file = "score.jsonl"

final_train = []
final_val = []

random.seed(42)

# Hàm làm sạch dòng đảm bảo có newline duy nhất ở cuối
def clean_lines(line_list):
    cleaned = []
    for l in line_list:
        s = l.strip()
        if s:
            cleaned.append(s + "\n")
    return cleaned

# 1. Xử lý nhóm Kiến thức (Nhân bản x3)
knowledge_pool = []
for filename in knowledge_files:
    path = os.path.join(cwd, filename)
    if os.path.exists(path):
        with open(path, 'r', encoding='utf-8-sig') as f:
            lines = clean_lines(f.readlines())
            knowledge_pool.extend(lines * 3)
            print(f"Đã nạp {filename}: {len(lines)} gốc -> {len(lines)*3} sau nhân bản")

# 2. Xử lý nhóm Điểm số (Bốc cân bằng 17 nhãn)
if os.path.exists(os.path.join(cwd, score_file)):
    with open(os.path.join(cwd, score_file), 'r', encoding='utf-8-sig') as f:
        score_lines = clean_lines(f.readlines())
    
    career_groups = defaultdict(list)
    for line in score_lines:
        try:
            data = json.loads(line)
            response_text = data['contents'][1]['parts'][0]['text']
            match = re.search(r"Nghề nghiệp định hướng:\s*(.*?)\.", response_text)
            if match:
                career_label = match.group(1).strip()
                career_groups[career_label].append(line)
            else:
                career_groups["Unknown"].append(line)
        except Exception:
            continue

    print(f"Tìm thấy {len(career_groups)} nhãn nghề nghiệp khác nhau.")

    target_total = 1200
    num_labels = len(career_groups)
    target_per_label = target_total // num_labels
    
    selected_scores = []
    for label, lines in career_groups.items():
        sample_size = min(len(lines), target_per_label)
        selected_scores.extend(random.sample(lines, sample_size))
        print(f" - Nhãn '{label}': bốc {sample_size}/{len(lines)}")
    
    if len(selected_scores) < target_total:
        remaining = list(set(score_lines) - set(selected_scores))
        additional = random.sample(remaining, min(target_total - len(selected_scores), len(remaining)))
        selected_scores.extend(additional)
        print(f"Đã bốc thêm {len(additional)} dòng để đạt mục tiêu {target_total} dòng.")

    print(f"Tổng số dòng score đã chọn: {len(selected_scores)}")
else:
    selected_scores = []

# Gộp và trộn
all_data = knowledge_pool + selected_scores
random.shuffle(all_data)

# 3. Chia Train/Val (5%)
val_count = int(len(all_data) * 0.05)
final_val = all_data[:val_count]
final_train = all_data[val_count:]

# Lưu file (Đảm bảo ghi chuẩn)
with open(os.path.join(cwd, "fine_tune.jsonl"), 'w', encoding='utf-8') as f:
    f.writelines(final_train)

with open(os.path.join(cwd, "validation.jsonl"), 'w', encoding='utf-8') as f:
    f.writelines(final_val)

print(f"\n--- KẾT QUẢ CÂN BẰNG THÔNG MINH (ĐÃ FIX LỖI JSON) ---")
print(f"Tổng số mẫu: {len(all_data)}")
print(f"Số mẫu Train: {len(final_train)}")
print(f"Số mẫu Validation: {len(final_val)}")
