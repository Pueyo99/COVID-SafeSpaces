from flask import request
import requests

url = 'http://127.0.0.1:5000/image'
my_img = {'image': open('panda.jpg', 'rb')}
r = requests.post(url, files=my_img)
print(r.json())