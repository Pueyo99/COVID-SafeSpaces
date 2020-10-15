from PIL import Image

def resize_images(test_path):
    for p in test_path:

        p = p["fpath_img"]
        img = Image.open(p)
        width, height = img.size

        if width > 1000 or height > 1000:
            img = img.resize((round(width/4),round(height/4)),Image.LANCZOS)
            img.save(p)
