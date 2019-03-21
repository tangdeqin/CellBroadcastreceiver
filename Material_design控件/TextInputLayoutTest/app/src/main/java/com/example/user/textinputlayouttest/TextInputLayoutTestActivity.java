package com.example.user.textinputlayouttest;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TextInputLayoutTestActivity extends AppCompatActivity {
    private Button button;
    private TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_input_layout_test);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void login(){
        textInputLayout = findViewById(R.id.t2);
        String password = textInputLayout.getEditText().getText().toString();
        if(password.length()<6){
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("密码字数过少");
        }else {
            textInputLayout.setErrorEnabled(false);
            Toast.makeText(TextInputLayoutTestActivity.this,"登陆成功",Toast.LENGTH_LONG).show();
        }

    }
}
