package com.example.osmdroid.Modelo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DialogoSelectorFecha extends DialogFragment {
    DatePickerDialog.OnDateSetListener escuchador;

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener escuchador){
        this.escuchador = escuchador;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Calendar calendario = Calendar.getInstance();
        Bundle args = this.getArguments();
        if(args != null){
            Long fecha = args.getLong("fecha");
            calendario.setTimeInMillis(fecha);
        }
        int anyo = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), escuchador, anyo, mes, dia);
    }
}