package com.amanoteam.unalix.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import org.json.JSONException;
import org.json.JSONObject;

import com.amanoteam.unalix.R;
import com.amanoteam.unalix.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
	
	// "Export" preferences listener
	private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(final ActivityResult result) {
			if (result.getResultCode() == Activity.RESULT_OK) {
				
				final Context context = getApplicationContext();
				
				final Intent intent = result.getData();
				final Uri fileUri = intent.getData();
				
				final ContentResolver contentResolver =  getContentResolver();
				
				try {
					final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
					final JSONObject obj = new JSONObject();
					
					obj.put("ignoreReferralMarketing", preferences.getBoolean("ignoreReferralMarketing", false));
					obj.put("ignoreRules", preferences.getBoolean("ignoreRules", false));
					obj.put("ignoreExceptions", preferences.getBoolean("ignoreExceptions", false));
					obj.put("ignoreRawRules", preferences.getBoolean("ignoreRawRules", false));
					obj.put("ignoreRedirections", preferences.getBoolean("ignoreRedirections", false));
					obj.put("skipBlocked", preferences.getBoolean("skipBlocked", false));
					obj.put("stripDuplicates", preferences.getBoolean("stripDuplicates", false));
					obj.put("stripEmpty", preferences.getBoolean("stripEmpty", false));
					obj.put("parseDocuments", preferences.getBoolean("parseDocuments", false));
					
					obj.put("timeout", Integer.valueOf(preferences.getString("timeout", "3000")));
					obj.put("maxRedirects", Integer.valueOf(preferences.getString("maxRedirects", "13")));
					
					obj.put("appTheme", preferences.getString("appTheme", "follow_system"));
					obj.put("disableClearURLActivity", preferences.getBoolean("disableClearURLActivity", false));
					obj.put("disableUnshortURLActivity", preferences.getBoolean("disableUnshortURLActivity", false));
					obj.put("disableCopyToClipboardActivity", preferences.getBoolean("disableCopyToClipboardActivity", false));
					
					final OutputStream outputStream = contentResolver.openOutputStream(fileUri);
					
					outputStream.write(obj.toString().getBytes());
					outputStream.close();
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error exporting preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show();
			}
		}
	});
	
	// "Import" preferences listener
	private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(new StartActivityForResult(),
			new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(final ActivityResult result) {
			if (result.getResultCode() == Activity.RESULT_OK) {
				
				final Context context = getApplicationContext();
				
				final Intent intent = result.getData();
				final Uri fileUri = intent.getData();
				
				final ContentResolver contentResolver =  getContentResolver();
				
				try {
					final InputStream inputStream = contentResolver.openInputStream(fileUri);
					final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()));
					final StringBuilder stringBuilder = new StringBuilder();
					
					String inputLine;
					
					while ((inputLine = bufferedReader.readLine()) != null)
						stringBuilder.append(inputLine);
					
					inputStream.close();
					
					final JSONObject obj = new JSONObject(stringBuilder.toString());
					
					final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
					final Editor editor = preferences.edit();
					
					editor.putBoolean("ignoreReferralMarketing", obj.getBoolean("ignoreReferralMarketing"));
					editor.putBoolean("ignoreRules", obj.getBoolean("ignoreRules"));
					editor.putBoolean("ignoreExceptions", obj.getBoolean("ignoreExceptions"));
					editor.putBoolean("ignoreRawRules", obj.getBoolean("ignoreRawRules"));
					editor.putBoolean("ignoreRedirections", obj.getBoolean("ignoreRedirections"));
					editor.putBoolean("skipBlocked", obj.getBoolean("skipBlocked"));
					editor.putBoolean("stripDuplicates", obj.getBoolean("stripDuplicates"));
					editor.putBoolean("stripEmpty", obj.getBoolean("stripEmpty"));
					editor.putBoolean("parseDocuments", obj.getBoolean("parseDocuments"));
					
					editor.putString("timeout", String.valueOf(obj.getInt("timeout")));
					editor.putString("maxRedirects", String.valueOf(obj.getInt("maxRedirects")));
					
					editor.putString("appTheme", obj.getString("appTheme"));
					editor.putBoolean("disableClearURLActivity", obj.getBoolean("disableClearURLActivity"));
					editor.putBoolean("disableUnshortURLActivity", obj.getBoolean("disableUnshortURLActivity"));
					editor.putBoolean("disableCopyToClipboardActivity", obj.getBoolean("disableCopyToClipboardActivity"));
					
					editor.commit();
				} catch (IOException | JSONException e) {
					Toast.makeText(context, "Error importing preferences file", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show();
			}
		}
	});
	
	private PackageManager packageManager;
	
	private final ComponentName clearUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.ClearURLActivity");
	private final ComponentName unshortUrlActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.UnshortURLActivity");
	private final ComponentName copyToClipboardActivity = new ComponentName("com.amanoteam.unalix", "com.amanoteam.unalix.activities.CopyToClipboardActivity");
	
	private SettingsFragment settingsFragment;
	private PreferenceScreen preferenceScreen;
	
	private final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
			
			if (key.equals("disableClearURLActivity")) {
				if (preferences.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(clearUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(clearUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("disableUnshortURLActivity")) {
				if (preferences.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(unshortUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(unshortUrlActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("disableCopyToClipboardActivity")) {
				if (preferences.getBoolean(key, false)) {
					packageManager.setComponentEnabledSetting(copyToClipboardActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				} else {
					packageManager.setComponentEnabledSetting(copyToClipboardActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			} else if (key.equals("appTheme")) {
				recreate();
			}
			
		}
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		packageManager = getPackageManager();
		
		// Preferences stuff
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
		
		// Dark mode stuff
		final String appTheme = preferences.getString("appTheme", "follow_system");
		
		boolean isDarkMode = false;
		
		if (appTheme.equals("follow_system")) {
			// Snippet from https://github.com/Andrew67/dark-mode-toggle/blob/11c1e16071b301071be0c4715a15fcb031d0bb64/app/src/main/java/com/andrew67/darkmode/UiModeManagerUtil.java#L17
			final UiModeManager uiModeManager = ContextCompat.getSystemService(this, UiModeManager.class);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR) {
				isDarkMode = true;
			}
		} else if (appTheme.equals("dark")) {
			isDarkMode = true;
		}
		
		if (isDarkMode) {
			setTheme(R.style.DarkTheme);
		} else {
			setTheme(R.style.LigthTheme);
		}
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		// Action bar
		final Toolbar settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
		setSupportActionBar(settingsToolbar);
		
		settingsFragment = new SettingsFragment();
		
		// Preferences screen
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame_layout_settings, settingsFragment)
			.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings_export:
				final Calendar calendar = Calendar.getInstance();
				final Date currentLocalTime = calendar.getTime();
				final DateFormat date = new SimpleDateFormat("dd-MM-yyy_HH-mm-ss");
				
				final Intent exportIntent = new Intent();
				
				exportIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
				exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
				exportIntent.setType("application/json");
				exportIntent.putExtra(Intent.EXTRA_TITLE, "unalix_settings_" + date.format(currentLocalTime) + ".json");
				
				exportPreferences.launch(exportIntent);
				
				return true;
			case R.id.settings_import:
				final Intent importIntent = new Intent();
				
				importIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				importIntent.addCategory(Intent.CATEGORY_OPENABLE);
				importIntent.setType("text/plain");
				
				importPreferences.launch(importIntent);
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	
}