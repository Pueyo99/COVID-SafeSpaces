from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import time
import os
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
		try:
			db = database.Database()
			password = db.selectUser(username)
			db.close()
			return jsonify({'password':password})
		except Exception:
			return jsonify({'error':'Not such username'})
	else:
		return jsonify({'error':'favicon'})

@app.route('/building', methods = ['GET'])
def getBuilding():
	try:
		db = database.Database()
		buildings = db.getBuildings()
		db.close()
		return buildings
	except Exception:
		return jsonify({'error':''})


@app.route('/room/<string:building>', methods = ['GET'])
def getRoom(building):
	try:
		db = database.Database()
		rooms = db.getRooms(building)
		db.close()
		return rooms
	except Exception:
		return jsonify({'error':''})

@app.route('/capacity/<string:building>/<string:room>', methods = ['GET'])
def getCapacity(building, room):
	try:
		db = database.Database()
		capacity = db.getCapacity(building, room)
		db.close()
		return capacity
	except Exception:
		return jsonify({'error':''})

@app.route('/image', methods = ['POST'])
def read_image():
	json = request.json
	content = base64.b64decode(json['image'])
	filename = json['filename']
	rotation = json['rotation']
	username = json['username']
	print(username)
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 -90), expand=True)
	if not os.path.exists("images/{}/".format(username)):
		os.makedirs("images/{}/".format(username))
	image.save("images/"+username+"/"+filename+".jpeg")
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
    app.run(debug=True, host='0.0.0.0', ssl_context = ('ssl/certpae.pem', 'ssl/keypae.pem'))
