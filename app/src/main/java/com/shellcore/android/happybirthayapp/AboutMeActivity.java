package com.shellcore.android.happybirthayapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AboutMeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        WebView aboutMe = (WebView) findViewById(R.id.web_about_me);
        aboutMe.setWebViewClient(new WebViewClient());

        aboutMe.loadUrl("http://shellcore.github.io/Perfil-personal/");
    }
}
