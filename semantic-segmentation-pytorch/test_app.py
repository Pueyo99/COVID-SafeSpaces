from flask import Flask, jsonify, request
from PIL import Image
import base64
from io import BytesIO
import datetime
import os
import time
import json
import threading
from pre_process_image import resize_image

import database
import mail
import random
import string

app = Flask(__name__)

def count_people(filename):
	os.system("python3 object_counter/images.py --images object_counter/images/"+filename+".jpg")

def detect_masks(filename):
	os.system("python3 mask_detection/demo.py -n prn -i mask_detection/images/"+filename+".jpg")

@app.route('/<float:room_dim>', methods = ['GET'])
def max_cap(room_dim):
    SQR_M_PERSON = 4.0
    max_capacity = int(room_dim / SQR_M_PERSON)
    return jsonify(

        {'max_cap': max_capacity}

        )
#Parte APP
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

@app.route('/verify',methods = ['GET'])
def verify():
	username = request.args.get('username')
	try:
		db = database.Database()
		output = db.selectUnverified(username)
		print(output)
		db.close()
		return jsonify(output)
	except Exception:
		return jsonify({'verification':'user not verified'})

@app.route('/register', methods = ['POST'])
def register():
	json = request.json
	username = json['username']
	password = json['password']
	email = json['mail']
	db = database.Database()
	db.registerUnverified(username, password,email)
	db.close()
	mailSender = mail.Mail('paeaccenture@gmail.com','PAEAccenture-1',email,password)
	mailSender.writeVerification(username)
	mailSender.send()
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
	password = getRandomPassword()
	print(password)
	try:
		db =  database.Database()
		db.updatePassword(username, password)
		email = db.recover(username)[0]
		print(mail)
		db.close()
		mailSender = mail.Mail('paeaccenture@gmail.com','PAEAccenture-1',email,password)
		mailSender.writeRecovery()
		mailSender.send()
		return jsonify({'recover':'Password sended'})
	except Exception as ex:
		print(ex)
		traceback.print_exc()
		return  jsonify({'error':'Non-existent username'})

def getRandomPassword():
	password_characters = string.ascii_letters + string.digits + string.punctuation
	password = ''.join(random.choice(password_characters) for i in range(15))
	return password

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


#Fin Part APP


@app.route('/window', methods = ['POST'])
def compute_window():
	r = request.json
	content = base64.b64decode(r['image'])
	filename = r['filename']
	rotation = r['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 -90), expand=True)
	image = resize_image(image)

	image.save("test_images/"+filename+".jpg")

	# Run test.py
	os.system("python3 -u test.py --imgs test_images/" +filename+ ".jpg --gpu 0 --cfg config/ade20k-resnet50dilated-ppm_deepsup.yaml")
	
	while not os.path.exists("json_results/"+filename+".json"):
		time.sleep(5)
	json_file = json.loads(open("json_results/"+filename+".json").read())
	return jsonify(json_file)

@app.route('/mask', methods = ['POST'])
def compute_mask():
	r = request.json
	content = base64.b64decode(r['image'])
	filename = r['filename']
	rotation = r['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 - 90), expand=True)
	image = resize_image(image)

	image.save("mask_detection/images/"+filename+".jpg")

	#Run demo.py
	os.system("python3 mask_detection/demo.py -n prn -i mask_detection/images/"+filename+".jpg")
	
	while not os.path.exists("mask_detection/json_results/"+filename+".json"):
		time.sleep(5)
	json_file = json.loads(open("mask_detection/json_results/"+filename+".json").read())
	
	return jsonify(json_file)

@app.route('/people', methods = ['POST'])
def people_masks():
	r = request.json
	content = base64.b64decode(r['image'])
	filename = r['filename']
	rotation = r['rotation']
	print(rotation)
	image = Image.open(BytesIO(content)).rotate((rotation*90 - 90), expand=True)
	image = resize_image(image)

	image.save("object_counter/images/"+filename+".jpg")
	image.save("mask_detection/images/"+filename+".jpg")

	#t_people = threading.Thread(target=count_people, args=filename, daemon=True)
	#t_masks = threading.Thread(target=detect_masks, args=filename, daemon=True)

	os.system("python3 object_counter/images.py --images object_counter/images/"+filename+".jpg")
	os.system("python3 mask_detection/demo.py -n prn -i mask_detection/images/"+filename+".jpg")

	while not os.path.exists("object_counter/json_results/"+filename+".json") or not os.path.exists("mask_detection/json_results/"+filename+".json"):
		time.sleep(2)
		print("Esperando al fichero: "+filename+".json")

	num_people = json.loads(open("object_counter/json_results/"+filename+".json").read())
	masks_found = json.loads(open("mask_detection/json_results/"+filename+".json").read())
	num_people.update(masks_found)
	print(num_people)

	return jsonify(num_people)
     

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8999, ssl_context = ('ssl/certpae.pem', 'ssl/keypae.pem'))
