import json
import random

def merge_datasets():
    # 1. Các file cơ sở (lấy toàn bộ)
    base_files = [
        'interest.jsonl',
        'ksa_details.jsonl',
        'occupation.jsonl',
        'paper.jsonl',
        'Q&A.jsonl'
    ]
    
    target_careers = [
        'Bác sĩ', 'Chủ doanh nghiệp', 'Giáo viên', 'Kế toán', 'Kỹ sư phần mềm',
        'Kỹ sư xây dựng', 'Luật sư', 'Nghiên cứu mạng xã hội', 'Nghệ sĩ', 
        'Nhà khoa học', 'Nhà phát triển bất động sản', 'Nhà phát triển game', 
        'Nhà thiết kế', 'Nhà văn', 'Nhà đầu tư chứng khoán', 'Nhân viên ngân hàng', 
        'Viên chức nhà nước'
    ]

    all_data = []

    # 1. Đọc dữ liệu từ các file cơ sở
    for filename in base_files:
        try:
            with open(filename, 'r', encoding='utf-8') as f:
                lines = [line.strip() for line in f if line.strip()]
                all_data.extend(lines)
            print(f"Đã đọc {len(lines)} dòng từ {filename}")
        except FileNotFoundError:
            print(f"Không tìm thấy file {filename}")

    # 2. Chọn 11 mẫu cho mỗi ngành từ score.jsonl
    selection = {career: [] for career in target_careers}
    try:
        with open('score.jsonl', 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line: continue
                try:
                    data = json.loads(line)
                    model_text = data['contents'][1]['parts'][0]['text']
                    for career in target_careers:
                        if f'Nghề nghiệp định hướng: {career}' in model_text:
                            if len(selection[career]) < 11:
                                selection[career].append(line)
                            break
                except:
                    continue
        
        score_data = []
        for career, samples in selection.items():
            if len(samples) < 11:
                print(f"CẢNH BÁO: Chỉ tìm thấy {len(samples)} mẫu cho ngành {career}")
            score_data.extend(samples)
            
        print(f"Đã trích xuất {len(score_data)} dòng cân bằng từ score.jsonl")
        all_data.extend(score_data)
        
    except FileNotFoundError:
        print("Không tìm thấy file score.jsonl")

    # 3. Tráo ngẫu nhiên
    random.seed(42) # Để kết quả có thể tái lập
    random.shuffle(all_data)
    
    # 4. Chia 95% Train - 5% Validation
    total_samples = len(all_data)
    split_index = int(total_samples * 0.95)
    train_data = all_data[:split_index]
    val_data = all_data[split_index:]
    
    # 5. Ghi ra file
    with open('fine-tune.jsonl', 'w', encoding='utf-8') as f:
        for line in train_data:
            f.write(line + '\n')
            
    with open('validation.jsonl', 'w', encoding='utf-8') as f:
        for line in val_data:
            f.write(line + '\n')
            
    print(f"\nHoàn tất!")
    print(f"Tổng số mẫu: {total_samples}")
    print(f"Số mẫu Train: {len(train_data)}")
    print(f"Số mẫu Validation: {len(val_data)}")

if __name__ == "__main__":
    merge_datasets()
