from PIL import Image

def calculate_window(img_name):
    img = Image.open(img_name)

    width, height = img.size

    img_original = img.crop((0,0,width/2,height))
    img_crop = img.crop((width/2,0,width,height))

    colors = img_crop.getcolors()
    #print(colors)

    for c in colors:
        if c[1] == (230,230,230):
            window_pixels = c[0]
            total_pixels = (width*height)/2
            percentage = (window_pixels/total_pixels)*100
            print(f"Percentage of windows = {percentage}")
        else:
            print("No windows found")

    img_original.show()
    img_crop.show()
