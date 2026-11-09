
package in.sahityraj.ashwaniai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    TextToSpeech tts;
    TextView status;
    static final int VOICE_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 35, 28, 25);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.rgb(12, 18, 35));

        TextView title = new TextView(this);
        title.setText("ASHWANI AI");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 20, 0, 25);
        layout.addView(title);

        TextView welcome = new TextView(this);
        welcome.setText("नमस्ते! मैं आपका AI Assistant हूँ।");
        welcome.setTextSize(19);
        welcome.setTextColor(Color.WHITE);
        welcome.setGravity(Gravity.CENTER);
        layout.addView(welcome);

        status = new TextView(this);
        status.setText("आपका आदेश सुनने के लिए तैयार हूँ।");
        status.setTextColor(Color.rgb(100, 220, 180));
        status.setTextSize(16);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 30, 10, 30);
        layout.addView(status);

        Button voice = new Button(this);
        voice.setText("🎤 बोलकर आदेश दें");
        layout.addView(voice);

        Button time = new Button(this);
        time.setText("अभी कितना बजा है?");
        layout.addView(time);

        Button date = new Button(this);
        date.setText("आज की तारीख");
        layout.addView(date);

        Button youtube = new Button(this);
        youtube.setText("YouTube खोलें");
        layout.addView(youtube);

        voice.setOnClickListener(v -> listen());
        time.setOnClickListener(v -> tellTime());
        date.setOnClickListener(v -> tellDate());
        youtube.setOnClickListener(v -> openYouTube());

        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
            }
        });
    }

    private void listen() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "अपना आदेश बोलिए");

            startActivityForResult(intent, VOICE_CODE);
        } catch (Exception e) {
            status.setText("Voice Recognition उपलब्ध नहीं है।");
            speak("वॉइस सुविधा अभी उपलब्ध नहीं है।");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_CODE && resultCode == RESULT_OK
                && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                String command = results.get(0).toLowerCase(Locale.ROOT);
                status.setText("आपने कहा: " + command);
                handleCommand(command);
            }
        }
    }

    private void handleCommand(String command) {

        if (command.contains("समय") || command.contains("कितना बजा")
                || command.contains("time")) {
            tellTime();
        }
        else if (command.contains("तारीख") || command.contains("date")) {
            tellDate();
        }
        else if (command.contains("यूट्यूब")
                || command.contains("youtube")) {
            openYouTube();
        }
        else if (command.contains("कॉल") || command.contains("call")) {
            String number = command.replaceAll("[^0-9]", "");

            if (number.length() >= 10) {
                new AlertDialog.Builder(this)
                        .setTitle("कॉल की पुष्टि")
                        .setMessage("क्या इस नंबर को Dialer में खोलना है?\n" + number)
                        .setPositiveButton("हाँ", (d, w) -> {
                            Intent dial = new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + number));
                            startActivity(dial);
                        })
                        .setNegativeButton("नहीं", null)
                        .show();
            } else {
                speak("कृपया नंबर बोलकर बताइए।");
            }
        }
        else if (command.contains("फोन अनलॉक")
                || command.contains("फोन लॉक")) {
            speak("फोन की सुरक्षा के कारण मैं इसे अनलॉक या लॉक नहीं कर सकता।");
        }
        else {
            speak("माफ कीजिए, यह आदेश अभी समझ नहीं आया।");
        }
    }

    private void tellTime() {
        String time = new SimpleDateFormat(
                "hh:mm a", Locale.getDefault()).format(new Date());
        status.setText("अभी समय है: " + time);
        speak("अभी समय है " + time);
    }

    private void tellDate() {
        String date = new SimpleDateFormat(
                "dd MMMM yyyy", new Locale("hi", "IN"))
                .format(new Date());
        status.setText("आज की तारीख: " + date);
        speak("आज की तारीख है " + date);
    }

    private void openYouTube() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com"));
        startActivity(intent);
        speak("यूट्यूब खोल रहा हूँ।");
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "ashwani");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
            }
        
