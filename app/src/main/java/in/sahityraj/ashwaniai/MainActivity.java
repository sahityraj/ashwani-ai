
package in.sahityraj.ashwaniai;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Typeface;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextToSpeech speaker;
    private EditText messageInput;
    private TextView chatOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        speaker = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                speaker.setLanguage(new Locale("hi", "IN"));
            }
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 32, 24, 24);

        TextView title = new TextView(this);
        title.setText("ASHWANI AI");
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("आपका Personal AI Assistant");
        subtitle.setTextSize(16);
        layout.addView(subtitle);

        chatOutput = new TextView(this);
        chatOutput.setText("\nनमस्ते! मैं Ashwani AI हूँ।\nआप क्या करना चाहते हैं?");
        chatOutput.setTextSize(18);
        layout.addView(chatOutput);

        messageInput = new EditText(this);
        messageInput.setHint("यहाँ अपना संदेश लिखें...");
        layout.addView(messageInput);

        Button sendButton = new Button(this);
        sendButton.setText("Send Message");
        layout.addView(sendButton);

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();

            if (!message.isEmpty()) {
                chatOutput.setText("आपका संदेश:\n" + message
                    + "\n\nAI सर्वर अभी कनेक्ट नहीं है।");
                speaker.speak(
                    "आपका संदेश मिल गया। AI सर्वर अभी कनेक्ट नहीं है।",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "chat_reply"
                );
                messageInput.setText("");
            }
        });

        Button researchButton = new Button(this);
        researchButton.setText("Internet Research");
        layout.addView(researchButton);

        researchButton.setOnClickListener(v -> {
            String query = messageInput.getText().toString().trim();

            if (query.isEmpty()) {
                messageInput.setText("आज की ताजा खबरें");
            } else {
                Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q="
                        + Uri.encode(query))
                );
                startActivity(intent);
            }
        });

        Button plannerButton = new Button(this);
        plannerButton.setText("My Planner");
        layout.addView(plannerButton);

        plannerButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("My Planner")
                .setMessage("अपना काम और पढ़ाई की योजना बनाएँ।")
                .setPositiveButton("OK", null)
                .show();
        });

        setContentView(layout);
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
