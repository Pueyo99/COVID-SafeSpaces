from PIL import Image

def resize_images(test_path):

	for p in test_path:
		p = p["fpath_img"]
		img = Image.open(p)
		width, height = img.size

	while width > 1000 or height > 1000:
		img = img.resize((round(width/1.5),round(height/1.5)),Image.LANCZOS)
		width, height = img.size
		img.save(p)

def resize_image(img):
	width, height = img.size
	while width > 1000 or height > 1000:
		img = img.resize((round(width/1.5),round(height/1.5)),Image.LANCZOS)
		width, height = img.size
	
	return img

