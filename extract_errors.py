import re
import sys

def extract_errors(log_path):
    with open(log_path, 'rb') as f:
        data = f.read()
    
    # Try different encodings
    encodings = ['utf-16', 'utf-8', 'ascii']
    text = ""
    for enc in encodings:
        try:
            text = data.decode(enc)
            if "error:" in text:
                break
        except:
            continue
    
    if not text:
        text = data.decode('utf-8', errors='ignore')

    lines = text.splitlines()
    for line in lines:
        if "e: " in line:
            print(line)

if __name__ == "__main__":
    extract_errors("build_errors.txt")
