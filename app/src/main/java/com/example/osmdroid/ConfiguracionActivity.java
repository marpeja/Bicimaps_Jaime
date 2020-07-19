package com.example.osmdroid;

import android.app.Activity;
import android.os.Bundle;


public class ConfiguracionActivity extends Activity {

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new ConfiguracionFragment()).commit();
    }
}

