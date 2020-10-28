from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import os
import time
import json

app = Flask(__name__)

@app.route('/<float:room_dim>', methods = ['GET'])
def max_cap(room_dim):
    SQR_M_PERSON = 4.0
    max_capacity = int(room_dim / SQR_M_PERSON)
    return jsonify(

        {'max_cap': max_capacity}

        )


@app.route('/image', methods = ['POST'])
def read_image():
	r = request.json
	content = base64.b64decode(r['image'])
	filename = r['filename']
	rotation = r['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 -90), expand=True)
	image.save("test_images/"+filename+".jpg")

	# Run test.py
	os.system("python3 -u test.py --imgs test_images/" +filename+ ".jpg --gpu 0 --cfg config/ade20k-resnet50dilated-ppm_deepsup.yaml")
	
	while not os.path.exists("json_results/"+filename+".json"):
		time.sleep(5)
	json_file = json.loads(open("json_results/"+filename+".json"))
	return jsonify(json_file)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8999)
