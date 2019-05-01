package com.example.speechrecognitionproject;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;


import java.util.concurrent.Future;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    //API key
    private static String speechSubscriptionKey = "f7fb271b4fd24ceeb67c7aec6c2ef625";

    //API location
    private static String serviceRegion = "northcentralus";

    //to detect if it's empty
    private static String toReturn2;

    /**
     * This is the onCreate. lmao.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set spannables
        TextView toitalic = findViewById(R.id.instruction);
        String parse = "To begin with, please click on the button below";
        SpannableString italic = new SpannableString(parse);
        StyleSpan ita = new StyleSpan(Typeface.ITALIC);
        italic.setSpan(ita,0,italic.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //set api request
        int requestCode = 5;
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET}, requestCode);
    }

    /**
     * This onSpeechButtonClicked calls API when the "Start" is clicked.
     * @param v
     */
    public void onSpeechButtonClicked(final View v) {
        //initialize
        //for script
        TextView textResult = (TextView) this.findViewById(R.id.script);
        //for filler counter
        TextView fillerCount = (TextView) this.findViewById(R.id.counter);
        //for level of speech
        TextView level = (TextView) this.findViewById(R.id.levelofSpeech);

        //recognizing speech
        try {
            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
            assert(config != null);

            SpeechRecognizer reco = new SpeechRecognizer(config);
            assert(reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);


            SpeechRecognitionResult result = task.get();
            assert(result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                String storeResult = result.toString();
                String[] toReturn1 = storeResult.split("Recognized text:<");
                toReturn2 = toReturn1[1].replace(">", "");

                textResult.setText(toReturn2);
                fillerCount.setText(getFiller(result.toString()));
                level.setText(getFillerPercentage(toReturn2));
            }
            else {
                // when detect null
                textResult.setText("you may want to speak again");
            }

            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
    }

    /**
     * This getFiller function detects how many times we use a filler.
     * @param speech
     * @return counter
     * @throws IllegalArgumentException
     */
    public String getFiller(final String speech) throws IllegalArgumentException {
        if (speech == null) {
            throw new IllegalArgumentException("Please speak something");
        }
        speech.toLowerCase();
        String[] words = speech.split(" ");
        int countFiller = 0;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("like") || words[i].equals("so") || words[i].equals("actually") || words[i].equals("and")
                    || words[i].equals("right") || words[i].equals("well")) { // more words needed
                countFiller++;
                continue;
            }
            try {
                if ((words[i].equals("i") && words[i + 1].equals("mean")) || (words[i].equals("you") && words[i + 1].equals("know"))
                        || (words[i].equals("kind") && words[i + 1].equals("of"))) { // more words needed
                    countFiller++;
                    continue;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        return Integer.toString(countFiller);
    }

    /**
     * This getFillerPercentage returns the % of fillers in our speech.
     * @param speech
     * @return % of fillers
     * @throws IllegalArgumentException
     */
    public String getFillerPercentage(final String speech) throws IllegalArgumentException {
        if (speech == null) {
            throw new IllegalArgumentException("Please speak something");
        }
        String[] words = speech.split(" ");
        double length = words.length;
        double fillerCount = Double.parseDouble(getFiller(speech));
        if ((fillerCount / length) > 0.3) {
            return "Too many filler!";
        } else if ((fillerCount / length) > 0.1) {
            return "A bit too many filler!";
        } else if ((fillerCount / length) <= 0.05) {
            if (toReturn2.trim() == null
                    || toReturn2.equals(".")
                    || toReturn2 == null) {
                return "Volume up~! You may want to speak again.";
            }
            return "Great speech!";
        }
        return "you may want to speak again";
    }
}

