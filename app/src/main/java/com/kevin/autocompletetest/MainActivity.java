package com.kevin.autocompletetest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kevin.autocompleteedittext.widget.AutoCompleteEditText;

import java.util.HashMap;

/**
 * Create by KevinTu on 2018/10/15
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initEmail();
    }

    /**
     * 初始化常用的邮箱
     */
    private void initEmail() {
        String[] userInputs = getResources().getStringArray(R.array.auto_complete_emails_input);
        String[] notify = getResources().getStringArray(R.array.auto_complete_emails_notify);
        if (userInputs.length == notify.length && userInputs.length > 0) {
            HashMap autoData = new HashMap<String, String>();
            for (int i = 0; i < userInputs.length; i++) {
                autoData.put(userInputs[i], notify[i]);
            }
            ((AutoCompleteEditText) findViewById(R.id.email)).initEmail(autoData);
        }

    }
}
