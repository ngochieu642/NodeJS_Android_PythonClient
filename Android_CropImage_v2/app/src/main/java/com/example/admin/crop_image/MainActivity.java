package com.example.admin.crop_image;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {

    //Screen manipulate variable
    LinearLayout l1;
    Button btnClear, btnCrop;
    String url ="https://androidsamppsql.000webhostapp.com/laptop.jpeg";
    ImageView imgViewCrop;
    Bitmap bmp, multableBitmap;
    TextView txtStartPosition, txtCurrentPosition, txtRectangle, txtRoot;
    Canvas canvas;
    Paint paint;

    //Biến Display
    int[] arrayCrop ={0,0,0,0};
    int[] viewCoords = new int[2]; // Top left
    int rectEndX,rectEndY;

    //Syncing Info
    private Socket mSocket;

    int imgOriWidth=500,imgOriHeight=333; //Image Size in pixels75


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Kết nối đến Server
        Connect2Server();
        mSocket.on("server-send-ok",onReceiveStatus);

        //Khai báo màn hình
        AnhXa();

        //Load hình ảnh từ url
        LoadImageFromUrl(url);

        //Event Listener
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("startX",arrayCrop[0]);
                    jsonObject.put("startY",arrayCrop[1]);
                    jsonObject.put("endX",arrayCrop[2]);
                    jsonObject.put("endY",arrayCrop[3]);
                    jsonObject.put("imageWidth",l1.getWidth());
                    jsonObject.put("imageHeight",l1.getHeight());

                    mSocket.emit("client-send-img-info",jsonObject);
                } catch(JSONException e){

                }
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clear all rectangles
                ClearAllRects();
                ResetText();
            }
        });
        imgViewCrop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Variable to draw rectangle must be continuous
                l1.getLocationOnScreen(viewCoords);
                txtRoot.setText("Root  x:"+viewCoords[0]+"  y:"+viewCoords[1]
                        +"   Image Size ("+l1.getWidth()+","+l1.getHeight()+")");
                rectEndX = (int)event.getX(); //Current position of the rectangle
                rectEndY=(int)event.getY();

                txtCurrentPosition.setText("x:" +rectEndX +"  y:"+rectEndY);
                txtStartPosition.setText("x:"+arrayCrop[0]+"  y:"+arrayCrop[1]);

                //Delete first, then create
                ClearAllRects();
                CreateRect(arrayCrop[0]*imgOriWidth/l1.getWidth(),arrayCrop[1]*imgOriHeight/l1.getHeight(),
                        rectEndX*imgOriWidth/l1.getWidth(),rectEndY*imgOriHeight/l1.getHeight(),
                        paint);

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    //Display info if TOUCH
                    int touchX = (int)event.getX();
                    int touchY = (int)event.getY();

                    int imageX = touchX;
                    int imageY = touchY;


                    //Save the start Position
                    arrayCrop[0]=imageX;
                    arrayCrop[1]=imageY;

                }else if(event.getAction()==MotionEvent.ACTION_UP){
                    //RELEASE
                    int touchX = (int)event.getX();
                    int touchY = (int)event.getY();
                    int imageX = touchX ;
                    int imageY = touchY ;

                    arrayCrop[2]=imageX;
                    arrayCrop[3]=imageY;
                    txtRectangle.setText("Start ("+arrayCrop[0]+","+arrayCrop[1]+")  End ("+arrayCrop[2]+","+arrayCrop[3]+")");
                }
                return true;
            }
        });
    }
    private Emitter.Listener onReceiveStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = (JSONObject)args[0];
                    try {
                        boolean status =object.getBoolean("status");
                        if (status){
                            Toast.makeText(MainActivity.this, "Info has been sent to Server", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Info Lost", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private void Connect2Server(){
        try {
            mSocket = IO.socket("http://192.168.2.26:3000/");
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Server fail to start...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        mSocket.connect();
    }
    private void ResetText(){
        txtRoot.setText(R.string.defaultRoot);
        txtStartPosition.setText(R.string.defaultStartPos);
        txtCurrentPosition.setText(R.string.defaultCurrentPos);
        txtRectangle.setText(R.string.defaultRectangle);
    }
    private void ClearAllRects(){
        multableBitmap = bmp.copy(Bitmap.Config.ARGB_8888,true);
        l1.setBackground(new BitmapDrawable(multableBitmap));
    }
    private void CreateRect(int left, int top, int right, int bottom, Paint rectPaint){
        canvas = new Canvas(multableBitmap);
        canvas.drawRect(left,top,right,bottom,rectPaint);
        imgViewCrop.setImageDrawable(null);
        l1.setBackground(new BitmapDrawable(multableBitmap));
    }
    private void AnhXa(){
        l1                  = findViewById(R.id.linearLayout_1);
        btnClear            = findViewById(R.id.buttonClear);
        btnCrop             = findViewById(R.id.buttonCrop);
        imgViewCrop         = findViewById(R.id.imageViewCrop);
        txtCurrentPosition  = findViewById(R.id.txtCurrentPosition);
        txtRectangle        = findViewById(R.id.txtRect);
        txtStartPosition    = findViewById(R.id.txtStartPosition);
        txtRoot             = findViewById(R.id.txtViewRoot);
    }
    private void LoadImageFromUrl(String url){
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                .into(imgViewCrop, new com.squareup.picasso.Callback(){
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Read Successfully!", Toast.LENGTH_SHORT).show();

                        paint = new Paint();
                        paint.setColor(Color.parseColor("#003399"));
                        paint.setAlpha(50);

                        bmp = ((BitmapDrawable)imgViewCrop.getDrawable()).getBitmap();
                        multableBitmap = bmp.copy(Bitmap.Config.ARGB_8888,true);

                        //Create Rectangle
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(MainActivity.this, "Database not available", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
