
package in.sahityraj.ashwaniai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private TextToSpeech speaker;
    private EditText messageInput;
    private TextView chatOutput;

    private int blue = Color.rgb(25, 65, 125);
    private int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        speaker = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                speaker.setLanguage(new Locale("hi", "IN"));
            }
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(242, 246, 252));

        TextView header = new TextView(this);
        header.setText("ASHWANI AI");
        header.setTextSize(26);
        header.setTextColor(white);
        header.setTypeface(null, Typeface.BOLD);
        header.setGravity(Gravity.CENTER);
        header.setPadding(16, 24, 16, 24);
        header.setBackgroundColor(blue);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(20, 20, 20, 20);

        TextView welcome = new TextView(this);
        welcome.setText("नमस्ते! मैं Ashwani AI हूँ।\nआपका Personal AI Assistant");
        welcome.setTextSize(21);
        welcome.setTextColor(Color.rgb(30, 45, 70));
        welcome.setPadding(0, 10, 0, 24);
        content.addView(welcome);

        TextView chatTitle = new TextView(this);
        chatTitle.setText("AI CHAT");
        chatTitle.setTextSize(18);
        chatTitle.setTypeface(null, Typeface.BOLD);
        chatTitle.setTextColor(blue);
        content.addView(chatTitle);

        chatOutput = new TextView(this);
        chatOutput.setText("Ashwani AI: नमस्ते! आज मैं आपकी क्या मदद करूँ?");
        chatOutput.setTextSize(17);
        chatOutput.setTextColor(Color.DKGRAY);
        chatOutput.setPadding(12, 18, 12, 18);
        content.addView(chatOutput);

        messageInput = new EditText(this);
        messageInput.setHint("यहाँ अपना संदेश लिखें...");
        messageInput.setTextSize(16);
        content.addView(messageInput);

        Button send = makeButton("Send Message");
        content.addView(send);

        send.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();

            if (message.isEmpty()) {
                messageInput.setError("संदेश लिखें");
                return;
            }

            chatOutput.setText("आप: " + message
                    + "\n\nAshwani AI: AI Backend अभी कनेक्ट नहीं है।");

            if (speaker != null) {
                speaker.speak(
                    "आपका संदेश मिल गया। AI सर्वर अभी कनेक्ट नहीं है।",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "reply"
                );
            }

            messageInput.setText("");
        });

        Button research = makeButton("Internet Research");
        content.addView(research);

        research.setOnClickListener(v -> {
            String query = messageInput.getText().toString().trim();

            if (query.isEmpty()) {
                messageInput.setError("पहले खोजने के लिए विषय लिखें");
                return;
            }

            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q="
                    + Uri.encode(query))
            );

            startActivity(intent);
        });

        Button planner = makeButton("My Planner");
        content.addView(planner);

        planner.setOnClickListener(v -> {
            EditText task = new EditText(this);
            task.setHint("अपना काम लिखें");

            new AlertDialog.Builder(this)
                .setTitle("My Planner")
                .setMessage("आज का काम")
                .setView(task)
                .setPositiveButton("Save", (dialog, which) -> {
                    String value = task.getText().toString().trim();

                    if (!value.isEmpty()) {
                        new AlertDialog.Builder(this)
                            .setTitle("Task Saved")
                            .setMessage(value)
                            .setPositiveButton("OK", null)
                            .show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        TextView footer = new TextView(this);
        footer.setText("\nAshwani AI | Version 1.0");
        footer.setGravity(Gravity.CENTER);
        footer.setTextColor(Color.GRAY);
        content.addView(footer);

        scroll.addView(content);
        root.addView(scroll);

        setContentView(root);
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);
        return button;
    }

    @Override
    protected void onDestroy() {
        if (speaker != null) {
            speaker.stop();
            speaker.shutdown();
        }
        super.onDestroy();
    }
}

