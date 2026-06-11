import re
import requests
import pandas as pd
from bs4 import BeautifulSoup
from datetime import datetime

URL = "http://www.jpvs.org/menu-restaurant/index.html#kanto"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

VEGE_TYPE_SWITCH = {
    "v.gif": "VEGETARIAN",  # ベジタリアンのお店
    "norm.gif": "VEGAN_FRIENDLY",  # ベジタリアン料理もあるお店
    "fish.gif": "PESCATARIAN",  # 肉を使わず魚の料理があるお店
}


def _normalize_spaces(text):
    """
    [Internal Utility] Standardize brackets and compress all types of spaces.
    """
    if not text:
        return ""

    text = text.replace("（", "(").replace("）", ")")
    # Convert full-width to half-width
    text = text.replace("　", " ")

    # Compress continuous half-width spaces
    while "  " in text:
        text = text.replace("  ", " ")
    return text.strip()


def clean_html_cell(cell_tag, replacement):
    """
    Clean the HTML cell string,
    removing br label, newlines, and continuous spaces.
    """
    if not isinstance(replacement, str):
        raise TypeError(f"replacement must be str, "
                        f"but it is {type(replacement)}")
    if not cell_tag:
        return ""

    # remove <br> and space from terminals
    br_tags = cell_tag.find_all("br")
    for br in br_tags:
        br.replace_with("")
    content = cell_tag.text.strip()
    content = content.replace("\n", replacement).replace("\r", replacement)

    return _normalize_spaces(content.strip())


def clean_note_comment(raw_alt_text):
    """
    reform comment message.
    """
    if not raw_alt_text or not isinstance(raw_alt_text, str):
        return ""

    cleaned = re.sub(r"■\s*コメント[\s：■]*", "", raw_alt_text)
    cleaned = cleaned.replace('"', "").replace("“", "").replace("”", "")
    cleaned = cleaned.replace("\n", "、").replace("\r", "、")

    cleaned = _normalize_spaces(cleaned)

    while "、、" in cleaned:
        cleaned = cleaned.replace("、、", "、")

    cleaned = cleaned.strip().strip("、").strip("。")

    return cleaned


try:
    response = requests.get(URL, headers=HEADERS, timeout=10)
    if response.status_code != 200:
        raise requests.HTTPError(
            f"Request failed with status code {response.status_code}"
        )
    response.encoding = "utf-8"

    soup = BeautifulSoup(response.text, "html.parser")
    current_timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    all_restaurants = []

    elements = soup.find_all(["h2", "h3", "table"])
    area_achor = ""

    for element in elements:

        if element.name == "h2":
            area_achor = element.find("a")

        elif element.name == "h3":
            area_achor = element.find("a")

        elif element.name == "table":
            area_name = "unknown"
            if area_achor and area_achor.has_attr("id"):
                area_name = area_achor["id"].strip()
            print(f"Processing area: {area_name}")

            rows = element.find_all("tr")

            for row in rows:
                cells = row.find_all("td")

                if not cells or len(cells) < 6:
                    continue

                city = cells[0].text.strip()
                # skip table titles and empty layout rows
                if re.search(r"地\s*域", city):
                    continue

                name = clean_html_cell(cells[2], " ")
                if not name:
                    continue

                img_tag = cells[1].find("img")
                vege_type = "UNKNOWN"
                note_comment = ""

                if img_tag and not isinstance(img_tag, int):
                    if img_tag.has_attr("src"):
                        gif_name = img_tag["src"].split("/")[-1]
                        vege_type = VEGE_TYPE_SWITCH.get(gif_name, "UNKNOWN")

                    if img_tag.has_attr("alt"):
                        note_comment = clean_note_comment(img_tag["alt"])

                style = clean_html_cell(cells[3], "、")
                phone = cells[4].text.strip()
                address = clean_html_cell(cells[5], " ")

                all_restaurants.append(
                    {
                        "area_name": area_name,  # 都道県府
                        "city": city,  # 地域
                        "veg_type": vege_type,  # 類
                        "raw_name": name,
                        "raw_style": style,  # 料理
                        "raw_phone": phone,
                        "raw_address": address,
                        "raw_comment": note_comment,  # 類(コメント)
                        "scraped_at": current_timestamp,
                    }
                )

    df = pd.DataFrame(all_restaurants)
    df.to_csv("certified_restaurants.csv", index=False, encoding="utf-8-sig")
    print(f"Scraping completed. "
          f"Total {len(all_restaurants)} restaurants extracted.")

except Exception as e:
    print(f"Exception occurred: {e}")
