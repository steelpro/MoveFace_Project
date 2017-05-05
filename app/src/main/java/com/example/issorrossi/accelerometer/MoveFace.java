package com.example.issorrossi.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MoveFace extends Activity implements SensorEventListener{

    Bitmap face;
    Bitmap Body;
    SensorManager sm;
    Face ourView;
    float x, y, sensorX, sensorY;
    Paint paint = new Paint();
    public Canvas canvas;

    /** SENSOR MANAGER FOR ACCELEROMETER **/
    @Override
    public void onSensorChanged(SensorEvent event) {

        try { Thread.sleep(6); }//THREAD.SLEEP TO LOWER SENSITIVITY OF ACCELEROMETER
        catch(InterruptedException e){ e.printStackTrace(); }

        sensorX = event.values[1];
        sensorY = event.values[0];
        //VALUE[1] IS USUALLY Y AXIS BUT SINCE LANDSCAPE THEY ARE SWITCHED
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    //METHOD NEEDED FOR SENSOR TO WORK
    /** END SENSOR MANAGER FOR ACCELEROMETER **/

    /** FACE CREATION CLASS **/
    public class Face extends SurfaceView implements Runnable {

        SurfaceHolder ourHolder;
        boolean isRunning = true;
        Thread ourThread = null;

        public Face(Context context) {
            super(context);
            ourHolder = getHolder();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) { //CODE TO HANDLE TOUCH EVENTS

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                paint.setTextSize(100);

                try { Thread.sleep(3); } //USED TO HANDLE SENSITIVITY
                catch(InterruptedException e) { e.printStackTrace(); }

                if(sensorX < 3.20 && sensorX > 2.70 && sensorY < 0.22 && sensorY > -0.40)
                    canvas.drawText("You Win!!!", 100, 200, paint);
                //TEST IF FACE IS IN CORRECT X AND Y COORDINATES

                else
                    canvas.drawText("You didn't win. You lost.", 100, 100, paint);

                destroy(); //END GAME
            }
            return true;
        }

        public void pause() {
            isRunning = false;

            while(true){
                try { ourThread.join(); }
                catch(InterruptedException e) { e.printStackTrace(); }
                break;
            }
            ourThread = null;
        }

        public void resume(){
            isRunning = true;
            ourThread = new Thread(this);
            ourThread.start();
        }

        public void destroy(){
            isRunning = false;
            ourThread = null;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(Body, 300, -150, null); //CREATE BODY
        }

        @Override
        public void run() {

            while(isRunning) {

                if(!ourHolder.getSurface().isValid())
                    continue;

                float startX = 700; //STARTING POINTS FOR BITMAP (FACE)
                float startY = 250;

                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.WHITE); //BACKGROUND IS WHITE FOR CLEAN INTERFACE
                canvas.drawBitmap(face, startX+sensorX*200, startY+sensorY*150, null);
                //CHANGING X AND Y CHANGES FACE DISTANCE TRAVELED ON SCREEN

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
    /** END FACE CREATION CLASS **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //RECEIVE ACCELEROMETER SENSOR

        if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            Sensor s = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
            sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Intent i = getIntent();
        x = y = sensorX = sensorY = 0;

        int imagePath = i.getIntExtra("image", 0); //RECEIVE FACE THAT WAS SENT FROM MENU
        face = BitmapFactory.decodeResource(getResources(), imagePath);

        int body = R.drawable.suit; //CREATE BODY
        Body = BitmapFactory.decodeResource(getResources(), body);

        ourView = new Face(this);
        ourView.resume();
        setContentView(ourView);
    }

    @Override
    protected void onPause(){
        sm.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ourView.destroy();
    }
}