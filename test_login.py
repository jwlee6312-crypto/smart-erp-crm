import urllib.request
import json
import http.cookiejar

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))

login_data = json.dumps({
    "cmpycd": "COIT",
    "userid": "coit",
    "passwd": "smart"
}).encode('utf-8')

req = urllib.request.Request(
    "http://172.30.1.11:8090/api/comm/login",
    data=login_data,
    headers={'Content-Type': 'application/json'}
)

try:
    with opener.open(req) as response:
        print("Status:", response.status)
        print("Body:", response.read().decode('utf-8'))
        print("Cookies:", [c.name + "=" + c.value for c in cj])
except Exception as e:
    print("Error:", e)
