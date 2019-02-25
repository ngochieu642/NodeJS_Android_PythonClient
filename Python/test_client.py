import socketio
import cv2

sio = socketio.Client()

@sio.on('connect')
def on_connect():
    print("I'm connected!")

@sio.on('server-send-ok')
def on_message(data):
    print('Server has received the image')
    sio.emit('client-request-img')

@sio.on('server-send-img')
def on_message(data):
    #Nhận được Image thì làm gì
    print("Image")
    print(data)
    showImage(data)

@sio.on('disconnect')
def on_disconenct():
    print("I'm disconnected")

sio.connect("http://localhost:3000")

def showImage(thisDict):
    imageWidth  =thisDict["imageWidth"]
    imageHeight =thisDict["imageHeight"]
    startX      =thisDict["startX"]
    startY      =thisDict["startY"]
    endX        =thisDict["endX"]
    endY        =thisDict["endY"]
    print("info Server: ",imageWidth,imageHeight,startX,startY,endX,endY)

    img = cv2.imread("../Server/laptop.jpeg")
    dimensions = img.shape
    height = img.shape[0]
    width = img.shape[1]
    channels = img.shape[2]
    print('Image Height       : ',height)
    print('Image Width        : ',width)
    print('Number of Channels : ',channels)

    stX = int(float(startX*width/imageWidth))
    eX  = int(float(endX*width/imageWidth))
    stY = int(float(startY*height/imageHeight))
    eY  = int(float(endY*height/imageHeight))

    cv2.rectangle(img,(stX,stY),(eX,eY),(255,0,0),2)
    cv2.imshow("Selected Region",img)
    cv2.waitKey(0)
