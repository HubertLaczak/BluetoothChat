package com.example.dell.mybluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button buttonON, buttonOFF, buttonShowPaired, buttonScan, buttonDiscoverability;
    ListView listViewPaired, listViewScan;
    TextView tvONOFF;
    BluetoothAdapter myBluetoothAdapter; // Reprezentuje kartę Bluetooth urządzenia lokalnego. BluetoothAdapter pozwala wykonywać podstawowe zadania

    TextView WelcomeMessage;
    String txtNickName;

    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    ArrayAdapter<String> BTArrayAdapter;


    Button  btn_changeNick;
    EditText text_newNick;
    SharedPreferences sharedPref;


    Intent btEnablingIntent;
    int requestCodeForEnable;
    private ListView lv;    //służy jako argument do czyszczenia listy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewByID();
        bDevInit();

        bluetoothONMethod();    //włączenie Bluetootha
        bluetoothOFFMethod();   //wyłączenie Bluetootha

        showButton();   //metoda do pokazywania sparowanych urządzeń
    }

    @Override //obsługa wyglądu UI dla bt.ON/bt.OFF
    protected void onStart() {
        super.onStart();
        if (myBluetoothAdapter.isEnabled()) {
            bIsOn();
        } else {
            if (!myBluetoothAdapter.isEnabled()) {
                bIsOff();
            }
        }
    }
    @Override //unregister receiver
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }
    @Override //zczywanie nicku
    protected void onResume() {
        super.onResume();
        readNickAndSetTextV();
    }

    public void findViewByID(){
        WelcomeMessage = findViewById(R.id.WelcomeMessage);
        WelcomeMessage.setText(txtNickName);

        buttonON =  findViewById(R.id.btON);
        buttonOFF = findViewById(R.id.btOFF);
        tvONOFF = findViewById(R.id.tvONOFF);
        buttonShowPaired = findViewById(R.id.btShow);
        listViewPaired =  findViewById(R.id.lvPaired);

        buttonDiscoverability = findViewById(R.id.btDiscoverability);

        buttonScan =  findViewById(R.id.btScan);
        listViewScan = findViewById(R.id.lvScan);

        btn_changeNick = findViewById(R.id.btn_changeNick);
        text_newNick = findViewById(R.id.text_newNick);
        btn_changeNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickName(text_newNick.getText().toString());
                text_newNick.setText("");
            }
        });

        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //służy do zakrycia klawiatury
    }

    public void bDevInit(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //Get a handle to the default local Bluetooth adapter
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listViewScan.setAdapter(BTArrayAdapter);
        listViewScan.setOnItemClickListener(SettingsActivity.this);//musi być do wybierania danego urządzenia do sparowania!
    }

    //zczytywanie nicku
    private void readNickAndSetTextV(){
        sharedPref = getApplicationContext().getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE); //
        txtNickName = sharedPref.getString("NickName", "User");
        WelcomeMessage.setText(getString(R.string.textWelcome) + " " + txtNickName + "!");
    }
    //zapisywanie nicku
    private void changeNickName(String s) {
        sharedPref = getApplicationContext().getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("NickName", s);
        editor.commit();
        Toast.makeText(this, "Zmieniono nick!", Toast.LENGTH_SHORT).show();
        WelcomeMessage.setText(getString(R.string.textWelcome) + " " + s + "!");
    }

    //metoda do pokazywania sparowanych urządzeń
    private void showButton()  {
        buttonShowPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> btBondedSet = myBluetoothAdapter.getBondedDevices();
                String[] strings = new String[btBondedSet.size()];
                int index = 0;

                if (btBondedSet.size() > 0) {
                    for (BluetoothDevice device : btBondedSet) {
                        strings[index] = device.getName() + "\n" + device.getAddress();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    listViewPaired.setAdapter(arrayAdapter);
                }
            }
        });
    }

    private void bluetoothONMethod() {
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
                }
                if (!myBluetoothAdapter.isEnabled()) {
                    startActivityForResult(btEnablingIntent, requestCodeForEnable); //nie zostanie zwrócone onActivityResult
                }
            }
        });
    }

    //wyłączanie Bluetooth przyciskiem
    private void bluetoothOFFMethod() {
        buttonOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter.isEnabled()) {
                    myBluetoothAdapter.disable();
                    clearListView(listViewPaired); //czyszczenie listy
                    clearListView(listViewScan); //czyszczenie listy
                    bIsOff();
                }
            }
        });
    }
    //obsługa włączenia skanowania okolicy
    public void startScan(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            myBluetoothAdapter.cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapter.startDiscovery();
        }
    }

    // w okolicy!
    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBTDevices.add(device);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };


    //obsługa bycia widocznym
    public void startDiscoverability(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30); //for 30second
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Jesteś widoczny przez 30 sekund", Toast.LENGTH_SHORT).show();
    }

    @Override //służy do parowania - trzeba zaimplementować AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) { //long: The row id of the item that was clicked.
        myBluetoothAdapter.cancelDiscovery(); //stopujemy szukanie na początku
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBTDevices.get(i).createBond(); //createBond może być użyte dopiero od JellyBean
    }

    public void bIsOn(){
        tvONOFF.setText(R.string.bluetoothON);
        tvONOFF.setTextColor(Color.rgb(0,255,0));
        buttonON.setEnabled(false);
        buttonOFF.setEnabled(true);
    }
    public void bIsOff(){
        tvONOFF.setText(R.string.bluetoothOFF);
        tvONOFF.setTextColor(Color.rgb(255,0,0));
        buttonON.setEnabled(true);
        buttonOFF.setEnabled(false);

    }
    public void clearListView(ListView lv) {
        this.lv = lv;
        lv.setAdapter(null);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case R.id.item1:
                openActivity1();
                break;
            case R.id.item2:
                Toast.makeText(this, "This window already opened", Toast.LENGTH_SHORT).show();
                break;
            case R.id.item3:
                openActivity3();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    private void openActivity3 () {
        Intent intent = new Intent(this, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void openActivity1 () {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
