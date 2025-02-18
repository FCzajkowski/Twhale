package com.example.twhale;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText enterText;
    private Button translateButton, flashlightButton, saveMp3Button, saveTxtButton;
    private TextView morseOutput, text2, text3;
    private CameraManager cameraManager;
    private String cameraId;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_CAMERA_PERMISSION = 2001;
    private MorseTranslator morseTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enterText = findViewById(R.id.enter_text);
        translateButton = findViewById(R.id.translate_button);
        morseOutput = findViewById(R.id.morse_output);
        flashlightButton = findViewById(R.id.flashlight_button);
        saveMp3Button = findViewById(R.id.save_mp3_button);
        saveTxtButton = findViewById(R.id.save_txt_button);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);

        morseOutput.setVisibility(View.GONE);
        flashlightButton.setVisibility(View.GONE);
        saveMp3Button.setVisibility(View.GONE);
        saveTxtButton.setVisibility(View.GONE);
        morseTranslator = new MorseTranslator();

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            System.out.println("You thought that camera was working, but It was me, ERROR");
        }

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        translateButton.setOnClickListener(v -> translateToMorse());
        flashlightButton.setOnClickListener(v -> blinkFlashlight());
        saveMp3Button.setOnClickListener(v -> saveMorseAsMp3());
        saveTxtButton.setOnClickListener(v -> saveMorseAsTxt());
    }

    private void translateToMorse() {
        String input = enterText.getText().toString();
        if (input.isEmpty()) {
            morseOutput.setText("Please enter text to translate.");
            return;
        }

        String morseCode = morseTranslator.toMorse(input);
        morseOutput.setText(morseCode);

        enterText.setVisibility(View.GONE);
        translateButton.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);
        text3.setVisibility(View.GONE);
        morseOutput.setVisibility(View.VISIBLE);
        flashlightButton.setVisibility(View.VISIBLE);
        saveMp3Button.setVisibility(View.VISIBLE);
        saveTxtButton.setVisibility(View.VISIBLE);
    }

    private void blinkFlashlight() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        String morseCode = morseOutput.getText().toString();
        if (!morseCode.isEmpty()) {
            new Thread(() -> playMorseFlash(morseCode)).start();
        }
    }

    private void playMorseFlash(String morseCode) {
        Handler handler = new Handler(getMainLooper());
        int dotDuration = 400;
        int dashDuration = 800;
        int spaceDuration = dotDuration * 3;
        int wordPause = dotDuration * 7;

        long currentTime = 0;

        for (char symbol : morseCode.toCharArray()) {
            int duration;
            if (symbol == '.') {
                duration = dotDuration;
            } else if (symbol == '-') {
                duration = dashDuration;
            } else if (symbol == ' ') {
                currentTime += wordPause;
                continue;
            } else {
                currentTime += spaceDuration;
                continue;
            }

            handler.postDelayed(() -> toggleFlashlight(true), currentTime);
            currentTime += duration;

            handler.postDelayed(() -> toggleFlashlight(false), currentTime);
            currentTime += dotDuration;
        }
    }

    private void toggleFlashlight(boolean state) {
        try {
            cameraManager.setTorchMode(cameraId, state);
        } catch (CameraAccessException e) {
            morseOutput.setText("Error while accessing camera");
        }
    }

    private void saveMorseAsMp3() {
        String morseText = morseOutput.getText().toString();
        if (morseText.isEmpty()) {
            morseOutput.setText("No Morse code to save.");
            return;
        }

        if (textToSpeech == null) {
            morseOutput.setText("TTS is not initialized.");
            return;
        }

        File path = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (path == null) {
            morseOutput.setText("Storage unavailable.");
            return;
        }

        File file = new File(path, "morse_translation.mp3");

        textToSpeech.synthesizeToFile(morseText, null, file.getAbsolutePath());

        morseOutput.setText("Saved: " + file.getAbsolutePath());
    }

    private void saveMorseAsTxt() {
        String morseText = morseOutput.getText().toString();
        if (morseText.isEmpty()) {
            morseOutput.setText("No Morse code to save.");
            return;
        }

        File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (path == null) {
            morseOutput.setText("Storage unavailable.");
            return;
        }

        File file = new File(path, "morse_translation.txt");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(morseText);
            writer.flush();
            morseOutput.setText("Saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            morseOutput.setText("Error saving Morse code.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            blinkFlashlight();
        }
    }
}
