package com.example.chen.naoc;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.logging.LogRecord;

public class ControlActivity extends AppCompatActivity {
    private static final int SERVER_PORT = 47306;
    private String recvStr = null;
    MulticastSocket mSocket;
    String buffer = "";

    private EditText etContent = null;
    private Button btCtrlForward = null;
    private Button btCtrlBackWard = null;
    private Button btCtrlLeft = null;
    private Button btCtrlRight = null;
    private TextView tvReceived = null;

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x11) {
                Bundle bundle = msg.getData();
                tvReceived.append("server:"+bundle.getString("msg")+"\n");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        btCtrlForward = (Button) findViewById(R.id.btCtrlForward);
        btCtrlBackWard = (Button) findViewById(R.id.btCtrlBackward);
        btCtrlLeft = (Button) findViewById(R.id.btCtrlLeft);
        btCtrlRight = (Button) findViewById(R.id.btCtrlRight);



    }

    class MyThread extends Thread {
        public String content;
        public float DistanceX;
        public float DistanceY;
        public float Theta;

        public MyThread(String str , float x, float y, float t){
            DistanceX = x;
            DistanceY = y;
            Theta = t;
            content = str;
        }
        @Override
        public void run(){
            //Message msg = new Message();
            //msg.what = 0x11;
            //Bundle bundle = new Bundle();
            //bundle.clear();
            try{
                InetAddress serverAdd = InetAddress.getByName("255.255.255.255");
                DatagramSocket dSocket = new DatagramSocket();
                mSocket = new MulticastSocket();
                content = DistanceX + "#" + DistanceY + "#" + Theta;
                byte data[] = content.getBytes();
                DatagramPacket dSendPacket = new DatagramPacket(data, data.length, serverAdd, SERVER_PORT);
                byte recv[] = new byte[1024];
                DatagramPacket dRecvPacket = new DatagramPacket(recv, recv.length);

                dSocket.send(dSendPacket);

                dSocket.receive(dRecvPacket);
                recvStr = new String(dRecvPacket.getData());

                //bundle.putString("msg", buffer.toString());
                //msg.setData(bundle);
                //myHandler.sendMessage(msg);

            }catch (SocketTimeoutException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
