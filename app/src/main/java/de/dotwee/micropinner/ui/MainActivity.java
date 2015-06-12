package de.dotwee.micropinner.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import de.dotwee.micropinner.R;
import de.dotwee.micropinner.tools.BootReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_VISIBILITY = "EXTRA_VISIBILITY", EXTRA_PRIORITY = "EXTRA_PRIORITY", EXTRA_TITLE = "EXTRA_TITLE", EXTRA_CONTENT = "EXTRA_CONTENT", EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION";
    public static final String PREF_FIRSTUSE = "pref_firstuse", PREF_SHOWNEWPIN = "pref_shownewpin";
    public static final String LOG_TAG = "MainActivity";
    public static final boolean DEBUG = true;

    Spinner spinnerVisibility, spinnerPriority;
    EditText editTextContent, editTextTitle;
    SharedPreferences sharedPreferences;
    CheckBox checkBoxShowNewPin;
    TextView dialogTitle;

    public static Notification generatePin(Context context, int visibility, int priority, int id, String title, String content) {
        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setSmallIcon(R.drawable.ic_star_24dp);

        if (Build.VERSION.SDK_INT >= 21) {
            notification.setVisibility(visibility);
        }

        Intent resultIntent = new Intent(context, EditActivity.class);
        resultIntent.putExtra(EXTRA_NOTIFICATION, id);
        resultIntent.putExtra(EXTRA_CONTENT, content);
        resultIntent.putExtra(EXTRA_TITLE, title);

        resultIntent.putExtra(EXTRA_VISIBILITY, visibility);
        resultIntent.putExtra(EXTRA_PRIORITY, priority);

        notification.setContentIntent(PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        return notification.build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // setup dialog title
        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getResources().getString(R.string.main_name));

        // setup checkbox and set it to its last instance state
        checkBoxShowNewPin = (CheckBox) findViewById(R.id.checkBoxNewPin);
        checkBoxShowNewPin.setChecked(sharedPreferences.getBoolean(MainActivity.PREF_SHOWNEWPIN, true));
        checkBoxShowNewPin.setOnClickListener(this);

        // declare buttons
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        findViewById(R.id.buttonPin).setOnClickListener(this);

        editTextContent = (EditText) findViewById(R.id.editTextContent);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);

        // set focus to the title input
        editTextTitle.performClick();

        sendBroadcast(new Intent(this, BootReceiver.class));

        spinnerVisibility = (Spinner) findViewById(R.id.spinnerVisibility);
        spinnerVisibility.setAdapter(getVisibilityAdapter());

        spinnerPriority = (Spinner) findViewById(R.id.spinnerPriority);
        spinnerPriority.setAdapter(getPriorityAdapter());

        if (!sharedPreferences.getBoolean(PREF_FIRSTUSE, false)) {

            /*
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this, MainActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
                    */

            sharedPreferences.edit().putBoolean(PREF_FIRSTUSE, true).apply();
        }
    }

    private ArrayAdapter<String> getPriorityAdapter() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.array_priorities));
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return priorityAdapter;
    }

    private ArrayAdapter<String> getVisibilityAdapter() {
        ArrayAdapter<String> visibilityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.array_visibilities));
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return visibilityAdapter;
    }

    public int _getVisibility() {

        String selected = spinnerVisibility.getSelectedItem().toString();
        if (DEBUG) Log.i(LOG_TAG, "Spinner selected: " + selected);

        if (Build.VERSION.SDK_INT >= 21) {

            if (selected.equalsIgnoreCase("private")) return Notification.VISIBILITY_PRIVATE;
            else if (selected.equalsIgnoreCase("secret")) return Notification.VISIBILITY_SECRET;
            else return Notification.VISIBILITY_PUBLIC;
        } else return 0;
    }

    public int _getPriority() {

        String selected = spinnerPriority.getSelectedItem().toString();
        if (DEBUG) Log.i(LOG_TAG, "Spinner selected: " + selected);

        if (selected.equalsIgnoreCase("low")) return Notification.PRIORITY_LOW;
        else if (selected.equalsIgnoreCase("min")) return Notification.PRIORITY_MIN;
        else if (selected.equalsIgnoreCase("high")) return Notification.PRIORITY_HIGH;
        else return Notification.PRIORITY_DEFAULT;
    }

    public String _getTitle() {
        return editTextTitle.getText().toString();
    }

    public String _getContent() {
        return editTextContent.getText().toString();
    }

    private void pinEntry() {
        String title = _getTitle();
        String content = _getContent();
        int notificationID = randomNotificationID();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (title.equalsIgnoreCase("") | title.equalsIgnoreCase(null))
            Toast.makeText(this, "The title has to contain text.", Toast.LENGTH_SHORT).show();

        else {
            if (DEBUG)
                Log.i(LOG_TAG, "New pin: " + "\nTitle: " + title + "\nContent: " + content + "\nVisibility: " + _getVisibility() + "\nPriority: " + _getPriority());
            notificationManager.notify(notificationID, generatePin(this, _getVisibility(), _getPriority(), notificationID, title, content));
            finish();
        }
    }

    private int randomNotificationID() {
        int start = 1, end = 256;

        return new Random().nextInt(end - start + 1) + start;
    }

    @Override
    public void onClick(View v) {
        if (DEBUG) Log.i(LOG_TAG, "clicked: " + v.getId());
        switch (v.getId()) {
            case R.id.buttonCancel:
                finish();
                break;
            case R.id.buttonPin:
                pinEntry();
                break;
            case R.id.checkBoxNewPin:
                sharedPreferences.edit().putBoolean(PREF_SHOWNEWPIN, checkBoxShowNewPin.isChecked()).apply();
                sendBroadcast(new Intent(this, BootReceiver.class));
        }
    }
}
