from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import smtplib
import threading


class Mail():

<<<<<<< HEAD
<<<<<<< HEAD
	def __init__(self, sender, password, receiver, userPassword):
		self.msg = MIMEMultipart()
		self.msg['From'] = sender
		self.msg['To'] = receiver
		self.msg['Subject'] = "Password recovery"
		self.password = password
		self.message = self.writeMessage(userPassword)
		threading.Thread(target=self.sendMessage).start()


<<<<<<< HEAD
	def writeMessage(self,password):
=======
	def writeMessage(self, name,password):
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8
		str = "Your password is: "+password
		return str

=======
=======
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8
	def __init__(self, sender, password, receiver, name):
		self.msg = MIMEMultipart()
		self.msg['From'] = sender
		self.msg['To'] = receiver
		self.msg['Subject'] = "Alta mèdica"
		self.password = password
		self.message = self.writeMessage(name,self.password)
		threading.Thread(target=self.sendMessage).start()


	def writeMessage(self, name,password):
		str = "Your password is: "+password
		return str
		
<<<<<<< HEAD
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8
=======
>>>>>>> 35ee40196c47fadc73d59299e6a991329d4bf5e8

	def sendMessage(self):
		self.msg.attach(MIMEText(self.message, 'plain'))

		server = smtplib.SMTP('smtp.gmail.com:587')
		server.starttls()

		server.login(self.msg['From'], self.password)

		server.sendmail(self.msg['From'], self.msg['To'], self.msg.as_string())

		server.quit()


