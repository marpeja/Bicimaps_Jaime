package com.example.osmdroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.osmdroid.Datos.SliderAdapter;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager slideViewPager;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private SliderAdapter sliderAdapter;
    private int currentPage;
    private Button nextButton;
    private Button prevButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tutorial_activity);

        slideViewPager = findViewById(R.id.sliderViewPager);
        dotsLayout = findViewById(R.id.dots_layout);
        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);

        currentPage = 0;

        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentPage < dots.length -1 ){
                    slideViewPager.setCurrentItem(currentPage + 1);
                } else {
                    boolean from_home = getIntent().getBooleanExtra("from_home", false);
                    Log.i("TUTORIAL_ACTIVITY", "FROM HOME "+ from_home);
                    if(from_home){
                        Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        finish();
                    }
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideViewPager.setCurrentItem(currentPage - 1);
            }
        });

        slideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(TutorialActivity.this, "CAMBIO DE PAGINA", Toast.LENGTH_SHORT).show();
                addDotsIndicator(position);
                currentPage = position;
                if(currentPage == 0){
                    prevButton.setVisibility(View.INVISIBLE);
                    nextButton.setText("Comenzar");
                } else if(currentPage == dots.length - 1){
                    nextButton.setText("Finalizar");
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    prevButton.setText("Anterior");
                    nextButton.setText("Siguiente");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        addDotsIndicator(0);

    }

    public void addDotsIndicator (int position){

        dots = new TextView[7];

        dotsLayout.removeAllViews();

        for(int i = 0; i < dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.colorTransparentGrey));
            dotsLayout.addView(dots[i]);
        }

        if(dots[position] != null){
            dots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }
}
