package in.sahityraj.ashwaniai;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout root, content, chatArea;
    private EditText messageInput;
    private TextView statusText;
    private SharedPreferences prefs;
    private TextToSpeech tts;

    private static final int VOICE_REQUEST = 101;
    private static final int DOCUMENT_REQUEST = 102;
    private static final String REMINDER_ACTION =
            "in.sahityraj.ashwaniai.REMINDER";

    private String language = "hi";
    private JSONArray tasks = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("AshwaniAI", MODE_PRIVATE);
        language = prefs.getString("language", "hi");

        try {
            tasks = new JSONArray(prefs.getString("tasks", "[]"));
        } catch (Exception e) {
            tasks = new JSONArray();
        }

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                setTtsLanguage();
            }
        });

        requestNotificationPermission();
        showHome();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private TextView makeText(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(0xFF202A44);
        text.setPadding(dp(12), dp(10), dp(12), dp(10));
        return text;
    }

    private Button makeButton(String value, Runnable action) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void setupPage(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F6FB);

        TextView header = makeText("ASHWANI AI", 24);
        header.setTextColor(0xFFFFFFFF);
        header.setGravity(Gravity.CENTER);
        header.setBackgroundColor(0xFF172554);
        root.addView(header, new LinearLayout.LayoutParams(
                -1, dp(64)));

        statusText = makeText(title, 17);
        statusText.setGravity(Gravity.CENTER);
        root.addView(statusText);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(16));
        scroll.addView(content);

        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(0xFFE0E7FF);

        nav.addView(makeButton("Home", this::showHome),
                new LinearLayout.LayoutParams(0, dp(55), 1));
        nav.addView(makeButton("Chat", this::showChat),
                new LinearLayout.LayoutParams(0, dp(55), 1));
        nav.addView(makeButton("Planner", this::showPlanner),
                new LinearLayout.LayoutParams(0, dp(55), 1));
        nav.addView(makeButton("Settings", this::showSettings),
                new LinearLayout.LayoutParams(0, dp(55), 1));

        root.addView(nav);
        setContentView(root);
    }

    private void showHome() {
        setupPage("आपका व्यक्तिगत AI सहायक");

        content.addView(makeText(
                "नमस्ते! मैं Ashwani AI हूँ।", 22));

        content.addView(makeText(
                "आपके सहायक में उपलब्ध सुविधाएँ:", 17));

        content.addView(makeButton("AI Chat खोलें",
                this::showChat));

        content.addView(makeButton("Planner और Reminders",
                this::showPlanner));

        content.addView(makeButton("PDF / Document खोलें",
                this::openDocument));

        content.addView(makeButton("Internet Research",
                this::internetResearch));

        content.addView(makeButton("बोलकर सवाल पूछें",
                this::startVoiceInput));

        content.addView(makeText(
                "नोट: AI उत्तर देने की सुविधा Backend कनेक्शन के बाद चालू होगी।",
                14));
    }

    private void showChat() {
        setupPage("Ashwani AI Chat");

        chatArea = new LinearLayout(this);
        chatArea.setOrientation(LinearLayout.VERTICAL);
        content.addView(chatArea);

        String history = prefs.getString("chat_history", "");
        if (!history.isEmpty()) {
            TextView oldChat = makeText(history, 15);
            oldChat.setBackgroundColor(0xFFFFFFFF);
            chatArea.addView(oldChat);
        }

        messageInput = new EditText(this);
        messageInput.setHint("अपना सवाल यहाँ लिखें...");
        messageInput.setMinLines(2);
        messageInput.setTextSize(16);
        content.addView(messageInput);

        content.addView(makeButton("Send", this::sendMessage));
        content.addView(makeButton("Voice Input",
                this::startVoiceInput));
        content.addView(makeButton("संदेश सुनें",
                this::speakLastMessage));
        content.addView(makeButton("Chat History मिटाएँ",
                this::clearChat));
    }

    private void sendMessage() {
        if (messageInput == null) return;

        String message = messageInput.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "पहले सवाल लिखें",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String history = prefs.getString("chat_history", "");
        history += "\n\nआप: " + message;
        history += "\nAshwani AI: Backend अभी कनेक्ट नहीं है।";

        prefs.edit().putString("chat_history", history).apply();

        messageInput.setText("");
        showChat();

        Toast.makeText(this,
                "सवाल मोबाइल में सेव हो गया है।",
                Toast.LENGTH_SHORT).show();
    }

    private void clearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Chat History")
                .setMessage("क्या आप पुरानी चैट मिटाना चाहते हैं?")
                .setNegativeButton("नहीं", null)
                .setPositiveButton("हाँ", (d, w) -> {
                    prefs.edit().remove("chat_history").apply();
                    showChat();
                })
                .show();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                language.equals("hi") ? "hi-IN" : "en-IN");

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "बोलकर अपना सवाल पूछें");

        try {
            startActivityForResult(intent, VOICE_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Voice recognition उपलब्ध नहीं है।",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void speakLastMessage() {
        String history = prefs.getString("chat_history", "");

        if (history.isEmpty()) {
            Toast.makeText(this, "अभी कोई संदेश नहीं है",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] lines = history.split("\n");
        String last = lines[lines.length - 1];

        if (tts != null) {
            setTtsLanguage();
            tts.speak(last, TextToSpeech.QUEUE_FLUSH, null, "chat_voice");
        }
    }

    private void setTtsLanguage() {
        if (tts == null) return;

        Locale locale = language.equals("hi")
                ? new Locale("hi", "IN")
                : new Locale("en", "IN");

        int result = tts.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this,
                    "इस भाषा की आवाज़ मोबाइल में उपलब्ध नहीं है।",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlanner() {
        setupPage("आपका Planner");

        content.addView(makeText(
                "अपने काम और रिमाइंडर यहाँ सेव करें।", 17));

        content.addView(makeButton("नया काम जोड़ें",
                this::addTask));

        content.addView(makeText("आपके सेव किए गए काम:", 18));

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) continue;

            String title = task.optString("title", "काम");
            long time = task.optLong("time", 0);

            String display = title;
            if (time > 0) {
                display += "\n" + new SimpleDateFormat(
                        "dd-MM-yyyy hh:mm a",
                        Locale.getDefault()).format(new Date(time));
            }

            content.addView(makeText("• " + display, 16));
        }

        content.addView(makeButton("सभी काम मिटाएँ", () -> {
            new AlertDialog.Builder(this)
                    .setMessage("क्या सभी काम मिटाने हैं?")
                    .setNegativeButton("नहीं", null)
                    .setPositiveButton("हाँ", (d, w) -> {
                        tasks = new JSONArray();
                        saveTasks();
                        showPlanner();
                    }).show();
        }));
    }

    private void addTask() {
        EditText input = new EditText(this);
        input.setHint("काम का नाम लिखें");

        new AlertDialog.Builder(this)
                .setTitle("नया काम")
                .setView(input)
                .setNegativeButton("रद्द करें", null)
                .setPositiveButton("अगला", (d, w) -> {
                    String title = input.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "काम का नाम लिखें",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chooseReminderTime(title);
                }).show();
    }

    private void chooseReminderTime(String title) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(this, (timeView, hour, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                long time = calendar.getTimeInMillis();

                try {
                    JSONObject task = new JSONObject();
                    task.put("title", title);
                    task.put("time", time);
                    tasks.put(task);
                    saveTasks();

                    scheduleReminder(title, time);

                    Toast.makeText(this, "काम सेव हो गया",
                            Toast.LENGTH_SHORT).show();

                    showPlanner();
                } catch (Exception e) {
                    Toast.makeText(this, "काम सेव नहीं हो सका",
                            Toast.LENGTH_SHORT).show();
                }

            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), false).show();

        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTasks() {
        prefs.edit().putString("tasks", tasks.toString()).apply();
    }

    private void scheduleReminder(String title, long time) {
        if (time <= System.currentTimeMillis()) {
            Toast.makeText(this,
                    "बीते हुए समय का Reminder नहीं लगाया गया।",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(REMINDER_ACTION);
        intent.setPackage(getPackageName());
        intent.putExtra("title", title);

        int requestCode = (int) (time % Integer.MAX_VALUE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent);
        }
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(intent, DOCUMENT_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Document picker नहीं खुल सका।",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void internetResearch() {
        EditText input = new EditText(this);
        input.setHint("किस विषय पर खोजें?");

        new AlertDialog.Builder(this)
                .setTitle("Internet Research")
                .setView(input)
                .setNegativeButton("रद्द करें", null)
                .setPositiveButton("Search", (d, w) -> {
                    String query = input.getText().toString().trim();

                    if (query.isEmpty()) return;

                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q="
                                    + Uri.encode(query)));

                    startActivity(intent);
                }).show();
    }

    private void showSettings() {
        setupPage("Settings");

        content.addView(makeText("भाषा चुनें", 18));

        content.addView(makeButton("हिंदी", () -> {
            language = "hi";
            prefs.edit().putString("language", language).apply();
            setTtsLanguage();
            Toast.makeText(this, "हिंदी चुनी गई",
                    Toast.LENGTH_SHORT).show();
        }));

        content.addView(makeButton("English", () -> {
            language = "en";
            prefs.edit().putString("language", language).apply();
            setTtsLanguage();
            Toast.makeText(this, "English selected",
                    Toast.LENGTH_SHORT).show();
        }));

        content.addView(makeText(
                "Chat history और Planner डेटा इसी मोबाइल में सेव होते हैं।",
                15));

        content.addView(makeText(
                "AI Backend: अभी कनेक्ट नहीं है।",
                15));

        content.addView(makeText(
                "Voice की उपलब्धता आपके मोबाइल के Speech और TTS इंजन पर निर्भर करती है।",
                14));
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(
                "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    200);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == VOICE_REQUEST) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                showChat();
                messageInput.setText(results.get(0));
            }
        }

        if (requestCode == DOCUMENT_REQUEST) {
            Uri uri = data.getData();

            if (uri != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, getContentResolver()
                        .getType(uri));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this,
                            "इस फाइल को खोलने वाला ऐप नहीं मिला।",
                            Toast.LENGTH_LONG).show();
                }
            }
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


