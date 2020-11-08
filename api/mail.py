from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import smtplib
import threading


class Mail():

	def __init__(self, sender, password, receiver, name):
		self.msg = MIMEMultipart()
		self.msg['From'] = sender
		self.msg['To'] = receiver
		self.msg['Subject'] = "Alta mèdica"
		self.password = password
		self.message = self.writeMessage(name,self.password)
		threading.Thread(target=self.sendMessage).start()


	def writeMessage(self, name,password):
		str = "Your password is: "+passwords
		return str
		

	def sendMessage(self):
		self.msg.attach(MIMEText(self.message, 'plain'))

		server = smtplib.SMTP('smtp.gmail.com:587')
		server.starttls()

		server.login(self.msg['From'], self.password)

		server.sendmail(self.msg['From'], self.msg['To'], self.msg.as_string())

		server.quit()


