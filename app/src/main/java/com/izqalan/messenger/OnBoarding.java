package com.izqalan.messenger;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

public class OnBoarding extends AhoyOnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AhoyOnboarderCard onboarderCard = new AhoyOnboarderCard("Welcome to Collaborative Kitchen",
                "Collab kitchen is a social media app that allows users chat and to join cooking parties.",
                R.drawable.fried_egg);

        AhoyOnboarderCard onboarderCard2 = new AhoyOnboarderCard("Find your wingman or team",
                "Browse through events created by the community.", R.drawable.chat);

        AhoyOnboarderCard onboarderCard3 = new AhoyOnboarderCard("Collab with others or be the host",
                "Create a party and connect with the community.", R.drawable.networking);



        onboarderCard.setBackgroundColor(R.color.white);
        onboarderCard2.setBackgroundColor(R.color.white);
        onboarderCard3.setBackgroundColor(R.color.white);

        List<AhoyOnboarderCard> pages = new ArrayList<>();

        pages.add(onboarderCard);
        pages.add(onboarderCard2);
        pages.add(onboarderCard3);

        for (AhoyOnboarderCard page: pages){

            page.setTitleColor(R.color.black);
            page.setDescriptionColor(R.color.grey_600);

        }

        setFinishButtonTitle("Finish");
        showNavigationControls(false);


        List<Integer> colorList = new ArrayList<>();
        colorList.add(R.color.solidYolk);
        colorList.add(R.color.colorAccent);
        colorList.add(R.color.solidLime);

        setColorBackground(colorList);
        showNavigationControls(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){

            setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));

        }

//        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
//        setFont(face);

        setOnboardPages(pages);

    }

    @Override
    public void onFinishButtonPressed() {

        Intent intent = new Intent(OnBoarding.this, StartActivity.class );
        startActivity(intent);
        finish();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
