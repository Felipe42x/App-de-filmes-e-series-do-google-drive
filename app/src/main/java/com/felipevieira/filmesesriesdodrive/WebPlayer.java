package com.felipevieira.filmesesriesdodrive;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import java.util.Timer;

public class WebPlayer extends AppCompatActivity {
    WebView video;
    View decor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timer myTimer = new Timer();
        setFullScreen();

        setContentView(R.layout.activity_web_player);
        decor = getWindow().getDecorView();
        Timer timer = new Timer();
        video = (WebView) findViewById(R.id.webViewVideo);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String codigo = bundle.getString("URL_FILME");
        video.getSettings().setJavaScriptEnabled(true);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        decor.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0){
                    setFullScreen();
                }
            }
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        //setFullScreen();
        video.loadUrl(codigo);

        AlertDialog.Builder alert = new AlertDialog.Builder(WebPlayer.this);
        alert.setTitle("Avisos importantes!");
        alert.setMessage("1 - Aguarde o conteúdo carregar, pode demorar alguns segundos.\n" +
                "2 - O vídeo não começa sozinho, clique no sinal de PLAY para começar a carregar." +
                "\n3 - Depois que o vídeo começar, coloque em tela cheia para uma melhor visualização");
        alert.setPositiveButton("OK",null);
        alert.show();

    }

    public void setFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
