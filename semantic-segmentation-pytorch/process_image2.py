import os
import matplotlib.pyplot as plt

image_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'example.JPG')
img = plt.imread(image_path)
fig, ax = plt.subplots(1, figsize=(15, 15))
ax.imshow(img)
plt.show()
