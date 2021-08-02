package com.sahyadri.safety;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NumGetter extends AppCompatActivity {
    Intent data = new Intent();
    String text = "";
    static String num;
    //---set the data to pass back---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to hide the top title bar//

        setContentView(R.layout.num_getter);

        EditText txt = findViewById(R.id.editTextPhone);
        Button btn = findViewById(R.id.btn1);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                num = txt.getText().toString();
                Log.d("phn", num);
                Intent intent = new Intent();
                intent.putExtra("editTextValue", num);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    public static Intent test(){
        Intent intent = new Intent();
        intent.putExtra("editTextValue", num);
        return intent;
    }


}
