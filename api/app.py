from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import time
import os
import database
import mail


SQR_M_PERSON = 4.0

app = Flask(__name__)



@app.route('/<float:room_dim>', methods = ['GET'])
def max_cap(room_dim):

    max_capacity = int(room_dim / SQR_M_PERSON)
    time.sleep(3)
    return jsonify(

        {'max_cap': max_capacity}

        )


@app.route('/login', methods = ['GET'])
def login():
	username = request.args.get('username')
	print(username)
	try:
		db = database.Database()
		password = db.selectUser(username)
		db.close()
		return jsonify({'password':password})
	except Exception:
		return jsonify({'error':'Not such username'})


@app.route('/building', methods = ['GET'])
def getBuilding():
	username = request.args.get('username')
	try:
		db = database.Database()
		buildings = db.getBuildings(username)
		db.close()
		return buildings
	except Exception:
		return jsonify({'error':''})


@app.route('/room', methods = ['GET'])
def getRoom():
	username = request.args.get('username')
	building = request.args.get('building')
	try:
		db = database.Database()
		rooms = db.getRooms(username, building)
		db.close()
		return rooms
	except Exception:
		return jsonify({'error':''})

@app.route('/capacity', methods = ['GET'])
def getCapacity():
	username = request.args.get('username')
	building = request.args.get('building')
	room = request.args.get('room')
	try:
		db = database.Database()
		capacity = db.getCapacity(username, building, room)
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
	mail = json['mail']
	db = database.Database()
	db.registerUser(username, password,mail)
	db.close()
	return jsonify({'register':'successful register'})

@app.route('/update', methods = ['POST'])
def updatePassword():
	json = request.json
	username = json['username']
	password = json['password']
	db = database.Database()
	db.updatePassword(username,password)
	db.close()
	return jsonify({'update':'Password successfully updated'})



@app.route('/recover',methods = ['GET'])
def recoverPassword():
	username = request.args.get('username')
	print(username)
	try:
		db =  database.Database()
		data = db.recover(username)
		print(data)
		db.close()
		mailSender = mail.Mail('paeaccenture@gmail.com','PAEAccenture-1',data[0],data[1])
		return jsonify({'recover':'Password sended'})
	except Exception as ex:
		print(ex)
		return  jsonify({'error':'Non-existent username'})

@app.route('/profile',methods = ['GET'])
def getProfileInfo():
	username = request.args.get('username')
	print(username)
	try:
		db =  database.Database()
		data = db.recover(username)
		print(data)
		db.close()
		return jsonify({'mail':data[0],'password':data[1]})
	except Exception as ex:
		print(ex)
		return  jsonify({'error':'Non-existent username'})



if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8999, ssl_context = ('ssl/certpae.pem', 'ssl/keypae.pem'))


