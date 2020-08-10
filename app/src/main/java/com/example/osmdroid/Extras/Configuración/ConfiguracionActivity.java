package com.example.osmdroid.Extras.Configuración;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class ConfiguracionActivity extends AppCompatActivity {

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new ConfiguracionFragment()).commit();
    }
}

