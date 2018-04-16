package com.example.jacek.kalendarz;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    //*******************************************************************************************
    //Krok1
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    //*******************************************************************************************

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



        //*******************************************************************************************
        //Krok2
        // Aby można było bez logowania dodawać (logowanie anonimowe)
        mAuth = FirebaseAuth.getInstance();
        // *******************************************************************************************

        calendarView = findViewById(R.id.my_calendar);

        final LinearLayout LL = findViewById(R.id.MyListView);
        final EditText txt = findViewById(R.id.addText);


        FloatingActionButton add = findViewById(R.id.add_box);

        ListView LV = new ListView(this);
        ArrayList<String> list = new ArrayList<String>();
        ArrayAdapter<String> adapter;

        LL.addView(LV);

        //*******************************************************************************************
        //Krok4
        // Wywołanie funkcji:
        signInAnonymously();
        // *******************************************************************************************

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

    //*******************************************************************************************
    //Krok 3
    //Funkcja:
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Anonymous bijacz", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(MainActivity.this, MainActivity.class));
                    //finish();
                } else {
                    Toast.makeText(MainActivity.this, "U are not Anonymous", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //*******************************************************************************************


    //*******************************************************************************************

    //*******************************************************************************************
}
