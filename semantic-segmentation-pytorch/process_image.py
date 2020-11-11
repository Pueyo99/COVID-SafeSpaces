from PIL import Image
import json

def calculate_window(img_name):
	img = Image.open(img_name)
	width, height = img.size

	img_original = img.crop((0,0,width/2,height))
	img_crop = img.crop((width/2,0,width,height))

	colors = img_crop.getcolors()
	
	percentage = 0

	for c in colors:
		if c[1] == (230,230,230):
			window_pixels = c[0]
			total_pixels = (width*height)/2
			percentage = (window_pixels/total_pixels)*100
	
		
	img_name = img_name.replace("result_images/", "json_results/")
	print(img_name)
	
	with open(img_name.replace('.png','.json'), "w+") as json_file:
		json.dump({ "Window percentage" : percentage }, json_file)
	
	img_original.show()
	img_crop.show()
