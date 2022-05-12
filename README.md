# COVID Safe Spaces

COVID Safe Spaces is a mobile app that allows the user to know the size of
the room they are in by using their phone's camera. The app will then inform
the user about the suggested room capacity according to their country safe
recommendations. The app also provides information about the number of people
detected in the room and warns the user if any of them are not wearing a mask. 


## Authors

- Jorge Pueyo (pueyo99@gmail.com)

## Features

- Extract room capacity using the phone's camera.
- Compute the available ventilation using Computer Vision and Deep Learning.
- Detect the number of people in the room.
- Warn the user if any of the detected people is not wearing a mask.

## Dependencies
The android app is written using Kotlink, while the backened code
and the Machine Learning functionalities are implemented in Python.
- Python 3.7+
- Kotlin 1.3+

## Acknowledgements

 - [YOLOv2](https://pjreddie.com/darknet/yolov2/)
 - [Mask detector](https://github.com/MINED30/Face_Mask_Detection_YOLO)
 - [Semantic segmentator](https://github.com/CSAILVision/semantic-segmentation-pytorch)

## Contributors

- Aleix Clemens
- Miquel Torra

