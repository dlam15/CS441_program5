package com.example.myapi;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech ttspeech;
    private EditText editText;
    private Button start;
    private Button reset;
    private SeekBar pitch;
    private SeekBar speed;
    private Spinner lang;


    private String filename = "/data/data/com.example.myapi/files/words.wav";
    private String utterance = "key";
    private File file;
    private MediaPlayer mp;
    private String text;
    private VisualizeDesign visualizeDesign;
    private Visualizer visualizer;
    private int choice;
    private ArrayList<Locale> languages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.button3);
        reset = (Button) findViewById(R.id.button4);
        editText = (EditText) findViewById(R.id.editText);
        visualizeDesign = (VisualizeDesign) findViewById(R.id.view2);
        pitch = (SeekBar) findViewById(R.id.seekBar);
        speed = (SeekBar) findViewById(R.id.seekBar2);

        file = new File(filename);
        file.setReadable(true,false);


        ttspeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                     int lang = ttspeech.setLanguage(Locale.ENGLISH);

                    if(lang == TextToSpeech.LANG_NOT_SUPPORTED || lang == TextToSpeech.LANG_MISSING_DATA){
                        Toast.makeText(MainActivity.this,"This Language is not supported", Toast.LENGTH_SHORT).show();
                    } else{
                        start.setEnabled(true);
                    }

                } else{
                    Toast.makeText(MainActivity.this, "Error setting up Text to Speech", Toast.LENGTH_SHORT).show();
                }
            }
        });


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synthesize();

                //used to hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(),0);


            }
        });



        //https://developer.android.com/guide/topics/ui/controls/spinner
        //https://stackoverflow.com/questions/9476665/how-to-change-spinner-text-size-and-text-color
        lang = (Spinner) findViewById(R.id.spinner);
        lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //((TextView) parent.getChildAt(0)).setTextSize(20);
                choice = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        languages = new ArrayList<>();
        languages.add(Locale.CHINESE);
        languages.add(Locale.ENGLISH);
        languages.add(Locale.FRENCH);
        languages.add(Locale.GERMAN);
        languages.add(Locale.ITALIAN);
        languages.add(Locale.JAPANESE);
        languages.add(Locale.KOREAN);
        languages.add(Locale.SIMPLIFIED_CHINESE);
        languages.add(Locale.TRADITIONAL_CHINESE);

        ArrayList<String> options = new ArrayList<>();
        options.add("Chinese");
        options.add("English");
        options.add("French");
        options.add("German");
        options.add("Italian");
        options.add("Japanese");
        options.add("Korean");
        options.add("Simplified Chinese");
        options.add("Traditional Chinese");

        ArrayAdapter<String> adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,options);
        lang.setAdapter(adapter);
        lang.setSelection(1);


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lang.setSelection(1);
                pitch.setProgress(50);
                speed.setProgress(50);


            }
        });

    }


    private void synthesize(){
        text = editText.getText().toString();
        float pitchVal = (float) pitch.getProgress() / 50;
        if(pitchVal < 0.1) pitchVal = 0.1f;
        float speedVal = (float) speed.getProgress() / 50;
        if(speedVal < 0.1) speedVal = 0.1f;
        ttspeech.setPitch(pitchVal);
        ttspeech.setSpeechRate(speedVal);

        int lang = ttspeech.setLanguage(languages.get(choice));

        if(lang == TextToSpeech.LANG_NOT_SUPPORTED || lang == TextToSpeech.LANG_MISSING_DATA){
            Toast.makeText(MainActivity.this,"This Language is not supported", Toast.LENGTH_SHORT).show();
        } else{
            start.setEnabled(true);
        }


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
                        speak();
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

    private void speak() throws IOException {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mp = MediaPlayer.create(this, Uri.fromFile(file));

        setupVisualizer();
        visualizer.setEnabled(true);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                visualizer.setEnabled(false);
            }
        });
        mp.start();

    }

    private void setupVisualizer() {

        //needed for the visualizer (make sure that you have the needed permissions)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasAudioPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
            int hasInternetPermission = checkSelfPermission(Manifest.permission.INTERNET);
            List<String> permissions = new ArrayList<>();
            if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e("TTS", "record failed");
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                Log.e("TTS", "internet failed");
                permissions.add(Manifest.permission.INTERNET);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]),0); //Can pass any request code (just an identifier for the request)
            }
        }

        // Create the Visualizer object and attach it to our media player
        visualizer = new Visualizer(mp.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]); //Get the maximum capture size
        visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizeIn,
                                                      byte[] bytes, int samplingRate) {
                        visualizeDesign.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizeDesign,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
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