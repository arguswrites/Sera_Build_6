package org.tensorflow.lite.examples.imageclassification;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LandingActivity extends AppCompatActivity implements OnInitListener, RecognitionListener {

    private TextToSpeech textToSpeech;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        checkPermission();

        textToSpeech = new TextToSpeech(this, this);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

        greetUser();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());

            // Check if the language is supported
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d(TAG,"Text to Speech not implemented");
                Toast.makeText(LandingActivity.this, "Text-to-Speech not supported on this device.", Toast.LENGTH_SHORT).show();
            } else {
                greetUser();
            }
        } else {
            Toast.makeText(LandingActivity.this, "Initialization failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void greetUser() {
        // Greet the user with a welcome message
        String greeting = "Hello there!";
        textToSpeech.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, null);

        String comm = "Shall we begin?";
        textToSpeech.speak(comm, TextToSpeech.QUEUE_FLUSH, null, null);

        // Start listening for the command after the greeting
        startSpeechRecognition(this.getCurrentFocus());
    }


    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /** @noinspection deprecation*/
    public void startSpeechRecognition(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQUEST_RECORD_AUDIO_PERMISSION);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.e("TextReader", "has closed.");

    }

    @Override
    public void onError(int i) {
        Log.e("SpeechRecognition", "Error: " + i);
        startSpeechRecognition(this.getCurrentFocus());
    }

    @Override
    public void onResults(Bundle results) {
        // Get the speech recognition results
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        List<String> commandList = Arrays.asList("Start", "Yes");
        if (matches != null && !matches.isEmpty()) {
            String command = matches.get(0).toLowerCase();
            Log.d("SpeechRecognition", "Command: " + command);

            for (String match : commandList) {
                if(matches.equals(match)){
                    startMainActivity();
                } else {
                    textToSpeech.speak("Command not match", TextToSpeech.QUEUE_FLUSH, null, null);
                    Log.d("SpeechRecognition", "Command mismatch ");
                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}