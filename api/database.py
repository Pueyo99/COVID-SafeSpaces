import sqlite3
import json

class Database:
	def __init__(self):
		self.connection = sqlite3.connect("covid.db")
		self.connection.row_factory = sqlite3.Row
		self.cursor = self.connection.cursor()
		print("Conexión establecida")

	def selectUser(self, username):
		sql = 'SELECT PASSWORD FROM USERS WHERE USER = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()  #Returns a tuple with the user info
			return users['PASSWORD']
		except Exception as ex:
			raise

	def registerUser(self,username,password):
		values=(username,password)
		sql = 'INSERT INTO USERS VALUES (?,?)'
		try:
			self.cursor.execute(sql, values)
			self.connection.commit()
		except Exception as ex:
			raise

	def getBuildings(self):
		sql = 'SELECT DISTINCT BUILDING FROM MEASURES ORDER BY BUILDING'
		try:
			self.cursor.execute(sql)
			columns = [column[0] for column in self.cursor.description]
			buildings = []
			for row in self.cursor.fetchall():
				buildings.append(dict(zip(columns,row)))
			return json.dumps(buildings)
		except Exception as ex:
			raise

	def getRooms(self, building):
		sql = 'SELECT ROOM FROM MEASURES WHERE BUILDING=? ORDER BY ROOM'
		try:
			self.cursor.execute(sql,(building,))
			columns = [column[0] for column in self.cursor.description]
			rooms = []
			for row in self.cursor.fetchall():
				rooms.append(dict(zip(columns,row)))
			return json.dumps(rooms)
		except Exception as ex:
			raise

	def getCapacity(self, building, room):
		sql = 'SELECT CAPACITY FROM MEASURES WHERE BUILDING=? AND ROOM=?'
		try:
			self.cursor.execute(sql,(building,room))
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
	print(database.getCapacity("A1","101"))
	database.close()
