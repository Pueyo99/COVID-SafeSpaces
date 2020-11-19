import argparse
import glob
import os
import json

import cv2

from yolo import YOLO

ap = argparse.ArgumentParser()
ap.add_argument('-i', '--images', default="images", help='Path to images or image file')
ap.add_argument('-n', '--network', default="normal", help='Network Type: normal / tiny / prn')
ap.add_argument('-d', '--device', default=0, help='Device to use')
ap.add_argument('-s', '--size', default=416, help='Size for yolo')
ap.add_argument('-c', '--confidence', default=0.5, help='Confidence for yolo')
args = ap.parse_args()

if args.network == "normal":
    print("loading yolo...")
    # yolo = YOLO("models/cross-hands.cfg", "models/cross-hands.weights", ["good", "bad", "none"])
elif args.network == "prn":
    print("loading yolo-tiny-prn...")
    yolo = YOLO("mask_detection/models/mask-yolov3-tiny-prn.cfg", "mask_detection/models/mask-yolov3-tiny-prn.weights", ["good", "bad", "none"])
else:
    print("loading yolo-tiny...")
    # yolo = YOLO("models/cross-hands-tiny.cfg", "models/cross-hands-tiny.weights", ["good", "bad", "none"])

yolo.size = int(args.size)
yolo.confidence = float(args.confidence)

colors = [(0, 255, 0), (0, 165, 255), (0, 0, 255)]

print("extracting tags for each image...")
if args.images.endswith(".txt"):
    with open(args.images, "r") as myfile:
        lines = myfile.readlines()
        files = map(lambda x: os.path.join(os.path.dirname(args.images), x.strip()), lines)
else:
    #Check if it's a single file or a path to a directory
    if ".jpg" in args.images:
        files = sorted(glob.glob(args.images))
    else:
        files = sorted(glob.glob("%s/*.jpg" % args.images))

conf_sum = 0
detection_count = 0
warning = False

index=0

for file in files:
    index += 1 
    print(file)
    
    mat = cv2.imread(file)

    width, height, inference_time, results = yolo.inference(mat)

    print("%s in %s seconds: %s classes found!" %
          (os.path.basename(file), round(inference_time, 2), len(results)))

    output = []

    cv2.namedWindow('image', cv2.WINDOW_NORMAL)
    cv2.resizeWindow('image', 848, 640)

    for detection in results:
        id, name, confidence, x, y, w, h = detection
        cx = x + (w / 2)
        cy = y + (h / 2)

        conf_sum += confidence
        detection_count += 1
	
        if name is "none":
            warning = True

        # draw a bounding box rectangle and label on the image
        color = colors[id]
        cv2.rectangle(mat, (x, y), (x + w, y + h), color, 2)
        text = "%s (%s)" % (name, round(confidence, 2))
        cv2.putText(mat, text, (x, y - 5), cv2.FONT_HERSHEY_DUPLEX, 0.5, color, 1)

        print("%s with %s confidence" % (name, round(confidence, 2)))
        

    cv2.imwrite(file.replace("images","results"), mat)
    filename = file.replace("images","json_results")
    filename = filename.replace(".jpg",".json")

    with open(filename, "w+") as json_file:
        if warning:
            json.dump({"Message": "Warning, someone is not using a mask!"}, json_file)
        else:
            json.dump({"Message": "Everyone is wearing a mask"}, json_file)


    # show the output image
    cv2.imshow('image', mat)


