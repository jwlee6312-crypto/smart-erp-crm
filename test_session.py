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
opener.open(req)

req_session = urllib.request.Request("http://172.30.1.11:8090/api/comm/session")
with opener.open(req_session) as resp:
    print("Session API status:", resp.status)
    print("Session API body:", resp.read().decode('utf-8'))
