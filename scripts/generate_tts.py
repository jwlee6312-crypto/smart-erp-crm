import sys
import os
import subprocess
import urllib.parse

def generate_tts(script_id, text):
    # 1. 파일명 및 경로 설정
    output_path = f"/var/lib/asterisk/sounds/custom/{script_id}.wav"
    temp_mp3 = f"/tmp/{script_id}.mp3"

    # 2. Google Translate TTS URL 생성
    encoded_text = urllib.parse.quote(text)
    url = f"https://translate.google.com/translate_tts?ie=UTF-8&q={encoded_text}&tl=ko&client=tw-ob"

    # 3. MP3 다운로드
    try:
        subprocess.run(["curl", "-L", "-A", "Mozilla/5.0", "-o", temp_mp3, url], check=True)
    except Exception as e:
        print(f"Error downloading TTS: {e}")
        return

    # 4. FFmpeg을 이용한 Asterisk 규격 변환 (WAV, 8kHz, Mono, 16-bit)
    try:
        # -y: 덮어쓰기, -ar 8000: 샘플링 레이트, -ac 1: 모노
        subprocess.run([
            "ffmpeg", "-y", "-i", temp_mp3,
            "-ar", "8000", "-ac", "1", "-acodec", "pcm_s16le",
            output_path
        ], check=True)
        print(f"Successfully generated: {output_path}")
    except Exception as e:
        print(f"Error converting audio: {e}")
    finally:
        if os.path.exists(temp_mp3):
            os.remove(temp_mp3)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 generate_tts.py <script_id> <text>")
    else:
        generate_tts(sys.argv[1], sys.argv[2])
