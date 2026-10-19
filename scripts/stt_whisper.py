import whisper
import sys
import json
import warnings
import os
import subprocess

warnings.filterwarnings("ignore")

def get_channels(file_path):
    """파일의 채널 수 확인 (ffprobe 활용)"""
    try:
        cmd = ["ffprobe", "-v", "error", "-show_entries", "stream=channels", "-of", "compact=p=0:nk=1", file_path]
        res = subprocess.check_output(cmd).decode().strip()
        return int(res) if res else 1
    except: return 1

def transcribe_hybrid(file_path):
    if not os.path.exists(file_path):
        print(json.dumps({"success": False, "error": "File not found"}))
        return

    try:
        model = whisper.load_model("base")
        channels = get_channels(file_path)

        if channels >= 2:
            # 🚀 [스테레오 모드] 물리적 채널 분리 가동
            left_tmp = f"{file_path}.L.wav"
            right_tmp = f"{file_path}.R.wav"
            # 💡 최신 ffmpeg 방식의 정석 채널 분리
            subprocess.run(["ffmpeg", "-y", "-i", file_path, "-map_channel", "0.0.0", left_tmp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            subprocess.run(["ffmpeg", "-y", "-i", file_path, "-map_channel", "0.0.1", right_tmp], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

            cust_text = model.transcribe(left_tmp, language="ko")["text"].strip()
            agent_text = model.transcribe(right_tmp, language="ko")["text"].strip()

            if os.path.exists(left_tmp): os.remove(left_tmp)
            if os.path.exists(right_tmp): os.remove(right_tmp)

            output = {
                "success": True, "mode": "stereo",
                "trb_ment": cust_text, "ans_ment": agent_text,
                "text": f"고객: {cust_text}\n상담원: {agent_text}"
            }
        else:
            # 🚀 [모노 모드] 통합 추출 후 AI 분석으로 토스
            res = model.transcribe(file_path, language="ko")
            raw_text = res["text"].strip()
            output = {
                "success": True, "mode": "mono",
                "trb_ment": raw_text, "ans_ment": "", "text": raw_text
            }

        print(json.dumps(output, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({"success": False, "error": str(e)}, ensure_ascii=False))

if __name__ == "__main__":
    if len(sys.argv) < 2: sys.exit(1)
    else: transcribe_hybrid(sys.argv[1])
