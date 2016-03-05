package com.example.chen.naoc;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BehaviorsActivity extends AppCompatActivity {
    private ListView lvBehaviors = null;
    private static final int SERVER_PORT = 47306;
    private String recvStr = null;
    private MyAdapter behaviorAdapter;
    MulticastSocket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behaviors);
        lvBehaviors = (ListView) findViewById(R.id.behaviorsListView);

        new MyUDPThread("REQUEST").start();

    }

    public class MyAdapter extends BaseAdapter{

        LayoutInflater inflater;
        String[] behaviorsArr;
        public MyAdapter(Context context, String[] behaviorsArr){
            this.inflater = LayoutInflater.from(context);
            this.behaviorsArr = behaviorsArr;
        }
        @Override
        public int getCount() {
            return behaviorsArr.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            view = inflater.inflate(R.layout.item_behaviors_list, null);
            String behavior = behaviorsArr[position];
            TextView textView = (TextView) view.findViewById(R.id.tvBehaviorName);
            textView.setText(behavior);
            return view;
        }
    }

    class MyUDPThread extends Thread{
        public String content;

        public MyUDPThread(String str){
            content = str;
        }

        @Override
        public void run() {
            try {
                InetAddress serverAdd = InetAddress.getByName("255.255.255.255");
                DatagramSocket dSocket = new DatagramSocket();
                mSocket = new MulticastSocket();
                byte data[] = content.getBytes();
                DatagramPacket dSendPacket = new DatagramPacket(data, data.length, serverAdd, SERVER_PORT);
                byte recv[] = new byte[1024];
                DatagramPacket dRecvPacket = new DatagramPacket(recv, recv.length);
                dSocket.send(dSendPacket);
                Log.i("Behaviors", new String("send"));
                dSocket.receive(dRecvPacket);
                recvStr = new String(dRecvPacket.getData());
                Log.i("Behaviors", recvStr);
                JSONObject jRecv = new JSONObject(recvStr);
                String bRobotics = jRecv.getString("bRobotics");
                String stiffness = jRecv.getString("stiffness");
                String volume    = jRecv.getString("volume");
                String behaviorNumber = jRecv.getString("behaviorNumber");
                String behaviors = jRecv.getString("Behaviors");
                Log.i("Behaviors", bRobotics);
                Log.i("Behaviors", stiffness);
                Log.i("Behaviors", volume);
                Log.i("Behaviors", behaviorNumber);
                Log.i("Behaviors", behaviors);

                String[] behaviorsArr = behaviors.split("@");
                if(behaviorsArr == null){
                    Toast.makeText(getApplicationContext(), "没有Behaviors", Toast.LENGTH_LONG).show();
                }
                else{
                    behaviorAdapter = new MyAdapter(getApplicationContext(),behaviorsArr);
                    lvBehaviors.setAdapter(behaviorAdapter);
                }
                Log.i("Behaviors", behaviorsArr[1]);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_behaviors, menu);
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
