package org.tensorflow.lite.examples.imageclassification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class TutorialActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private SharedPreferences sharedPreferences;
    private TextToSpeech textToSpeech;
    private TextView textOut;

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int RECORD_AUDIO_PERMISSION_CODE = 101;

    private boolean isListeningForCommand = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        textToSpeech = new TextToSpeech(this, this);
        textOut = findViewById(R.id.textOut);

        checkAndRequestPermissions();

        if (!isTutorialShown()) {
            showTutorial();
        }
    }

    private boolean isTutorialShown() {
        return sharedPreferences.getBoolean("tutorial_shown", false);
    }

    private void tutorialShown() {
        sharedPreferences.edit().putBoolean("tutorial_shown", true).apply();
    }

    private void showTutorial() {
        String tutorialText = "Hi, my name is Sera.";
        speechText(tutorialText);
        tutorialText = "Using me is simple.";
        speechText(tutorialText);
        tutorialText = "Once you have started the app.";
        speechText(tutorialText);
        tutorialText = "You point the camera over";
        speechText(tutorialText);
        tutorialText = "to the product you are holding.";
        speechText(tutorialText);
        tutorialText = "Then I will tell you what kind";
        speechText(tutorialText);
        tutorialText = "of product you are holding.";
        speechText(tutorialText);
        tutorialText = "After that,";
        speechText(tutorialText);
        tutorialText = "I will send you back to the start.";
        speechText(tutorialText);
        tutorialText = "You can then start the scan again.";
        speechText(tutorialText);
        tutorialText = "Shall we start?";
        speechText(tutorialText);
        tutorialText = "";
        speechText(tutorialText);
    }

    private void speechText(@NonNull String text) {
        String[] words = text.split(" ");
        final int[] currentIndex = {0};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] < words.length) {
                    textOut.append(words[currentIndex[0]] + " ");
                    currentIndex[0]++;
                    new Handler(Looper.getMainLooper()).postDelayed(this, 200);
                } else {
                    speakText(text);
                    if(text.equals("")){
                        isListeningForCommand = true;
                        promptSpeechInput();
                        startNewActivity();
                    } else{
                        speakText("Error.");
                    }

                }
            }
        };
        new Handler(Looper.getMainLooper()).post(runnable);
    }


    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /** @noinspection deprecation*/
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Optionally set language for TTS
            Locale locale = Locale.getDefault();
            int result = textToSpeech.setLanguage(locale);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // Shut down the TextToSpeech engine when the activity is destroyed
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        textToSpeech.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                processSpokenText(spokenText);
            }
        }
    }

    private void processSpokenText(String spokenText) {
        // Update the TextView with the spoken text
        textOut.setText("Spoken Text: " + spokenText);

        if (isListeningForCommand) {
            if (spokenText.toLowerCase().contains("proceed")) {
                speakText("Thank you! You have successfully completed the tutorial.");
                speakText("Should we proceed?");
                if (isListeningForCommand) {
                    if (spokenText.toLowerCase().contains("proceed")) {
                        startNewActivity();
                    }
                }
            }
            isListeningForCommand = false;
        } else {
            tutorialShown();
        }
    }

    private void checkAndRequestPermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE
            );
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showTutorial();
            } else {
                Toast.makeText(
                        this,
                        "Permission denied. The app may not work as intended.",
                        Toast.LENGTH_SHORT
                ).show();
            }
    }


    protected void startNewActivity() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

}
