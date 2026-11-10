package in.sahityraj.ashwaniai;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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

    private static final int VOICE_REQUEST = 101;
    private static final int DOCUMENT_REQUEST = 102;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 200;
    private static final String PREFS = "AshwaniAI";
    private static final String REMINDER_ACTION =
            "in.sahityraj.ashwaniai.REMINDER";

    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout chatArea;
    private EditText messageInput;
    private TextToSpeech tts;
private boolean ttsReady = false;
    private String language = "hi";
    private JSONArray tasks = new JSONArray();
    private JSONArray notes = new JSONArray();
    private String lastAssistantReply = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        language = prefs.getString("language", "hi");
        tasks = readArray("tasks");
        notes = readArray("notes");

        tts = new TextToSpeech(this, status -> {
    if (status == TextToSpeech.SUCCESS) {
        ttsReady = true;
        setTtsLanguage();

        // नया जवाब पहले से तैयार है तो उसे बोलो
        if (!lastAssistantReply.isEmpty()) {
            speakLastMessage();
        }
    } else {
        ttsReady = false;
        toast("Voice सेवा शुरू नहीं हो सकी।");
    }
});

        requestNotificationPermission();
        showHome();
    }

    private JSONArray readArray(String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveArray(String key, JSONArray array) {
        prefs.edit().putString(key, array.toString()).apply();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private TextView makeText(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(0xFFE8EEFF);
        text.setPadding(dp(12), dp(10), dp(12), dp(10));
        return text;
    }

    private Button makeButton(String value, Runnable action) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF2563EB));
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private EditText makeInput(String hint, int lines) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(0xFFFFFFFF);
        input.setHintTextColor(0xFF9AA9C7);
        input.setTextSize(16);
        input.setMinLines(lines);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF60A5FA));
        return input;
    }

    private void setupPage(String title) {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0B1220);

        TextView header = makeText("✦ ASHWANI AI", 23);
        header.setGravity(Gravity.CENTER);
        header.setTextColor(0xFFFFFFFF);
        header.setBackgroundColor(0xFF111F3A);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(62)));

        TextView subtitle = makeText(title, 16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(0xFF93C5FD);
        root.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(18));
        scroll.addView(content);

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(0xFF111F3A);

        addNavButton(nav, "Home", this::showHome);
        addNavButton(nav, "Chat", this::showChat);
        addNavButton(nav, "Notes", this::showNotes);
        addNavButton(nav, "Planner", this::showPlanner);
        addNavButton(nav, "Settings", this::showSettings);

        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(58)));
        setContentView(root);
    }

    private void addNavButton(LinearLayout nav, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setTextColor(0xFFE8EEFF);
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF1E3A66));
        button.setOnClickListener(v -> action.run());
        nav.addView(button, new LinearLayout.LayoutParams(0, dp(52), 1));
    }

    private void addCardText(String value, int size) {
        TextView text = makeText(value, size);
        text.setBackgroundTintList(null);
        text.setBackgroundColor(0xFF16243B);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(5), 0, dp(5));
        content.addView(text, params);
    }

    private void showHome() {
        setupPage("आपका व्यक्तिगत सहायक");

        addCardText("नमस्ते! मैं Ashwani AI हूँ।", 22);
        addCardText("आपके काम, नोट्स और रिमाइंडर एक जगह।", 16);

        content.addView(makeButton("✦ AI Chat खोलें", this::showChat));
        content.addView(makeButton("🎙 बोलकर सवाल पूछें", this::startVoiceInput));
        content.addView(makeButton("📝 Notes खोलें", this::showNotes));
        content.addView(makeButton("📅 Planner और Reminders", this::showPlanner));
        content.addView(makeButton("📄 PDF / Document खोलें", this::openDocument));
        content.addView(makeButton("🌐 Internet Search", this::internetResearch));

        addCardText("Backend स्थिति: AI सेवा अभी कनेक्ट नहीं है। "
                + "स्थानीय Notes, Planner और डिवाइस Voice सुविधाएँ उपलब्ध हैं।", 14);

        String date = new SimpleDateFormat("EEEE, dd MMMM yyyy",
                Locale.getDefault()).format(new Date());
        addCardText("आज: " + date, 14);
    }

    private void showChat() {
        setupPage("Ashwani AI Chat");

        chatArea = new LinearLayout(this);
        chatArea.setOrientation(LinearLayout.VERTICAL);
        content.addView(chatArea);

        String history = prefs.getString("chat_history", "");
        if (!history.isEmpty()) {
            TextView oldChat = makeText(history, 14);
            oldChat.setBackgroundColor(0xFF16243B);
            chatArea.addView(oldChat);
        } else {
            addChatBubble("Ashwani AI", "नमस्ते! अपना सवाल लिखें या बोलें।");
        }

        messageInput = makeInput("अपना सवाल यहाँ लिखें...", 2);
        content.addView(messageInput);
        content.addView(makeButton("Send", this::sendMessage));
        content.addView(makeButton("🎙 Voice Input", this::startVoiceInput));
        content.addView(makeButton("🔊 पिछला जवाब सुनें", this::speakLastMessage));
        content.addView(makeButton("Chat History मिटाएँ", this::clearChat));
    }

    private void addChatBubble(String speaker, String message) {
        TextView bubble = makeText(speaker + ":\n" + message, 15);
        bubble.setBackgroundColor(speaker.equals("आप")
                ? 0xFF1E3A66 : 0xFF16243B);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(4), 0, dp(4));
        chatArea.addView(bubble, params);
    }

    private void sendMessage() {
    if (messageInput == null) return;

    String message = messageInput.getText().toString().trim();

    if (message.isEmpty()) {
        toast("पहले सवाल लिखें।");
        return;
    }

    String reply = handleLocalCommand(message);

    if (reply == null) {
        reply = "यह सवाल AI Backend को भेजने के लिए तैयार है, "
                + "लेकिन असली AI सेवा अभी कनेक्ट नहीं है। "
                + "अभी मैं समय, तारीख़ और ऐप के स्थानीय फीचर संभाल सकता हूँ।";
    }

    String history = prefs.getString("chat_history", "");

    history += "\n\nआप: " + message
            + "\nAshwani AI: " + reply;

    if (history.length() > 12000) {
        history = history.substring(history.length() - 12000);
    }

    prefs.edit().putString("chat_history", history).apply();

    lastAssistantReply = reply;

    messageInput.setText("");
    hideKeyboard();
    showChat();

    // जवाब अपने-आप आवाज़ में सुनाओ
    if (ttsReady) {
        speakLastMessage();
    }
    }

        String reply = handleLocalCommand(message);
        if (reply == null) {
            reply = "यह सवाल AI Backend को भेजने के लिए तैयार है, "
                    + "लेकिन असली AI सेवा अभी कनेक्ट नहीं है। "
                    + "अभी मैं समय, तारीख़ और ऐप के स्थानीय फीचर संभाल सकता हूँ।";
        }

        String history = prefs.getString("chat_history", "");
        history += "\n\nआप: " + message + "\nAshwani AI: " + reply;
        if (history.length() > 12000) {
            history = history.substring(history.length() - 12000);
        }
        prefs.edit().putString("chat_history", history).apply();

        lastAssistantReply = reply;
        messageInput.setText("");
        hideKeyboard();
        showChat();
    }

    private String handleLocalCommand(String message) {
        String q = message.toLowerCase(Locale.ROOT);

        if (q.contains("time") || q.contains("समय") || q.contains("कितने बजे")) {
            return "अभी समय है: " + new SimpleDateFormat("hh:mm a",
                    Locale.getDefault()).format(new Date());
        }

        if (q.contains("date") || q.contains("तारीख") || q.contains("आज कौन")) {
            return "आज की तारीख़ है: " + new SimpleDateFormat(
                    "EEEE, dd MMMM yyyy", Locale.getDefault()).format(new Date());
        }

        if (q.contains("planner") || q.contains("काम दिखाओ")
                || q.contains("मेरे काम")) {
            return "Planner में " + tasks.length()
                    + " काम सेव हैं। Planner टैब खोलें।";
        }

        if (q.contains("notes") || q.contains("नोट्स दिखाओ")) {
            return "आपके Notes सेक्शन में " + notes.length()
                    + " नोट्स सेव हैं। Notes टैब खोलें।";
        }

        return null;
    }

    private void clearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Chat History")
                .setMessage("क्या पुरानी चैट मिटानी है?")
                .setNegativeButton("नहीं", null)
                .setPositiveButton("हाँ", (d, w) -> {
                    prefs.edit().remove("chat_history").apply();
                    lastAssistantReply = "";
                    showChat();
                })
                .show();
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                language.equals("hi") ? "hi-IN" : "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "अपना सवाल बोलें");

        try {
            startActivityForResult(intent, VOICE_REQUEST);
        } catch (Exception e) {
            toast("Voice recognition सेवा इस डिवाइस पर उपलब्ध नहीं है।");
        }
    }

    private void speakLastMessage() {
        String text = lastAssistantReply;
        if (text == null || text.isEmpty()) {
            String history = prefs.getString("chat_history", "");
            int index = history.lastIndexOf("Ashwani AI: ");
            if (index >= 0) {
                text = history.substring(index + "Ashwani AI: ".length()).trim();
            }
        }
        if (text == null || text.isEmpty()) {
            toast("सुनाने के लिए कोई जवाब नहीं है।");
            return;
        }
        if (tts != null) {
            setTtsLanguage();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ashwani_reply");
        }
    }

    private void setTtsLanguage() {
        if (tts == null) return;
        Locale locale = language.equals("hi")
                ? new Locale("hi", "IN") : new Locale("en", "IN");
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            toast("चुनी गई भाषा का TTS voice डिवाइस में उपलब्ध नहीं है।");
        }
    }

    // ---------------- NOTES ----------------

    private void showNotes() {
        setupPage("आपके निजी Notes");

        content.addView(makeButton("+ नया Note बनाएँ", this::addNote));
        addCardText("कुल Notes: " + notes.length(), 15);

        if (notes.length() == 0) {
            addCardText("अभी कोई Note सेव नहीं है।", 15);
        }

        for (int i = 0; i < notes.length(); i++) {
            JSONObject note = notes.optJSONObject(i);
            if (note == null) continue;

            final int index = i;
            String title = note.optString("title", "बिना शीर्षक");
            String body = note.optString("body", "");
            String preview = body.length() > 180
                    ? body.substring(0, 180) + "…" : body;

            TextView card = makeText("📝 " + title + "\n" + preview, 15);
            card.setBackgroundColor(0xFF16243B);
            card.setOnClickListener(v -> showNoteOptions(index));
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, dp(5), 0, dp(5));
            content.addView(card, params);
        }
    }

    private void addNote() {
        showNoteEditor(-1);
    }

    private void showNoteOptions(int index) {
        JSONObject note = notes.optJSONObject(index);
        if (note == null) return;

        String[] options = {"पढ़ें", "Edit करें", "Delete करें"};
        new AlertDialog.Builder(this)
                .setTitle(note.optString("title", "Note"))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        new AlertDialog.Builder(this)
                                .setTitle(note.optString("title", "Note"))
                                .setMessage(note.optString("body", ""))
                                .setPositiveButton("बंद करें", null)
                                .show();
                    } else if (which == 1) {
                        showNoteEditor(index);
                    } else {
                        confirmDeleteNote(index);
                    }
                }).show();
    }

    private void showNoteEditor(int editIndex) {
        JSONObject old = editIndex >= 0 ? notes.optJSONObject(editIndex) : null;
        EditText titleInput = makeInput("Note का शीर्षक", 1);
        EditText bodyInput = makeInput("यहाँ अपना Note लिखें...", 5);

        if (old != null) {
            titleInput.setText(old.optString("title", ""));
            bodyInput.setText(old.optString("body", ""));
        }

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.addView(titleInput);
        form.addView(bodyInput);

        new AlertDialog.Builder(this)
                .setTitle(editIndex >= 0 ? "Note Edit करें" : "नया Note")
                .setView(form)
                .setNegativeButton("रद्द", null)
                .setPositiveButton("Save", (d, w) -> {
                    String title = titleInput.getText().toString().trim();
                    String body = bodyInput.getText().toString().trim();
                    if (title.isEmpty() || body.isEmpty()) {
                        toast("शीर्षक और Note दोनों लिखें।");
                        return;
                    }
                    try {
                        JSONObject item = new JSONObject();
                        item.put("title", title);
                        item.put("body", body);
                        item.put("updated", System.currentTimeMillis());

                        if (editIndex >= 0) {
                            notes.put(editIndex, item);
                        } else {
                            notes.put(item);
                        }
                        saveArray("notes", notes);
                        showNotes();
                        toast("Note सेव हो गया।");
                    } catch (Exception e) {
                        toast("Note सेव नहीं हो सका।");
                    }
                }).show();
    }

    private void confirmDeleteNote(int index) {
        new AlertDialog.Builder(this)
                .setMessage("क्या यह Note हटाना है?")
                .setNegativeButton("नहीं", null)
                .setPositiveButton("हटाएँ", (d, w) -> {
                    notes = removeArrayItem(notes, index);
                    saveArray("notes", notes);
                    showNotes();
                }).show();
    }

    // ---------------- PLANNER ----------------

    private void showPlanner() {
        setupPage("Planner और Reminders");

        content.addView(makeButton("+ नया काम जोड़ें", this::addTask));
        addCardText("कुल काम: " + tasks.length(), 15);

        if (tasks.length() == 0) {
            addCardText("अभी कोई काम नहीं है।", 15);
        }

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.optJSONObject(i);
            if (task == null) continue;

            final int index = i;
            String title = task.optString("title", "काम");
            long time = task.optLong("time", 0);
            boolean done = task.optBoolean("done", false);

            String when = time > 0
                    ? new SimpleDateFormat("dd-MM-yyyy hh:mm a",
                    Locale.getDefault()).format(new Date(time))
                    : "समय तय नहीं";

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(8), dp(5), dp(8), dp(5));
            row.setBackgroundColor(0xFF16243B);

            CheckBox check = new CheckBox(this);
            check.setText(title);
            check.setTextColor(0xFFFFFFFF);
            check.setTextSize(16);
            check.setChecked(done);
            check.setOnCheckedChangeListener((button, checked) -> {
                JSONObject current = tasks.optJSONObject(index);
                if (current != null) {
                    try {
                        current.put("done", checked);
                        saveArray("tasks", tasks);
                    } catch (Exception ignored) { }
                }
            });

            TextView date = makeText(when, 13);
            row.addView(check);
            row.addView(date);
            row.addView(makeButton("Edit / Delete", () -> showTaskOptions(index)));

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(-1, -2);
            params.setMargins(0, dp(5), 0, dp(5));
            content.addView(row, params);
        }

        content.addView(makeButton("सभी काम मिटाएँ", this::clearTasks));
    }

    private void addTask() {
        EditText input = makeInput("काम का नाम लिखें", 1);
        new AlertDialog.Builder(this)
                .setTitle("नया काम")
                .setView(input)
                .setNegativeButton("रद्द", null)
                .setPositiveButton("अगला", (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (title.isEmpty()) {
                        toast("काम का नाम लिखें।");
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

                if (time <= System.currentTimeMillis()) {
                    toast("भविष्य का समय चुनें।");
                    return;
                }

                try {
                    JSONObject task = new JSONObject();
                    task.put("title", title);
                    task.put("time", time);
                    task.put("done", false);
                    tasks.put(task);
                    saveArray("tasks", tasks);
                    scheduleReminder(title, time);
                    showPlanner();
                    toast("काम और Reminder सेव हो गया।");
                } catch (Exception e) {
                    toast("काम सेव नहीं हो सका।");
                }
            }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), false).show();
        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void scheduleReminder(String title, long time) {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (manager == null) return;

        Intent intent = new Intent(REMINDER_ACTION);
        intent.setPackage(getPackageName());
        intent.putExtra("title", title);

        int requestCode = (int) (time % Integer.MAX_VALUE);
        PendingIntent pending = PendingIntent.getBroadcast(
                this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending);
    }

    private void showTaskOptions(int index) {
        JSONObject task = tasks.optJSONObject(index);
        if (task == null) return;

        String[] options = {"Edit करें", "Delete करें"};
        new AlertDialog.Builder(this)
                .setTitle(task.optString("title", "काम"))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        EditText input = makeInput("काम का नाम", 1);
                        input.setText(task.optString("title", ""));

                        new AlertDialog.Builder(this)
                                .setTitle("काम Edit करें")
                                .setView(input)
                                .setNegativeButton("रद्द", null)
                                .setPositiveButton("Save", (d, w) -> {
                                    String updatedTitle =
                                            input.getText().toString().trim();

                                    if (updatedTitle.isEmpty()) {
                                        toast("काम का नाम लिखें।");
                                        return;
                                    }

                                    try {
                                        task.put("title", updatedTitle);
                                        saveArray("tasks", tasks);
                                        showPlanner();
                                    } catch (Exception e) {
                                        toast("बदलाव सेव नहीं हुआ।");
                                    }
                                }).show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage("क्या यह काम हटाना है?")
                                .setNegativeButton("नहीं", null)
                                .setPositiveButton("हटाएँ", (d, w) -> {
                                    tasks = removeArrayItem(tasks, index);
                                    saveArray("tasks", tasks);
                                    showPlanner();
                                }).show();
                    }
                }).show();
    }

    private JSONArray removeArrayItem(JSONArray source, int removeIndex) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            if (i == removeIndex) continue;
            Object item = source.opt(i);
            if (item != null) result.put(item);
        }
        return result;
    }

    private void clearTasks() {
        new AlertDialog.Builder(this)
                .setTitle("सभी काम हटाएँ?")
                .setMessage("क्या Planner के सभी काम हटाने हैं?")
                .setNegativeButton("रद्द", null)
                .setPositiveButton("हटाएँ", (d, w) -> {
                    tasks = new JSONArray();
                    saveArray("tasks", tasks);
                    showPlanner();
                }).show();
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(intent, DOCUMENT_REQUEST);
        } catch (Exception e) {
            toast("Document picker नहीं खुल सका।");
        }
    }

    private void internetResearch() {
        EditText input = makeInput("किस विषय पर खोजें?", 1);
        new AlertDialog.Builder(this)
                .setTitle("Internet Search")
                .setView(input)
                .setNegativeButton("रद्द", null)
                .setPositiveButton("Search", (d, w) -> {
                    String query = input.getText().toString().trim();
                    if (query.isEmpty()) return;

                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q="
                                    + Uri.encode(query)));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        toast("Browser उपलब्ध नहीं है।");
                    }
                }).show();
    }

    private void showSettings() {
        setupPage("Settings");
        addCardText("भाषा चुनें", 18);

        content.addView(makeButton("हिंदी", () -> {
            language = "hi";
            prefs.edit().putString("language", language).apply();
            setTtsLanguage();
            toast("हिंदी चुनी गई।");
        }));

        content.addView(makeButton("English", () -> {
            language = "en";
            prefs.edit().putString("language", language).apply();
            setTtsLanguage();
            toast("English selected.");
        }));

        addCardText("Chat History, Notes और Planner डेटा इसी मोबाइल में सेव होते हैं।", 14);
        addCardText("AI Backend अभी कनेक्ट नहीं है।", 14);
        addCardText("Voice की उपलब्धता मोबाइल के Speech और TTS इंजन पर निर्भर है।", 14);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == VOICE_REQUEST) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                showChat();
                messageInput.setText(results.get(0));
                messageInput.setSelection(messageInput.length());
            }
        } else if (requestCode == DOCUMENT_REQUEST) {
            Uri uri = data.getData();
            if (uri != null) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(uri, getContentResolver().getType(uri));
                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(viewIntent);
                } catch (Exception e) {
                    toast("इस फाइल को खोलने वाला ऐप नहीं मिला।");
                }
            }
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager manager =
                    (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            if (manager != null && getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(
                        getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception ignored) { }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                                    
