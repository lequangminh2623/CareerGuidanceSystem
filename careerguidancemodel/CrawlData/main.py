import pandas as pd
import os
import time
import random
from selenium.webdriver.common.by import By
from utils import setup_driver, save_to_jsonl

# ==========================================
# 1. CÀO DỮ LIỆU TỪ VOZ
# ==========================================
def crawl_voz(output_file="voz_career_dataset.jsonl", start_page=1, end_page=11):
    base_url = "https://voz.vn/t/chuyen-tro-linh-tinh-tu-van-nghe-nghiep-cho-het-vao-day.1031810/"
    driver = setup_driver(headless=False)
    total_samples = 0
    if os.path.exists(output_file): os.remove(output_file)

    try:
        for page in range(start_page, end_page + 1):
            url = f"{base_url}page-{page}"
            print(f"[*] Đang cào trang {page}: {url}")
            driver.get(url)
            time.sleep(random.uniform(5, 8))
            posts = driver.find_elements(By.CLASS_NAME, "message-inner")
            for post in posts:
                try:
                    quotes = post.find_elements(By.CLASS_NAME, "bbCodeBlock--quote")
                    if quotes:
                        context = quotes[0].text.replace("Click to expand...", "").strip()
                        driver.execute_script("arguments[0].remove();", quotes[0])
                        reply_text = post.find_element(By.CLASS_NAME, "bbWrapper").text.strip()
                        if len(context) > 20 and len(reply_text) > 50:
                            save_to_jsonl({"instruction": f"Dựa trên bối cảnh: '{context}', hãy đưa ra lời khuyên hướng nghiệp phù hợp.", "output": reply_text}, output_file)
                            total_samples += 1
                except Exception: continue
            print(f"[+] Hoàn thành trang {page}. Tổng mẫu: {total_samples}")
            time.sleep(random.uniform(2, 4))
    finally:
        driver.quit()
        print(f"[OK] Crawl VOZ xong! Tổng: {total_samples}")

# ==========================================
# 2. CÀO ĐIỂM CHUẨN
# ==========================================
def scrape_targeted_scores(input_csv="nganh_lien_quan_17_nganh.csv", output_csv="diem_chuan.csv"):
    df_target = pd.read_csv(input_csv)
    target_dict = dict(zip(df_target['Ngành Tiếng Việt'], df_target['Liên quan đến (English Career)']))
    target_names = list(target_dict.keys())
    driver = setup_driver()
    base_url = "https://huongnghiepviet.com/tuyen-sinh/diem-chuan/diem-chuan-theo-nganh-nghe"
    try:
        driver.get(base_url)
        target_urls = {}
        while True:
            time.sleep(2)
            links = driver.find_elements(By.CLASS_NAME, "linktitle_01")
            for link in links:
                name = link.text.replace("Điểm chuẩn ngành ", "").strip()
                if name in target_names and name not in target_urls: target_urls[name] = link.get_attribute("href")
            try:
                next_btn = driver.find_element(By.CSS_SELECTOR, "a[aria-label='Go to next page']")
                driver.execute_script("arguments[0].click();", next_btn)
            except: break

        all_scores = []
        for i, (name, url) in enumerate(target_urls.items()):
            print(f"[{i+1}/{len(target_urls)}] Cào: {name}")
            driver.get(url)
            time.sleep(random.uniform(1.5, 3))
            try:
                school_blocks = driver.find_elements(By.CLASS_NAME, "item-diemchuan-nganh")
                for block in school_blocks:
                    school_name = block.find_element(By.TAG_NAME, "a").text.strip()
                    tables = block.find_elements(By.TAG_NAME, "table")
                    for table in tables:
                        headers = [th.text for th in table.find_elements(By.TAG_NAME, "th")]
                        rows = table.find_elements(By.TAG_NAME, "tr")[1:]
                        for row in rows:
                            cells = row.find_elements(By.TAG_NAME, "td")
                            if len(cells) >= 5:
                                all_scores.append({
                                    "English Career": target_dict[name], "Ngành Tiếng Việt": name, "Trường": school_name,
                                    "Mã ngành": cells[1].text.strip(), "Tổ hợp": cells[3].text.strip(),
                                    "Phương thức": headers[4] if len(headers) > 4 else "N/A",
                                    "Điểm chuẩn": cells[4].text.strip(), "Ghi chú": cells[5].text.strip() if len(cells) > 5 else ""
                                })
            except Exception as e: print(f"Lỗi {name}: {e}")
            if (i + 1) % 10 == 0: pd.DataFrame(all_scores).to_csv("diem_chuan_backup.csv", index=False, encoding="utf-8-sig")
        pd.DataFrame(all_scores).to_csv(output_csv, index=False, encoding="utf-8-sig")
    finally:
        driver.quit()

# ==========================================
# 3. CÀO THÔNG TIN NGÀNH (MÔ TẢ & TRƯỜNG)
# ==========================================
def scrape_major_info(input_csv="nganh_lien_quan_17_nganh.csv", output_csv="tri_thuc_nganh_nghe_full.csv"):
    df_target = pd.read_csv(input_csv)
    target_dict = dict(zip(df_target['Ngành Tiếng Việt'], df_target['Liên quan đến (English Career)']))
    driver = setup_driver()
    base_url = "https://huongnghiepviet.com/nganh-nghe"
    try:
        driver.get(base_url)
        time.sleep(3)
        links = driver.find_elements(By.CLASS_NAME, "linktoarticle_huongnghiep")
        target_urls = {l.text.replace("Ngành ", "").strip(): l.get_attribute("href") for l in links if any(tn.lower() in l.text.lower() for tn in target_dict.keys())}

        all_data = []
        for i, (name, url) in enumerate(target_urls.items()):
            if not url or url == "None": continue
            driver.get(url)
            time.sleep(random.uniform(1.5, 3))
            try:
                desc = driver.find_element(By.CSS_SELECTOR, "section.hnvcontent").text.strip()
                schools = []
                for b in driver.find_elements(By.CSS_SELECTOR, ".list-tuyensinh .d-block.mb-4"):
                    try:
                        s_name = b.find_element(By.TAG_NAME, "b").text.strip()
                        tbl = pd.read_html(b.find_element(By.TAG_NAME, "table").get_attribute('outerHTML'))[0]
                        schools.append(f"--- {s_name} ---\n{tbl.to_string(index=False, header=False)}")
                    except: continue
                all_data.append({"English Career": target_dict.get(name, "N/A"), "Ngành Tiếng Việt": name, "Mô tả": desc, "Danh sách trường": "\n\n".join(schools)})
            except: continue
        pd.DataFrame(all_data).to_csv(output_csv, index=False, encoding="utf-8-sig")
    finally:
        driver.quit()

# ==========================================
# 4. CÀO PHƯƠNG PHÁP CHỌN NGHỀ & HƯỚNG NGHIỆP
# ==========================================
def scrape_articles(base_url, output_csv):
    driver = setup_driver()
    article_links = []
    try:
        driver.get(base_url)
        while True:
            time.sleep(2)
            for t in driver.find_elements(By.CSS_SELECTOR, "h2.contentheading-tin a"):
                href = t.get_attribute("href")
                if href and href not in article_links: article_links.append(href)
            try:
                next_btn = driver.find_element(By.CSS_SELECTOR, "a[aria-label='Go to next page']")
                driver.execute_script("arguments[0].click();", next_btn)
            except: break

        all_data = []
        for i, url in enumerate(article_links):
            driver.get(url)
            time.sleep(random.uniform(1.5, 3))
            try:
                title = driver.find_element(By.TAG_NAME, "h1").text.strip()
                content = driver.find_element(By.CSS_SELECTOR, "section.hnvcontent").text.strip()
                all_data.append({"Tiêu đề": title, "Link gốc": url, "Nội dung": content})
            except: continue
        pd.DataFrame(all_data).to_csv(output_csv, index=False, encoding="utf-8-sig")
    finally: driver.quit()

# ==========================================
# 5. LÀM SẠCH DỮ LIỆU O*NET
# ==========================================
def clean_onet_data(file_type):
    configs = {
        'Knowledge': ('o*net/Knowledge.xlsx', 'Knowledge_Cleaned_For_LLM.csv', "Nghề {row['Title']} cần kiến thức về {row['Element Name']} ({row['Scale Name']}) ở mức {row['Data Value']}."),
        'Abilities': ('o*net/Abilities.xlsx', 'Abilities_Cleaned_For_LLM.csv', "Nghề {row['Title']} đòi hỏi tố chất/năng lực về {row['Element Name']} ({row['Scale Name']}) với điểm số {row['Data Value']}."),
        'Interests': ('o*net/Interests.xlsx', 'Interests_Holland_Synced.csv', "")
    }
    path, out, template = configs[file_type]
    try:
        df = pd.read_excel(path)
        kw = ["Lawyer", "Physician", "Surgeon", "Legislator", "Regulatory Affairs", "Fine Artist", "Public Relations", "Software Developer", "Teacher", "Chief Executive", "Operations Manager", "Scientist", "Financial Manager", "Teller", "Accountant", "Auditor", "Writer", "Author", "Graphic Designer", "Commercial Designer", "Video Game Designer", "Real Estate", "Investment Analyst", "Civil Engineer", "Construction Manager"]
        pat = '|'.join(kw)
        f_df = df[df['Title'].str.contains(pat, case=False, na=False)].copy()
        if file_type == 'Interests':
            h_map = {'Realistic': 'Thực tế (R)', 'Investigative': 'Nghiên cứu (I)', 'Artistic': 'Nghệ thuật (A)', 'Social': 'Xã hội (S)', 'Enterprising': 'Quản lý (E)', 'Conventional': 'Nghiệp vụ (C)'}
            f_df['Holland_Group'] = f_df['Element Name'].map(h_map)
            f_df['Training_Sentence'] = f_df.apply(lambda r: f"Nghề {r['Title']} phù hợp với người có sở thích thuộc nhóm {r['Holland_Group']} (Mức độ phù hợp: {r['Data Value']}/5).", axis=1)
        else:
            f_df['Training_Sentence'] = f_df.apply(lambda row: eval(f'f"{template}"'), axis=1)
        f_df.to_csv(out, index=False, encoding='utf-8-sig')
        print(f"Đã lưu {out}")
    except Exception as e: print(f"Lỗi {file_type}: {e}")

if __name__ == "__main__":
    # crawl_voz()
    # scrape_targeted_scores()
    # scrape_major_info()
    # scrape_articles("https://huongnghiepviet.com/khoa-hoc-huong-nghiep", "phuong_phap_chon_nghe.csv")
    # clean_onet_data('Knowledge')
    print("Chọn hàm cần chạy trong mã nguồn.")

