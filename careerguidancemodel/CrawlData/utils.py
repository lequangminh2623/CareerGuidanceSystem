import undetected_chromedriver as uc
import json
import os

def setup_driver(headless=True, version_main=146):
    """
    Cấu hình và khởi tạo trình điều khiển Chrome.
    """
    options = uc.ChromeOptions()
    if headless:
        options.add_argument("--headless")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    
    # Sử dụng version_main mặc định là 146 như trong code gốc
    driver = uc.Chrome(version_main=version_main, options=options)
    return driver

def save_to_jsonl(data, output_file):
    """
    Lưu dữ liệu vào file JSONL.
    """
    with open(output_file, 'a', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False)
        f.write('\n')

def ensure_dir(file_path):
    """
    Đảm bảo thư mục chứa file tồn tại.
    """
    directory = os.path.dirname(file_path)
    if directory and not os.path.exists(directory):
        os.makedirs(directory)
