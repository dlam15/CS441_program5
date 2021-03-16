package com.example.myapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity2 extends AppCompatActivity {

    private TextToSpeech ttspeech;
    private EditText editText;
    private Button button;
    private String filename = "/data/data/com.example.myapi/files/words.wav";
    private String utterance = "key";
    private File file;
    private MediaPlayer mp;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        button = (Button) findViewById(R.id.button3);
        editText = (EditText) findViewById(R.id.editText);

        file = new File(filename);


        ttspeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                     int lang = ttspeech.setLanguage(Locale.ENGLISH);

                    if(lang == TextToSpeech.LANG_NOT_SUPPORTED || lang == TextToSpeech.LANG_MISSING_DATA){
                        Toast.makeText(MainActivity2.this,"This Language is not supported", Toast.LENGTH_SHORT).show();
                    } else{
                        button.setEnabled(true);
                    }

                } else{
                    Toast.makeText(MainActivity2.this, "Error setting up Text to Speech", Toast.LENGTH_SHORT).show();
                }
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak();

                //used to hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(),0);


            }
        });


    }


    private void speak(){
        text = editText.getText().toString();
        ttspeech.setPitch(1);
        ttspeech.setSpeechRate(1);

        //Needed to be able to write synthesized speech to file
        HashMap<String,String> test = new HashMap<>();
        test.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttspeech.synthesizeToFile(text, null, file, utterance);
        } else {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance);
            ttspeech.synthesizeToFile(text, test, file.getPath());
        }


        ttspeech.setOnUtteranceProgressListener ( new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId){
                if (utteranceId.equals(utterance))
                    try {
                        mp = new MediaPlayer();
                        mp.setDataSource(filename);
                        mp.prepare();
                        mp.start();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }

            @Override
            public void onError(String utteranceId){
            }

            @Override
            public void onStart(String utteranceId){
            }
        });

    }

    @Override
    protected void onDestroy(){
        if(ttspeech != null){
            ttspeech.stop();
            ttspeech.shutdown();
        }
        super.onDestroy();
    }

}