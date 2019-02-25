var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
var fs = require("fs");

server.listen(process.env.PORT || 3000);

console.log("Image Crop Server.... Sever is running on port 3000");

//Biến để kiểm soát PROCESS
var GET_IMAGE_SUCCESS=0;
var nhanDuoc = false;
var img_obj;

//Show html file for Users who accesed to The Server
app.get("/",function(req,res){
    res.sendFile(__dirname + "/index.html");
})

//Read Image and display to console
// console.log("Reading Image...")
// console.log(image.toString());

//Thông báo có thiết bị kết nối
io.sockets.on('connection',function(socket){
    console.log("Có thiết bị kết nối đến Server !!!");

    socket.on('client-send-img-info',function(noiDung){
        img_obj=noiDung;
        console.log(img_obj);
        nhanDuoc = true;
        io.sockets.emit('server-send-ok',{status : nhanDuoc}); //Gửi tới tất cả device
    })
    nhanDuoc=false;

    //Python client
    socket.on('client-request-img',function(){
    	console.log("Python client detected");
    	socket.emit('server-send-img',img_obj);
    })
});