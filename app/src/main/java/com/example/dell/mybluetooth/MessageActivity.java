package com.example.dell.mybluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MessageActivity extends AppCompatActivity {

    Button send, listen, listDevices; EditText writeMsg;
    ListView listView;
    TextView status, msg_box;

    ListView mojawiadomosc;
    ArrayAdapter<String> BTArrayAdapter;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;
    BluetoothDevice bdConnected;

    SendReceive sendReceive;

    private static final String APP_NAME = "myChat";
    private static final UUID MY_UUID =java.util.UUID.fromString("f53dcdad-2a0c-47cc-a756-71ccd342deba");

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mojawiadomosc = findViewById(R.id.mojawiadomosc);
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mojawiadomosc.setAdapter(BTArrayAdapter);

        findViewByID();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!bluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        implementListeners();
    }

    private void implementListeners() {


        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;
                if(bt.size()>0){
                    for (BluetoothDevice device : bt)
                    {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index ++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });


        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                 ClientClass clientClass = new ClientClass(btArray[position]);
                 clientClass.start();
                 status.setText("Connecting");
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(getNowTime() + "\n" + "Od " + bdConnected.getName()+ "\n"+ writeMsg.getText() );
                sendReceive.write(string.getBytes());
            }
        });
        writeMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                writeMsg.set
            }
        });

    }

    private String getNowTime() {
        DateFormat df = new SimpleDateFormat("H:m:s");
        Date now = Calendar.getInstance().getTime();
        String text;
        return text = df.format(now);
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting  ");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connecting Failed");
                    break;
                case STATE_MESSAGE_RECEIVED: //zakomentuj ciało tego i sprawdź czy działa
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    BTArrayAdapter.add(tempMsg);

//                    msg_box.setText(tempMsg);
                    break;

            }

            return true;
        }
    });

    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;
        public ServerClass(){
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run(){
            BluetoothSocket socket= null;

            while(socket == null){
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if(socket != null){
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                }
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            this.device = device1;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                bdConnected = device;
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }



    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        Message msg;

        private SendReceive(BluetoothSocket socket){
            this.bluetoothSocket=socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;

        }
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;

            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget(); //to później zakomentuj i sprawdź czy działa!!
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0 ,msg.arg1);
                BTArrayAdapter.add(tempMsg);
                BTArrayAdapter.notifyDataSetChanged();
//                msg_box.setText(tempMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e1) { //droga na opak i na około
                e1.printStackTrace();
            }
        }
    }

    public void findViewByID(){
        listen = findViewById(R.id.btnListen);
        send = findViewById(R.id.btnSend);
        listView = findViewById(R.id.listView);
//        msg_box = findViewById(R.id.tvMessages);
        status = findViewById(R.id.tvStatus);
        writeMsg = findViewById(R.id.etMessage);
        listDevices = findViewById(R.id.btnDeviceList);
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item3:
//                openActivity3();
                return true;
            case R.id.item2:
                openActivity2();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openActivity2() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    private void openActivity3() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
