from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import os
import time

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
	json = request.json
	content = base64.b64decode(json['image'])
	filename = json['filename']
	rotation = json['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 -90), expand=True)
	image.save("test_images/"+filename+".jpg")

	# Run test.py
	os.system("python3 -u test.py --imgs test_images/" +filename+ ".jpg --gpu 0 --cfg config/ade20k-resnet50dilated-ppm_deepsup.yaml")
	
	#time.sleep(10)
	#json_file = open("json_results/"+filename+".json")
	#return json_file
	return jsonify({"Outcome": "Success"})


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8999)
