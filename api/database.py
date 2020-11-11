import sqlite3
import json

class Database:
	def __init__(self):
		self.connection = sqlite3.connect("covid.db")
		self.connection.row_factory = sqlite3.Row
		self.cursor = self.connection.cursor()
		print("Conexión establecida")

	def selectUser(self, username):
		sql = 'SELECT PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()  #Returns a tuple with the user info
			return users['PASSWORD']
		except Exception as ex:
			raise

<<<<<<< HEAD
	def recover(self,username):
		sql = 'SELECT EMAIL,PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()
			return (users['EMAIL'],users['PASSWORD'])
		except Exception as ex:
			raise

=======
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8
	def registerUser(self,username,password, mail):
		values=(username,password,mail)
		sql = 'INSERT INTO USERS VALUES (?,?,?)'
		try:
			self.cursor.execute(sql, values)
			self.connection.commit()
		except Exception as ex:
			raise

	def getBuildings(self, username):
		sql = 'SELECT DISTINCT BUILDING FROM MEASURES WHERE USERNAME=? ORDER BY BUILDING'
		try:
			self.cursor.execute(sql,(username,))
			columns = [column[0] for column in self.cursor.description]
			buildings = []
			for row in self.cursor.fetchall():
				buildings.append(dict(zip(columns,row)))
			return json.dumps(buildings)
		except Exception as ex:
			raise

	def getRooms(self, username, building):
		sql = 'SELECT ROOM FROM MEASURES WHERE USERNAME=? AND BUILDING=? ORDER BY ROOM'
		try:
			self.cursor.execute(sql,(username,building))
			columns = [column[0] for column in self.cursor.description]
			rooms = []
			for row in self.cursor.fetchall():
				rooms.append(dict(zip(columns,row)))
			return json.dumps(rooms)
		except Exception as ex:
			raise

	def getCapacity(self, username, building, room):
		sql = 'SELECT CAPACITY FROM MEASURES WHERE USERNAME=? AND BUILDING=? AND ROOM=?'
		try:
			self.cursor.execute(sql,(username,building,room))
			columns = [column[0] for column in self.cursor.description]
			capacity = []
			for row in self.cursor.fetchall():
				capacity.append(dict(zip(columns,row)))
			return json.dumps(capacity[0])
		except Exception as ex:
			raise
	
	def close(self):
		self.connection.close()
    		

if __name__ =="__main__":
	database = Database()
<<<<<<< HEAD
	print(database.recover("aleix.clemens"))
=======
	print(database.selectUser("aleix.clemens"))
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8
	database.close()
