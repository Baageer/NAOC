package com.example.chen.naoc;

import android.net.wifi.ScanResult;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import java.util.List;

public class ConnectActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private String wifiPassword = null;
    private List<WifiConfiguration> wifiConfigList;//WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
    private WifiInfo wifiConnectedInfo;//已经建立好网络链接的信息
    private MyAdapter arrayWifiAdapter;
    List<ScanResult> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        openWifi();
        list = wifiManager.getScanResults();
        wifiConfigList = wifiManager.getConfiguredNetworks();
        ListView listView = (ListView) findViewById(R.id.connectListView);
        if (list == null) {
            Toast.makeText(this, "wifi未打开！", Toast.LENGTH_LONG).show();
        } else {
            arrayWifiAdapter = new MyAdapter(this, list);
            listView.setAdapter(arrayWifiAdapter);

        }
        ListOnItemClickListener wifiListListener = new ListOnItemClickListener();
        listView.setOnItemClickListener(wifiListListener);


    }



    public void scanResultToString(List<ScanResult> listScan,List<String> listStr){
        for(int i = 0; i <listScan.size(); i++){
            ScanResult strScan = listScan.get(i);
            String str = strScan.SSID+"--"+strScan.BSSID;
            boolean bool = listStr.add(str);
            if(bool){
                arrayWifiAdapter.notifyDataSetChanged();//数据更新,只能单个Item更新，不能够整体List更新
            }
            else{
                Log.i("scanResultToSting","fail");
            }
            Log.i("scanResultToString",listStr.get(i));
        }
    }

    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    //得到Wifi配置好的信息
    public void getConfiguration(){
        wifiConfigList = wifiManager.getConfiguredNetworks();//得到配置好的网络信息
        for(int i =0;i<wifiConfigList.size();i++){
            Log.i("getConfiguration",wifiConfigList.get(i).SSID);
            Log.i("getConfiguration",String.valueOf(wifiConfigList.get(i).networkId));
        }
    }

    public class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        List<ScanResult> list;

        public MyAdapter(Context context, List<ScanResult> list) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            view = inflater.inflate(R.layout.item_wifi_list, null);
            ScanResult scanResult = list.get(position);
            TextView textView = (TextView) view.findViewById(R.id.textViewSSID);
            textView.setText(scanResult.SSID);
            /*
            TextView signalStrenth = (TextView) view.findViewById(R.id.signal_strenth);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            //判断信号强度，显示对应的指示图标
            if (Math.abs(scanResult.level) > 100) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ico_connect, null));
            }
            */

            return view;
        }
    }


    public class ListOnItemClickListener implements AdapterView.OnItemClickListener {
        String wifiItemSSID = null;
        private View selectedItem;

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method stub
            Log.i("ListOnItemClickListener", "start");
            selectedItem = arg1;
            arg1.setBackgroundResource(R.color.gray);//点击的Item项背景设置

            wifiItemSSID = list.get(arg2).SSID;
            Log.i("ListOnItemClickListener",wifiItemSSID);
            int wifiItemId = IsConfiguration("\""+wifiItemSSID+"\"");
            Log.i("ListOnItemClickListener", String.valueOf(wifiItemId));
            if(wifiItemId != -1){
                if(ConnectWifi(wifiItemId)){//连接指定WIFI
                    arg1.setBackgroundResource(R.color.green);
                }
            }
            else{//没有配置好信息，配置
                WifiPswDialog pswDialog = new WifiPswDialog(ConnectActivity.this,new WifiPswDialog.OnCustomDialogListener() {
                    @Override
                    public void back(String str) {
                        // TODO Auto-generated method stub
                        wifiPassword = str;
                        if(wifiPassword != null){
                            int netId = AddWifiConfig(list,wifiItemSSID, wifiPassword);
                            Log.i("WifiPswDialog",String.valueOf(netId));
                            if(netId != -1){
                                getConfiguration();//添加了配置信息，要重新得到配置信息
                                if(ConnectWifi(netId)){
                                    selectedItem.setBackgroundResource(R.color.green);
                                }
                            }
                            else{
                                Toast.makeText(ConnectActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
                                selectedItem.setBackgroundResource(R.color.burlywood);
                            }
                        }
                        else{
                            selectedItem.setBackgroundResource(R.color.burlywood);
                        }
                    }
                });
                pswDialog.show();
            }
        }


        //判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
        public int IsConfiguration(String SSID){
            Log.i("IsConfiguration",String.valueOf(wifiConfigList.size()));
            for(int i = 0; i < wifiConfigList.size(); i++){
                Log.i(wifiConfigList.get(i).SSID,String.valueOf( wifiConfigList.get(i).networkId));
                if(wifiConfigList.get(i).SSID.equals(SSID)){//地址相同
                    return wifiConfigList.get(i).networkId;
                }
            }
            return -1;
        }

        //添加指定WIFI的配置信息,原列表不存在此SSID
        public int AddWifiConfig(List<ScanResult> wifiList,String ssid,String pwd){
            int wifiId = -1;
            for(int i = 0;i < wifiList.size(); i++){
                ScanResult wifi = wifiList.get(i);
                if(wifi.SSID.equals(ssid)){
                    Log.i("AddWifiConfig","equals");
                    WifiConfiguration wifiCong = new WifiConfiguration();
                    wifiCong.SSID = "\""+wifi.SSID+"\"";//\"转义字符，代表"
                    wifiCong.preSharedKey = "\""+pwd+"\"";//WPA-PSK密码
                    wifiCong.hiddenSSID = false;
                    wifiCong.status = WifiConfiguration.Status.ENABLED;
                    wifiId = wifiManager.addNetwork(wifiCong);//将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                    if(wifiId != -1){
                        return wifiId;
                    }
                }
            }
            return wifiId;
        }

        //连接指定Id的WIFI
        public boolean ConnectWifi(int wifiId){
            for(int i = 0; i < wifiConfigList.size(); i++){
                WifiConfiguration wifi = wifiConfigList.get(i);
                if(wifi.networkId == wifiId){
                    while(!(wifiManager.enableNetwork(wifiId, true))){//激活该Id，建立连接
                        Log.i("ConnectWifi",String.valueOf(wifiConfigList.get(wifiId).status));//status:0--已经连接，1--不可连接，2--可以连接
                    }
                    return true;
                }
            }
            return false;
        }

    }
}
