package com.example.btbeacons;

import android.util.Log;

import java.net.URISyntaxException;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class PhoneHome {
    private String ip;
    private Socket mSocket;
//    {
//        try {
//            mSocket = IO.socket("http://192.168.2.50:3319");
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void setIP(String outside_ip){
        ip = "http://" + outside_ip;
        Log.d("socket", "IP received: " + ip);
        try {
            mSocket = IO.socket(ip);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket(){
        return mSocket;
    }
}