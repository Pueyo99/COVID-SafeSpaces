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

	def deleteUser(self,username):
		sql = 'DELETE FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			self.connection.commit()
		except Exception as ex:
			raise

	def showUnverifiedUsers(self):
		sql = 'SELECT * FROM UNVERIFIEDUSERS'
		try:
			self.cursor.execute(sql)
			columns = [column[0] for column in self.cursor.description]
			users = []
			for row in self.cursor.fetchall():
				users.append(dict(zip(columns,row)))
			return json.dumps(users)

		except Exception as ex:
			raise

	def recover(self,username):
		sql = 'SELECT EMAIL,PASSWORD FROM USERS WHERE USERNAME = ?'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()
			return (users['EMAIL'],users['PASSWORD'])
		except Exception as ex:
			raise


	def registerUser(self,username,password, mail):
		values=(username,password,mail)
		sql = 'INSERT INTO USERS VALUES (?,?,?)'
		try:
			self.cursor.execute(sql, values)
			self.connection.commit()
		except Exception as ex:
			raise
		
	def registerUnverified(self,username,password,mail):
		values=(username,password,mail)
		sql1='INSERT INTO UNVERIFIEDUSERS (USERNAME,PASSWORD,EMAIL) VALUES (?,?,?)'
		sql2='DELETE FROM UNVERIFIEDUSERS WHERE (JULIANDAY()-JULIANDAY(REGISTERTIME)) > 1.0'
		try:
			self.cursor.execute(sql1,values)
			self.cursor.execute(sql2)
			self.connection.commit()
		except Exception as ex:
			raise

	def selectUnverified(self,username):
		print("Seleccionando")
		sql1 = 'SELECT PASSWORD,EMAIL FROM UNVERIFIEDUSERS WHERE USERNAME=? AND (JULIANDAY()-JULIANDAY(REGISTERTIME)) < 1.0'
		sql2 = 'DELETE FROM UNVERIFIEDUSERS WHERE USERNAME=?'
		try:
			self.cursor.execute(sql1,(username,))
			data = self.cursor.fetchone()
			if(data):
				print("He obtenido datos")
				password = data['PASSWORD']
				email = data['EMAIL']
				self.registerUser(username,password,email)
				self.cursor.execute(sql2,(username,))
				self.connection.commit()
				return {'verification':'user verified'}
			print("No hay datos en la tabla")	
			self.cursor.execute(sql2)
			self.connection.commit()	 
			return {'verification':'user not verified'}
		except Exception as ex:
			print(ex)
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

	def updatePassword(self,username, password):
		sql = 'UPDATE USERS SET PASSWORD=? WHERE USERNAME=?'
		try:
			self.cursor.execute(sql,(password,username))
			self.connection.commit()
		except Exception as ex:
			raise
	
	def close(self):
		self.connection.close()
    		

if __name__ =="__main__":
	database = Database()
	#print(database.updatePassword("aleix.clemens","Clemens_7"))
	#print(database.selectUser("aleix.clemens"))
	print(database.showUnverifiedUsers())
	database.deleteUser("clemens")
	database.close()

