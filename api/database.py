import pymysql

class Database:
	def __init__(self):
		self.connection = pymysql.connect(
			host='localhost',
			user='root',
			password='',
			db='covid',
			cursorclass=pymysql.cursors.DictCursor
			)

		self.cursor = self.connection.cursor()
		print("Conexión establecida")

	def selectUser(self, username):
		sql = 'SELECT PASSWORD FROM USERS WHERE USER = %s'
		try:
			self.cursor.execute(sql,(username,))
			users = self.cursor.fetchone()  #Returns a tuple with the user info
			return users['PASSWORD']
		except Exception as ex:
			raise

	def registerUser(self,username,password):
		values=(username,password)
		sql = 'INSERT INTO USERS VALUES (%s,%s)'
		try:
			self.cursor.execute(sql, values)
			self.connection.commit()
		except Exception as ex:
			raise
	
	def close(self):
		self.connection.close()
    		

if __name__ =="__main__":
	database = Database()
	print(database.selectUser("aleix.clemens"))
	database.registerUser("juan.palomo", "jorgePayaso")
	database.close()
