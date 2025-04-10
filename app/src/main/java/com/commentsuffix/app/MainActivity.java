package com.commentsuffix.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CommentSuffixPrefs";
    private static final String SUFFIX_KEY = "suffix_text";
    private static final String XIAOHONGSHU_KEY = "xiaohongshu_enabled";
    private static final String DOUYIN_KEY = "douyin_enabled";
    private static final String KUAISHOU_KEY = "kuaishou_enabled";

    private EditText suffixEditText;
    private CheckBox checkboxXiaohongshu;
    private CheckBox checkboxDouyin;
    private CheckBox checkboxKuaishou;
    private Button saveButton;
    private Button enableServiceButton;
    private TextView serviceStatusText;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 初始化视图
        suffixEditText = findViewById(R.id.suffixEditText);
        checkboxXiaohongshu = findViewById(R.id.checkboxXiaohongshu);
        checkboxDouyin = findViewById(R.id.checkboxDouyin);
        checkboxKuaishou = findViewById(R.id.checkboxKuaishou);
        saveButton = findViewById(R.id.saveButton);
        enableServiceButton = findViewById(R.id.enableServiceButton);
        serviceStatusText = findViewById(R.id.serviceStatusText);

        // 加载保存的设置
        loadSavedSettings();

        // 设置按钮点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                Toast.makeText(MainActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }
        });

        enableServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilitySettings();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void loadSavedSettings() {
        String savedSuffix = preferences.getString(SUFFIX_KEY, "");
        boolean xiaohongshuEnabled = preferences.getBoolean(XIAOHONGSHU_KEY, true);
        boolean douyinEnabled = preferences.getBoolean(DOUYIN_KEY, true);
        boolean kuaishouEnabled = preferences.getBoolean(KUAISHOU_KEY, true);

        suffixEditText.setText(savedSuffix);
        checkboxXiaohongshu.setChecked(xiaohongshuEnabled);
        checkboxDouyin.setChecked(douyinEnabled);
        checkboxKuaishou.setChecked(kuaishouEnabled);
    }

    private void saveSettings() {
        String suffix = suffixEditText.getText().toString().trim();
        boolean xiaohongshuEnabled = checkboxXiaohongshu.isChecked();
        boolean douyinEnabled = checkboxDouyin.isChecked();
        boolean kuaishouEnabled = checkboxKuaishou.isChecked();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SUFFIX_KEY, suffix);
        editor.putBoolean(XIAOHONGSHU_KEY, xiaohongshuEnabled);
        editor.putBoolean(DOUYIN_KEY, douyinEnabled);
        editor.putBoolean(KUAISHOU_KEY, kuaishouEnabled);
        editor.apply();
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }

    private void updateServiceStatus() {
        boolean isServiceEnabled = isAccessibilityServiceEnabled();
        if (isServiceEnabled) {
            serviceStatusText.setText(getString(R.string.service_status) + " " + getString(R.string.service_enabled));
        } else {
            serviceStatusText.setText(getString(R.string.service_status) + " " + getString(R.string.service_disabled));
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + CommentSuffixService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            // 设置不存在，返回false
            return false;
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                return settingValue.contains(service);
            }
        }
        return false;
    }
} 