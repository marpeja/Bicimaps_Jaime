package com.example.osmdroid.Extras.Tutorial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.example.osmdroid.R;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;


    public SliderAdapter (Context context){
        this.context = context;
    }

    public int[] slider_image = {
        R.drawable.ic_bicimap,
        R.drawable.tutorial_1,
        R.drawable.tutorial_2,
        R.drawable.tutorial_3,
        R.drawable.tutorial_4,
        R.drawable.tutorial_5,
        R.drawable.tutorial_6
    };

    public int[] slider_icon = {
            0,
            0,
            R.drawable.ic_white_location_36dp,
            R.drawable.ic_white_hamburger_36dp,
            R.drawable.crear_ruta,
            R.drawable.inicar_navegacion,
            R.drawable.ic_navigation_white_36dp
    };

    public String[] slider_headings = {
            "Bienvenido a Bicimaps",
            "",
            "Explorar",
            "Barra de información",
            "Rutas",
            "Iniciar navegación",
            "Modo navegación"
    };

    public String[] slider_description = {
            "Desliza para comenzar el tutorial",
            "",
            "Mantener pulsado sobre el mapa para seleccionar un destino ",
            "Escribir la dirección de destino y ver el menú de opciones",
            "Crear las posibles rutas",
            "Modo rápido y modo baja contaminación",
            "Seguir las indicaciones hasta llegar al destino"
    };

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position){
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView sliderImageCover = view.findViewById(R.id.tutorial_cover);
        ImageView sliderImageBegin = view.findViewById(R.id.tutorial_begin);
        ImageView sliderScreenshot = view.findViewById(R.id.tutorial_screenshot);
        TextView sliderHeading = view.findViewById(R.id.tutorial_heading);
        ImageView sliderIcon = view.findViewById(R.id.tutorial_icon);
        TextView sliderDescription = view.findViewById(R.id.tutorial_description);

        switch(position){
            case 0:
                sliderImageCover.setVisibility(View.VISIBLE);
                sliderImageBegin.setVisibility(View.INVISIBLE);
                sliderScreenshot.setVisibility(View.INVISIBLE);
                sliderIcon.setVisibility(View.INVISIBLE);
                sliderHeading.setVisibility(View.VISIBLE);
                sliderDescription.setVisibility(View.VISIBLE);
                sliderHeading.setText(slider_headings[position]);
                sliderDescription.setText(slider_description[position]);
                break;
            case 1:
                sliderImageCover.setVisibility(View.INVISIBLE);
                sliderImageBegin.setVisibility(View.VISIBLE);
                sliderScreenshot.setVisibility(View.INVISIBLE);
                sliderIcon.setVisibility(View.INVISIBLE);
                sliderHeading.setVisibility(View.INVISIBLE);
                sliderDescription.setVisibility(View.INVISIBLE);
                break;

            default:
                sliderImageCover.setVisibility(View.INVISIBLE);
                sliderImageBegin.setVisibility(View.INVISIBLE);
                sliderScreenshot.setVisibility(View.VISIBLE);
                sliderIcon.setVisibility(View.VISIBLE);
                sliderHeading.setVisibility(View.VISIBLE);
                sliderDescription.setVisibility(View.VISIBLE);
                sliderScreenshot.setImageResource(slider_image[position]);
                sliderHeading.setText(slider_headings[position]);
                sliderIcon.setImageResource(slider_icon[position]);
                sliderDescription.setText(slider_description[position]);
        }

        container.addView(view);
        return view;
    }
    @Override
    public int getCount() {
        return slider_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem (ViewGroup container, int position, @NonNull Object object){

        container.removeView((ConstraintLayout) object);
    }
}
