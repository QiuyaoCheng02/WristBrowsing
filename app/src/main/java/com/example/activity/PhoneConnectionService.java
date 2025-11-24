package com.example.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;

import helper.WebPage;

public class PhoneConnectionService extends Service {

    private String Currenturl=null;
    private static final String TAG = "ConnectionServicePhone";
    private static final String WATCH_IP = "172.20.10.2"; // 手表的IP地址
    private static final int WATCH_PORT = 8000; // 手表监听的端口号

    private static String currentUrl;
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private InputStream mInputStream;
    private static OutputStream mOutputStream;
    private Handler uiHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "started phone" );
        uiHandler = new Handler(Looper.getMainLooper());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectToWatch();
        Log.d(TAG, "connected phone" );
        showConnectedToast();

        return START_STICKY; // 使用START_STICKY标志，使得服务在被异常终止后自动重启
    }
    private void showConnectedToast() {
        // 在UI线程中显示Toast消息
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PhoneConnectionService.this, "Connected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 因为我们不需要绑定此服务，所以返回null
    }

    private void connectToWatch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = new Socket();
                    mSocket.connect(new InetSocketAddress(WATCH_IP, WATCH_PORT), 5000);

                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    Log.d(TAG, "Connected to watch: " + mSocket.getInetAddress());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] buffer = new byte[1024];
                            int bytes;
                            while (true) {
                                try {
                                    bytes = mInputStream.read(buffer);
                                    String receivedData = new String(buffer, 0, bytes);
                                    // 在这里处理手表发送过来的数据
                                    Log.d(TAG, "run: " + receivedData);
                                    processReceivedData(receivedData);
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading from InputStream", e);
                                    break;
                                }
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    Log.e(TAG, "Error connecting to watch", e);
                }
            }
        }).start();
    }


 public void processReceivedData(String data){
     Log.d(TAG, "processReceivedData: "+data);
     if ("CONFIRM_SAVE".equals(data)) {
         Log.d(TAG, "confirm " + currentUrl);
         // 启动 WebViewActivity
         Intent webViewIntent = new Intent(getApplicationContext(), webViewActivity.class);
         webViewIntent.putExtra("url", currentUrl);
         webViewIntent.putExtra("save",true);
         webViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 必须设置这个标记
         startActivity(webViewIntent);
         currentUrl = null;
     }
     if ("CONFIRM_NOTSAVE".equals(data)) {
         Log.d(TAG, "CONFIRM_NOTSAVE " + currentUrl);
         // 启动 WebViewActivity
         Intent webViewIntent = new Intent(getApplicationContext(), webViewActivity.class);
         webViewIntent.putExtra("url", currentUrl);
         webViewIntent.putExtra("save",false); // 设置为 false，不进行页面保存
         webViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 必须设置这个标记
         startActivity(webViewIntent);
         currentUrl = null;
     }
     if ("ZOOMIN".equals(data)) {
         Log.d(TAG, "zoomin");
         // 启动 WebViewActivity
         Intent intent = new Intent("zoom-webview-action");
         intent.putExtra("type","zoomin");
         LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
     }
     if ("ZOOMOUT".equals(data)) {
         Log.d(TAG, "zoomout");
         // 启动 WebViewActivity
         Intent intent = new Intent("zoom-webview-action");
         intent.putExtra("type","zoomout");
         LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
     }
     if ("ZOOM".equals(data)) {
         Log.d(TAG, "zoom");
         // 启动 WebViewActivity
         Intent intent = new Intent("zoom-webview-action");
         intent.putExtra("type","zoom");
         LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
     }
 }

    public static void writeToWatch(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(message.getBytes());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error writing to OutputStream", e);
                }
            }
        }).start();
        //Log.d(TAG, "writeToWatch: " +message);
    }
    public static void writeByteToWatch(byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.write(data);
                        Log.d(TAG, "send to watch");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error writing to OutputStream", e);
                }
            }
        }).start();

    }

   public static void sendWebPageDataToWatch(WebPage webPage,boolean isNew) {
       byte[] thumbnail = webPage.getThumbnail();
       Date openTime = webPage.getOpenTime();
       currentUrl=webPage.getUrl();
       byte[] titleBytes = webPage.getTitle().getBytes(Charset.forName("UTF-8"));
       byte[] timeBytes = convertDateToBytes(openTime);

       // Encode thumbnail length using VLQ
       List<Byte> vlqEncodedLength = new ArrayList<>();
       int thumbnailLength = thumbnail.length;
       do {
           byte b = (byte) (thumbnailLength & 0x7F);
           thumbnailLength >>= 7;
           if (thumbnailLength > 0) {
               b |= 0x80;
           }
           vlqEncodedLength.add(b);
       } while (thumbnailLength > 0);

       // Calculate total length of data array
       int totalLength = vlqEncodedLength.size() + thumbnail.length + timeBytes.length + titleBytes.length+1;
       byte[] data = new byte[totalLength];
       Log.d(TAG, "totalLength: "+totalLength);
       Log.d(TAG, "vlqEncodedLength.size(): "+vlqEncodedLength.size());
       // Add identifier for image data (e.g., 'B')
       Log.d(TAG, "thumbnail.length: "+thumbnail.length);
       if(isNew==true){
           data[0] = 'C';
       }
       else if(isNew==false){
           Log.d(TAG, "B");
           data[0] = 'B';
       }


       // Copy VLQ-encoded thumbnail length
       for (int i = 0; i < vlqEncodedLength.size(); i++) {
           data[i + 1] = vlqEncodedLength.get(i);
       }
// Copy time data
       //System.arraycopy(timeBytes, 0, data, 3 + thumbnail.length, timeBytes.length);
       // Copy thumbnail data
       System.arraycopy(thumbnail, 0, data, vlqEncodedLength.size() + 1, thumbnail.length);

       // Copy time data
       System.arraycopy(timeBytes, 0, data, vlqEncodedLength.size() + 1 + thumbnail.length, timeBytes.length);

       // Copy title data
       System.arraycopy(titleBytes, 0, data, vlqEncodedLength.size() + 1 + thumbnail.length + timeBytes.length, titleBytes.length);

       Log.d(TAG, "sendWebPageDataToWatch: " + data.length);
       PhoneConnectionService.writeByteToWatch(data);
       int expectedLength = decodeVLQ(data, 1);
       Log.d(TAG, "send expected length: "+expectedLength);
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



    private static byte[] convertDateToBytes(Date date) {
        Log.d(TAG, "convertDateToBytes: "+date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateString = dateFormat.format(date);
        Log.d(TAG, "datestring: "+dateString);
        Log.d(TAG, "datestring: "+dateString.getBytes(Charset.forName("UTF-8")).length);

        return dateString.getBytes(Charset.forName("UTF-8"));
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

