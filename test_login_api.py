import requests

url = 'http://172.30.1.11:8090/api/comm/login'
data = {'cmpycd': 'COIT', 'userid': 'coit', 'passwd': 'smart'}
try:
    res = requests.post(url, json=data)
    print("Status:", res.status_code)
    print("Response:", res.text)
except Exception as e:
    print("Error:", e)
