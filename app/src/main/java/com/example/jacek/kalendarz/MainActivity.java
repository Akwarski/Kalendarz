package com.example.jacek.kalendarz;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CalendarView calendarView;
    Button add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //*******************************************************************************************
        // Powoduję że po odpaleniu aplikacji kursor nie wędruję do editText
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //*******************************************************************************************

        calendarView = findViewById(R.id.my_calendar);

        final LinearLayout LL = findViewById(R.id.MyListView);
        final EditText txt = findViewById(R.id.addText);


        FloatingActionButton add = findViewById(R.id.add_box);

        ListView LV = new ListView(this);
        ArrayList<String> list = new ArrayList<String>();
        ArrayAdapter<String> adapter;

        LL.addView(LV);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String checkTXT = txt.getText().toString().trim();

                if(TextUtils.isEmpty(checkTXT)) {
                    Toast.makeText(MainActivity.this, "Add Something please", Toast.LENGTH_SHORT).show();
                }
                else{

                }
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

            }
        });

    }
}
