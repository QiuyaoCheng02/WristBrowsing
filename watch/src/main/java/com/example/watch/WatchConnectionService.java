package com.example.watch;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fragment.ConfirmDialogFragment;
import fragment.TabFragment;

public class WatchConnectionService extends Service implements SensorEventListener{
    private static final int GYROSCOPE_THRESHOLD = 4; // 陀螺仪阈值
    private static final int ACCELEROMETER_THRESHOLD = 4; // 加速度计阈值
    private StringBuilder thumbnailBuilder = new StringBuilder();
    private Boolean isWristUp = false; // 手腕是否抬起
    private Boolean isRotationDetected = false; // 是否检测到旋转手势
    private Boolean isTranslationDetected = false; // 是否检测到平移手势
    private float previousAx = 0f;
    private float previousAy = 0f;
    private float previousAz = 0f;
    private long shakeTime;//抬手时间
    private long showTime;//平放手机时间
    private long flatTime = 0; // 记录平放状态开始时间
    private long verticalTime = 0; // 记录垂直状态开始时间

    private enum GestureType {
        NONE,
        CLOCKWISE_ROTATION,
        COUNTERCLOCKWISE_ROTATION,

    }
    private GestureType currentGesture = GestureType.NONE;

    private static final String TAG = "ConnectionServiceWatch";
    private static final int PHONE_PORT = 8000; // 手表监听的端口号
    private boolean isFlat = true; // 默认设备处于水平状态
    private boolean isVertical = false; // 默认设备不处于垂直状态
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private InputStream mInputStream;
    private static OutputStream mOutputStream;
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private TabFragment tabFragment;
    private ConfirmDialogFragment confirmFragment;
    private static final int FILTER_SIZE = 10;
    private float[] accelerometerBufferX = new float[FILTER_SIZE];
    private float[] accelerometerBufferY = new float[FILTER_SIZE];
    private float[] accelerometerBufferZ = new float[FILTER_SIZE];
    private int bufferIndex = 0;
    private Handler handler;

    private boolean currentUp=true;
    private Handler uiHandler;

    private  boolean isUpClockwise=false;
    private boolean isUpdated=false;
    private String status;
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "started watch");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        uiHandler = new Handler(Looper.getMainLooper());
        int customDelay = 20000000;
        // 注册加速度计传感器
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, customDelay);
        }

        // 注册陀螺仪传感器
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor,customDelay);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectToPhone();
        Log.d(TAG, "connected watch");
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Log.d(TAG, "onSensorChanged");
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float ax = sensorEvent.values[0];
            float ay = sensorEvent.values[1];
            float az = sensorEvent.values[2];
         /*   // 将数据放入滤波器缓冲区
            accelerometerBufferX[bufferIndex] = ax;
            accelerometerBufferY[bufferIndex] = ay;
            accelerometerBufferZ[bufferIndex] = az;
            bufferIndex = (bufferIndex + 1) % FILTER_SIZE;
            int medumValue = 14;


            // 计算滤波后的数据平均值
            float filteredAx = 0;
            float filteredAy = 0;
            float filteredAz = 0;
            for (int i = 0; i < FILTER_SIZE; i++) {
                filteredAx += accelerometerBufferX[i];
                filteredAy += accelerometerBufferY[i];
                filteredAz += accelerometerBufferZ[i];
            }
            filteredAx /= FILTER_SIZE;
            filteredAy /= FILTER_SIZE;
            filteredAz /= FILTER_SIZE;
            *//*Log.d(TAG, "x:"+filteredAx);
            Log.d(TAG, "y:"+filteredAy);
            Log.d(TAG, "z:"+filteredAz);
*//*
            float x=filteredAx-previousAx;
            float y=filteredAy-previousAy;
            float z=filteredAz-previousAz;*/

          /*  // 进行判断和处理逻辑
            if (((Math.abs(filteredAx - previousAx) > ACCELEROMETER_THRESHOLD && Math.abs(filteredAx)<ACCELEROMETER_THRESHOLD)
                    || (Math.abs(filteredAy - previousAy) > ACCELEROMETER_THRESHOLD&& Math.abs(filteredAy)<ACCELEROMETER_THRESHOLD)
                    || Math.abs(filteredAz - previousAz) > ACCELEROMETER_THRESHOLD)
                    && currentGx > 4) {

                isWristUp = true;
                Log.d(TAG, "抬手");
            }*/
            // 进行判断和处理逻辑
            Log.d(TAG, "x:" + ax);
            Log.d(TAG, "y:" + ay);
            Log.d(TAG, "z:" + az);
         /*   if (Math.abs(ax) > ACCELEROMETER_THRESHOLD ||
                    Math.abs(ay) > ACCELEROMETER_THRESHOLD ||
                    Math.abs(az) > ACCELEROMETER_THRESHOLD ) {
                *//*isWristUp = true;
                Log.d(TAG, "抬手");
                if(currentUp!=true){
                    vibe();
                }
                currentUp=isWristUp;*//*
                shakeTime = System.currentTimeMillis();

                // 在这里执行抬手操作
            }*/
            //判断是否平放手机
            if (9 < az && -2 < ax && ax < 2 && -2 < ay && ay < 2) {
                if (flatTime==0) {
                    // 如果之前不是平放状态，记录平放状态开始时间
                    flatTime = System.currentTimeMillis();
                } else {
                    // 如果已经是平放状态，检查是否已经持续一段时间，如果是则标记为持续平放状态
                    if (System.currentTimeMillis() - flatTime > 500) { // 持续一秒钟
                        isFlat = true;
                        Log.d(TAG, "抬手");
                    }
                }
                // 重置垂直状态相关数据
                isVertical = false;
                verticalTime=0;
            }
            if (ax<-8 && -3 < az && az < 3 && -4 < ay && ay < 0) {
                Log.d(TAG, "放手1");
                if (verticalTime==0) {
                    // 如果之前不是垂直状态，记录垂直状态开始时间
                    verticalTime = System.currentTimeMillis();
                    Log.d(TAG, "vertical time "+verticalTime);
                } else {
                    // 如果已经是垂直状态，检查是否已经持续一段时间，如果是则标记为持续垂直状态
                    if (System.currentTimeMillis() - verticalTime > 500) { // 持续一秒钟
                        isVertical = true;
                        Log.d(TAG, "放手");

                    }
                }
                // 重置平放状态相关数据
                isFlat = false;
                flatTime=0;
            }

        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gx = sensorEvent.values[0];
            float gy = sensorEvent.values[1];
            float gz = sensorEvent.values[2];
           /* Log.d(TAG, "1: "+gx);
            Log.d(TAG, "2: "+gy);
            Log.d(TAG, "3: "+gz);*/

            // 判断顺时针/逆时针旋转
            if (isFlat) {

                    if (gx > 6&&!currentGesture.equals(GestureType.CLOCKWISE_ROTATION)) {
                        currentGesture = GestureType.CLOCKWISE_ROTATION;
                        Log.d(TAG, "检测到抬手顺时针旋转手势");
                        if(isUpdated==false){
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    writeToPhone("CONFIRM_SAVE");
                                    status="null";
                                    isUpdated=true;
                                    return null;
                                }
                            }.execute();
                        }


                    } else if (gx < -6&&!currentGesture.equals(GestureType.COUNTERCLOCKWISE_ROTATION)) {
                        currentGesture = GestureType.COUNTERCLOCKWISE_ROTATION;
                        Log.d(TAG, "检测到抬手逆时针旋转手势");
                        if(isUpdated==false){
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    writeToPhone("CONFIRM_NOTSAVE");
                                    isUpdated=true;
                                    return null;
                                }
                            }.execute();
                        }
                        // 在这里执行逆时针旋转手势的操作，例如发送广播或启动其他服务
                    }

            }
            if (isVertical) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        writeToPhone("ZOOM");

                        return null;
                    }
                }.execute();

                if (gz > 2 &&!currentGesture.equals(GestureType.CLOCKWISE_ROTATION)) {
                    currentGesture = GestureType.CLOCKWISE_ROTATION;
                    Log.d(TAG, "检测到顺时针旋转手势");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            writeToPhone("ZOOMIN");

                            return null;
                        }
                    }.execute();

                    // 在这里执行顺时针旋转手势的操作，例如发送广播或启动其他服务
                } else if (gz < -2 &&!currentGesture.equals(GestureType.COUNTERCLOCKWISE_ROTATION)){
                    currentGesture = GestureType.COUNTERCLOCKWISE_ROTATION;
                    Log.d(TAG, "检测到逆时针旋转手势");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            writeToPhone("ZOOMOUT");

                            return null;
                        }
                    }.execute();
                    // 在这里执行逆时针旋转手势的操作，例如发送广播或启动其他服务
                }

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 在这里实现传感器精度变化的处理
        // 这个方法可以为空，或者根据需要添加相应的处理代码
    }

    public void vibe() {
        Log.d(TAG, "vibe: ");
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 增加振幅以增强震动效果
        int amplitude = 255; // 调整振幅值，可以尝试不同的值来找到最合适的效果
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(3000, amplitude);

        vibrator.vibrate(vibrationEffect);

        Log.d(TAG, "vibeed: ");

        // 取消震动
        vibrator.cancel();
    }


    private WatchConnectionServiceBinder binder = new WatchConnectionServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class WatchConnectionServiceBinder extends Binder {
        public WatchConnectionService getService() {
            return WatchConnectionService.this;
        }
    }
    private void showConnectedToast() {
        // 在UI线程中显示Toast消息
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WatchConnectionService.this, "Connected", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void connectToPhone() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(PHONE_PORT);
                    mSocket = mServerSocket.accept();

                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    Log.d(TAG, "Phone connected: " + mSocket.getInetAddress());

                    showConnectedToast();

                    ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while (true) {
                        try {
                            bytesRead = mInputStream.read(buffer);
                            if (bytesRead == -1) {
                                // End of stream
                                break;
                            }

                            dataBuffer.write(buffer, 0, bytesRead);

                            while (true) {
                                if (dataBuffer.size() < 3) {
                                    // Not enough data to determine length
                                    break;
                                }

                                byte[] data = dataBuffer.toByteArray();
                                int expectedLength = decodeVLQ(data, 1);

                                if (expectedLength >= 30000) { // 过滤掉低于阈值的情况
                                    if (dataBuffer.size() >= expectedLength + 3) {
                                        processReceivedData(data, expectedLength + 3);

                                        // Store the remaining data in dataBuffer
                                        byte[] remainingData = Arrays.copyOfRange(data, expectedLength + 3, data.length);
                                        dataBuffer.reset();
                                        dataBuffer.write(remainingData, 0, remainingData.length);
                                    } else {
                                        // Not enough data yet
                                        break;
                                    }
                                } else {
                                    // Skip invalid data
                                    dataBuffer.reset();
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Error reading from InputStream", e);
                            break;
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error creating server socket", e);
                }
            }
        }).start();
    }

    public void writeToPhone(String message) {
        try {
            mOutputStream.write(message.getBytes());
            Log.d(TAG, "writeToPhone: "+message);
        } catch (IOException e) {
            Log.e(TAG, "Error writing to OutputStream", e);
        }
    }
    private int expectedTotalChunks = -1;
    private int receivedChunks = 0;
    private StringBuilder receivedDataBuilder = new StringBuilder();


    private void processReceivedData(byte[] data, int length) {
        status=null;
        receivedDataBuilder = new StringBuilder();
        receivedChunks = 0;

        int TIME_LENGTH = 19;
        if (length > 0) {
            byte firstByte = data[0];
            Log.d(TAG, "first: " + firstByte);

            if (firstByte == 'B') {

                Log.d(TAG, "processReceivedB: " + data.length + " " + length);

                int thumbnailLength = decodeVLQ(data, 1); // Decode VLQ-encoded length
                int thumbnailIndex=1+computeVLQSize(thumbnailLength);
                int timeIndex = 1 + thumbnailLength+computeVLQSize(thumbnailLength); // Calculate index of time data
                int titleIndex = timeIndex + TIME_LENGTH; // Calculate index of title data

                Log.d(TAG, "thumbnail length: " + thumbnailLength);
                Log.d(TAG, "thumbnail index: " + thumbnailIndex);
                Log.d(TAG, "timeIndex: "+timeIndex);
                Log.d(TAG, "titleIndex: "+titleIndex);
                //if (data.length >= titleIndex + thumbnailLength + TIME_LENGTH) {
                    // Process thumbnail data (data[titleIndex] to data[titleIndex + thumbnailLength - 1])
                    byte[] thumbnailData = Arrays.copyOfRange(data, thumbnailIndex,  timeIndex);

                    // Extract time data (data[timeIndex] to data[timeIndex + TIME_LENGTH - 1])
                    byte[] timeData = Arrays.copyOfRange(data, timeIndex, titleIndex);

                    Log.d(TAG, "time data: " + Arrays.toString(timeData));
                    Date date=convertBytesToDate(timeData);
                    String receivedTime = getTimeAgo(date);

                    // Extract title data (data[titleIndex + thumbnailLength + TIME_LENGTH] onwards)
                    byte[] titleData = Arrays.copyOfRange(data, titleIndex, data.length);

                    String receivedTitle = new String(titleData, Charset.forName("UTF-8"));
                    Log.d(TAG, "title：" + receivedTitle);

                    // Update UI with received data
                    tabFragment.updateUI(thumbnailData, receivedTime, receivedTitle);
                    status="UPDATED1";
                    isUpdated=false;
                //}
            }
            if (firstByte == 'C') {
                Log.d(TAG, "processReceivedC: " + data.length + " " + length);

                int thumbnailLength = decodeVLQ(data, 1); // Decode VLQ-encoded length
                int thumbnailIndex=1+computeVLQSize(thumbnailLength);
                int timeIndex = 1 + thumbnailLength+computeVLQSize(thumbnailLength); // Calculate index of time data
                int titleIndex = timeIndex + TIME_LENGTH; // Calculate index of title data

                Log.d(TAG, "thumbnail length: " + thumbnailLength);
                Log.d(TAG, "thumbnail index: " + thumbnailIndex);
                Log.d(TAG, "timeIndex: "+timeIndex);
                Log.d(TAG, "titleIndex: "+titleIndex);
                //if (data.length >= titleIndex + thumbnailLength + TIME_LENGTH) {
                // Process thumbnail data (data[titleIndex] to data[titleIndex + thumbnailLength - 1])
                byte[] thumbnailData = Arrays.copyOfRange(data, thumbnailIndex,  timeIndex);

                // Extract time data (data[timeIndex] to data[timeIndex + TIME_LENGTH - 1])
                byte[] timeData = Arrays.copyOfRange(data, timeIndex, titleIndex);

                Log.d(TAG, "time data: " + Arrays.toString(timeData));
                Date date=convertBytesToDate(timeData);
                String receivedTime1 = "This page is opened "+getTimeAgo(date);
                String receivedTime =getTimeAgo(date);


                // Extract title data (data[titleIndex + thumbnailLength + TIME_LENGTH] onwards)
                byte[] titleData = Arrays.copyOfRange(data, titleIndex, data.length);

                String receivedTitle = new String(titleData, Charset.forName("UTF-8"));
                Log.d(TAG, "title：" + receivedTitle);
                //tabFragment.updateUI(thumbnailData, receivedTime, receivedTitle);
                //tabFragment.updateUI2(thumbnailData, receivedTime, receivedTitle,receivedTitle, receivedTime);
                // Update UI with received data
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tabFragment.updateUI(thumbnailData, receivedTime, receivedTitle);
                        tabFragment.updateUI2();
                    }
                });
                status="UPDATED";
                isUpdated=false;
                //}
            }
        }
    }


    private static int decodeVLQ(byte[] data, int offset) {
        int result = 0;
        int shift = 0;
        byte b;
        do {
            b = data[offset++];
            result |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return result;
    }
    private int computeVLQSize(int value) {
        int size = 0;
        do {
            value >>= 7;
            size++;
        } while (value > 0);
        return size;
    }
    private Date convertBytesToDate(byte[] bytes) {
        try {
            String dateString = new String(bytes, Charset.forName("UTF-8"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
    public static String getTimeAgo(Date date) {
        long timeDifferenceMillis = System.currentTimeMillis() - date.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis);
        long days = TimeUnit.MILLISECONDS.toDays(timeDifferenceMillis);

        if (minutes < 60) {
            if(minutes<=1){
                return minutes + " minute ago";
            }
            return minutes + " minutes ago";
        } else if (hours < 24) {
            if(hours<=1){
                return hours + " hour ago";
            }
            return hours + " hours ago";
        } else {
            if(days<=1){
                return days + " day ago";
            }
            return days + " days ago";
        }
    }


    public void setTabFragment(TabFragment fragment) {
        if (tabFragment == null) {
            this.tabFragment = fragment;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing sockets", e);
        }
    }
}
