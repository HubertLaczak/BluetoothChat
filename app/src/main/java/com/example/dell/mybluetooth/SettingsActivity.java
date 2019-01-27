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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;
import java.util.Set;


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button buttonON, buttonOFF, buttonShowPaired, buttonScan, buttonDiscoverability;
    ListView listViewPaired, listViewScan;
    TextView tvONOFF;
    BluetoothAdapter myBluetoothAdapter, myBluetoothAdapterPaired;

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

        bluetoothONMethod(); //włączenie Bluetootha
        bluetoothOFFMethod(); //wyłączenie Bluetootha

        showButton();

        registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //służy do zakrycia klawiatury
    }

    @Override
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        readNickAndSetTextV();
    }
    private void readNickAndSetTextV(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        txtNickName = sharedPref.getString("NickName", "User");
        WelcomeMessage.setText(getString(R.string.textWelcome) + " " + txtNickName + "!");
    }
    private void changeNickName(String s) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("NickName", s);
        editor.commit();
        Toast.makeText(this, "Zmieniono nick!", Toast.LENGTH_SHORT).show();
        WelcomeMessage.setText(getString(R.string.textWelcome) + " " + s + "!");
    }

    public void findViewByID(){
        WelcomeMessage = findViewById(R.id.WelcomeMessage);
        WelcomeMessage.setText(txtNickName);

        buttonON =  findViewById(R.id.btON);
        buttonOFF = findViewById(R.id.btOFF);
        tvONOFF = findViewById(R.id.tvONOFF); //textview on/off bluetooth
        buttonShowPaired = findViewById(R.id.btShow); //przycisk do pokazywania sparowanych urządzeń
        listViewPaired =  findViewById(R.id.lvPaired); //lista do wyświetlania sparowanych urządzeń

        buttonDiscoverability = findViewById(R.id.btDiscoverability); //aby być widocznym przez 30s?

        buttonScan =  findViewById(R.id.btScan); //przycisk do pokazywania urządzeń w pobliżu
        listViewScan = findViewById(R.id.lvScan); //lista do wyświetlania urządzeń w pobliżu

        btn_changeNick = findViewById(R.id.btn_changeNick);
        text_newNick = findViewById(R.id.text_newNick);
        btn_changeNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNickName(text_newNick.getText().toString());
                text_newNick.setText("");
            }
        });
    }

    public void bDevInit(){
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myBluetoothAdapterPaired = BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listViewScan.setAdapter(BTArrayAdapter);
        listViewScan.setOnItemClickListener(SettingsActivity.this);//musi być do wybierania danego urządzenia do sparowania!

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






    protected void showButton() /*metoda do pokazywania sparowanych urządzeń*/ {
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

    public void clearListView(ListView lv) {
        this.lv = lv;
        lv.setAdapter(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == requestCodeForEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enable!", Toast.LENGTH_SHORT).show();
                bIsOn(); //czy ja mogę to zakomentować>? Sprawdz to. Pytam, bo są dwie wywołania tej funkcji

            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void bluetoothONMethod() {
        buttonON.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
                }
                if (!myBluetoothAdapter.isEnabled()) {
                    startActivityForResult(btEnablingIntent, requestCodeForEnable);
                }
            }
        });
    }

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

    public void startScan(View view) {
        if (myBluetoothAdapterPaired .isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapterPaired .cancelDiscovery();
        } else {
            BTArrayAdapter.clear();
            myBluetoothAdapterPaired.startDiscovery();
        }

    }


    private void checkBTPermissions() {
        /**
         * This method is required for all devices running API23+
         * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
         * in the manifest is not enough.
         *
         * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
         */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
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

                // add the name and the MAC address of the object to the arrayAdapter
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };



    public void startDiscoverability(View view) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30); //for 30second
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Jesteś widoczny przez 30 sekund", Toast.LENGTH_SHORT).show();
    }


    @Override //służy do parowania - trzeba zaimplementować AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        myBluetoothAdapterPaired.cancelDiscovery(); //stopujemy szukanie na początku
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            mBTDevices.get(i).createBond(); //createBond może być użyte dopiero od JellyBean
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
