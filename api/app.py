from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import time
import database

SQR_M_PERSON = 4.0

app = Flask(__name__)



@app.route('/<float:room_dim>', methods = ['GET'])
def max_cap(room_dim):

    max_capacity = int(room_dim / SQR_M_PERSON)
    time.sleep(3)
    return jsonify(

        {'max_cap': max_capacity}

        )


@app.route('/<string:username>', methods = ['GET'])
def login(username):
	if (username != "favicon.ico"):
		db = database.Database()
		password = db.selectUser(username)
		db.close()
		return jsonify({'password':password})
	else:
		return jsonify({'error':'favicon'})


@app.route('/image', methods = ['POST'])
def read_image():
	json = request.json
	content = base64.b64decode(json['image'])
	filename = json['filename']
	rotation = json['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 -90), expand=True)
	image.save("images/"+filename+".jpeg")
	return jsonify({'prueba':'Imagen guardada'})


@app.route('/register', methods = ['POST'])
def register():
	json = request.json
	username = json['username']
	password = json['password']
	db = database.Database()
	db.registerUser(username, password)
	db.close()
	return jsonify({'register':'successful register'})

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', ssl_context = ('ssl/cert2.pem', 'ssl/key2.pem'))
