from PIL import Image

def resize_images(test_path):

	for p in test_path:
		p = p["fpath_img"]
		img = Image.open(p)
		width, height = img.size

	while width > 1000 or height > 1000:
		width /= 2
		height /= 2
		img = img.resize((round(width/2),round(height/2)),Image.LANCZOS)
		img.save(p)
