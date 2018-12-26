package com.example.dell.mybluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AboutActivity extends AppCompatActivity {
TextView tekst;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tekst = findViewById(R.id.tVFromAbout);
        getNowTime();



    }


    private void getNowTime() {
        DateFormat df = new SimpleDateFormat("H:m:s");
        Date now = Calendar.getInstance().getTime();
        String text = df.format(now);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                openActivity1();
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

    private void openActivity1() {
        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
    }
}
