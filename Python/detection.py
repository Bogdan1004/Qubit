
                             #~~~~~~~#
                             # Qubit #
                             #~~~~~~~#
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Importez toate librariile de care am nevoie  #
#pentru video stream si prelucrarea imaginilor #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
import cv2
from sys import argv
import matplotlib.pyplot as plt
import numpy as np
from imutils.video import VideoStream
from imutils.video import FPS
import argparse
import imutils
import time

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Setez valoarea minima a treshold-ului #
#pentru a evita detectarile gresite     #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
threshold = 0.65;

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Initializez lista de obiecte din MobileNet SSD, ca mai apoi sa ma folosesc de          #
#reteaua neuronala antrenata de mine pentru a recunoaste deseurile si a incadra obiectul #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

CLASSES = ["unlabeled", "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic", "fire", "street", "stop", "parking", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "hat", "backpack", "umbrella", "shoe", "eye", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports", "kite", "baseball", "baseball", "skateboard", "surfboard", "tennis", "bottle", "plate", "wine", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot", "pizza", "donut", "cake", "chair", "couch", "potted", "bed", "mirror", "dining", "window", "desk", "toilet", "door", "tv", "laptop", "mouse", "remote", "keyboard", "cell", "microwave", "oven", "toaster", "sink", "refrigerator", "blender", "book", "clock", "vase", "scissors", "teddy", "hair", "toothbrush", "hair", "banner", "blanket", "branch", "bridge", "building-other", "bush", "cabinet", "cage", "cardboard", "carpet", "ceiling-other", "ceiling-tile", "cloth", "clothes", "clouds", "counter", "cupboard", "curtain", "desk-stuff", "dirt", "door-stuff", "fence", "floor-marble", "floor-other", "floor-stone", "floor-tile", "floor-wood", "flower", "fog", "food-other", "fruit", "furniture-other", "grass", "gravel", "ground-other", "hill", "house", "leaves", "light", "mat", "metal", "mirror-stuff", "moss", "mountain", "mud", "napkin", "net", "paper", "pavement", "pillow", "plant-other", "plastic", "platform", "playingfield", "railing", "railroad", "river", "road", "rock", "roof", "rug", "salad", "sand", "sea", "shelf", "sky-other", "skyscraper", "snow", "solid-other", "stairs", "stone", "straw", "structural-other", "table", "tent", "textile-other", "towel", "tree", "vegetable", "wall-brick", "wall-concrete", "wall-other", "wall-panel", "wall-stone", "wall-tile", "wall-wood", "water-other", "waterdrops", "window-blind", "window-other", "wood"]
# Se genereaza la intamplare un set de culori pentru fiecare obiect
COLORS = np.random.uniform(0, 255, size=(len(CLASSES), 3))

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Se incarca modelul cu ajutorul NEURAL COMPUTE STICK 2 #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
print("[INFO] loading model...")
net = cv2.dnn.readNet('frozen_inference_graph.bin', 'frozen_inference_graph.xml')

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Am setat sa se foloseasca dnn(deep neural network) si sa nu se foloseasca de haar(haarcascade) #
# deoarece avand sticul NCS2 procesarea se face pe GPU                                           #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
net.setPreferableTarget(cv2.dnn.DNN_TARGET_MYRIAD)

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Initializez camera si ii ofer un timp de calibrare pentru un start de frame-uri mai bun #
# deasemenea initializez si FPS counter ul                                                #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
print("[INFO] starting video stream...")
vs = cv2.VideoCapture(0)
vs.set(3, 1920)
vs.set(4, 1080)
time.sleep(2.0)
fps = FPS().start()
x_center = 0
y_center = 0

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Cat timp camera este pornita / programul ruleaza #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#

while True:
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Salveaza local fiecare frame si il trece printr-o serie de filtre  #
# 1. Se face resize la frame pentru a ajunge la 300 pixeli           #
# 2. Roteste imaginea la 90 grade                                    #


        ret, frame = vs.read()
        cropped = frame[60:1080-60, 0:960]
        cropped = imutils.resize(cropped, width=300)
        cropped = imutils.rotate(cropped, 90)
        
# 3. Dupa ce are imaginea la 300 pixeli o transforma dintr-o matrice intr-un

        (h, w) = cropped.shape[:2]
        frame = cropped
        blob = cv2.dnn.blobFromImage(cropped, size=(300,300), ddepth=cv2.CV_8U)  
        
# 4. Se trece vectorul prin reteaua neuronala si se obtine o predictie

        net.setInput(blob)
        detections = net.forward()
        
# 5. Se remodeleaza vectrul ce contine detectarea/detectarile

        detections2 = detections.reshape(-1,7)
        
# 6. Se reiau detectille pentru o acuratete mai mare

        for detection in detections2:
            
# 7. Se extrage probabilitatea/predictia

            confidence = float(detection[2])
            
# 8. Se filtreaza detectarile slabe prin verificare daca 'confidence' este mai mare
#decat valoarea minima a 'confidence'

            if confidence > threshold:
                
# 9.Se extrage index-ul clasei din 'detections', apoi se creeaza 'bounding box'
# in jurul obiectului detectat prin coordonate (x, y)

                idx = int(detection[1])
                box = detection[3:7] * np.array([w, h, w, h])
                (startX, startY, endX, endY) = box.astype("int")
                
# 10. Coordonatele obiectului sunt scrise intr-un fisier cu extensia txt

                if CLASSES[idx] == "bottle":
                    x_center = (startX+endX)/2
                    
                    file = open("testfile.txt","w")
                    file.seek(0)                        
                    file.truncate()
                    file.write(str(x_center)+","+str(endY))
                    file.close()
                
# 11. Predictia este scrisa langa 'bounding box' 
                
                label = "{}: {:.2f}%".format(CLASSES[idx], confidence * 100)
                cv2.rectangle(frame, (startX, startY), (endX, endY), COLORS[idx], 2)
                y = startY - 15 if startY - 15 > 15 else startY + 15
                cv2.putText(frame, label, (startX, y), cv2.FONT_HERSHEY_SIMPLEX, 0.5, COLORS[idx], 2)
                        
# 12. Se afiseaza frame-ul de final  
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
        cv2.imshow("Frame", frame)
        key = cv2.waitKey(1) & 0xFF


# In caz de eroare tasta Q va oprii programul
        if key == ord("q"):
            break

# Se adapteaza numarul de frame-uri 
        fps.update()
# Se opreste timer-ul
fps.stop()
print("[INFO] elapsed time: {:.2f}".format(fps.elapsed()))
print("[INFO] approx. FPS: {:.2f}".format(fps.fps()))

# Se curata datele CACHE pentru a nu influenta urmatoarea detectie
cv2.destroyAllWindows()
vs.stop()

                            #~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
                            # Signed by Qubit (Preda Bogdan)#
                            #~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
