package com.commentsuffix.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class CommentSuffixService extends AccessibilityService {

    private static final String PREFS_NAME = "CommentSuffixPrefs";
    private static final String SUFFIX_KEY = "suffix_text";
    private static final String XIAOHONGSHU_KEY = "xiaohongshu_enabled";
    private static final String DOUYIN_KEY = "douyin_enabled";
    private static final String KUAISHOU_KEY = "kuaishou_enabled";
    
    private static final String XIAOHONGSHU_PACKAGE = "com.xingin.xhs";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final String KUAISHOU_PACKAGE = "com.kuaishou.nebula";
    
    private static final String[] EDIT_TEXT_CLASS_NAMES = {
        "android.widget.EditText",
        "androidx.appcompat.widget.AppCompatEditText",
        "android.support.v7.widget.AppCompatEditText"
    };
    
    private String suffixText = "";
    private boolean xiaohongshuEnabled = true;
    private boolean douyinEnabled = true;
    private boolean kuaishouEnabled = true;
    private String lastEditTextContent = "";
    private long lastEditTime = 0;
    private static final long EDIT_COOLDOWN_MS = 1000; // 1秒冷却时间

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadSettings();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 如果后缀为空，直接返回
        if (TextUtils.isEmpty(suffixText)) {
            return;
        }
        
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
        
        // 检查是否为启用的应用
        if (!isAppEnabled(packageName)) {
            return;
        }
        
        // 针对TYPE_VIEW_TEXT_CHANGED事件或者TYPE_WINDOW_CONTENT_CHANGED事件处理输入框
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null) {
                nodeInfo = getRootInActiveWindow();
            }
            
            if (nodeInfo != null) {
                // 查找可编辑的文本框
                findAndProcessEditTexts(nodeInfo);
                nodeInfo.recycle();
            }
        }
    }

    private void findAndProcessEditTexts(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return;
        
        // 检查当前节点是否为EditText
        if (isEditTextNode(nodeInfo)) {
            processEditText(nodeInfo);
        }
        
        // 递归遍历子节点
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = nodeInfo.getChild(i);
            if (child != null) {
                findAndProcessEditTexts(child);
                child.recycle();
            }
        }
    }

    private boolean isEditTextNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return false;
        
        CharSequence className = nodeInfo.getClassName();
        if (className == null) return false;
        
        for (String editTextClass : EDIT_TEXT_CLASS_NAMES) {
            if (editTextClass.equals(className.toString())) {
                return nodeInfo.isEditable();
            }
        }
        
        return false;
    }

    private void processEditText(AccessibilityNodeInfo editText) {
        if (editText == null || !editText.isEditable()) return;
        
        CharSequence text = editText.getText();
        if (text == null) return;
        
        String currentText = text.toString();
        
        // 如果编辑框内容为空或只有后缀文本，跳过处理
        if (TextUtils.isEmpty(currentText) || currentText.equals(lastEditTextContent) || 
            currentText.endsWith(suffixText)) {
            return;
        }
        
        // 检查冷却时间
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEditTime < EDIT_COOLDOWN_MS) {
            return;
        }
        
        // 如果文本不为空且不以后缀结尾，添加后缀
        String newText = currentText + suffixText;
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText);
        editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        
        lastEditTextContent = newText;
        lastEditTime = currentTime;
    }

    private boolean isAppEnabled(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        
        if (XIAOHONGSHU_PACKAGE.equals(packageName)) {
            return xiaohongshuEnabled;
        } else if (DOUYIN_PACKAGE.equals(packageName)) {
            return douyinEnabled;
        } else if (KUAISHOU_PACKAGE.equals(packageName)) {
            return kuaishouEnabled;
        }
        
        return false;
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        suffixText = preferences.getString(SUFFIX_KEY, "");
        xiaohongshuEnabled = preferences.getBoolean(XIAOHONGSHU_KEY, true);
        douyinEnabled = preferences.getBoolean(DOUYIN_KEY, true);
        kuaishouEnabled = preferences.getBoolean(KUAISHOU_KEY, true);
    }

    @Override
    public void onInterrupt() {
        // 服务中断时的处理
    }
} 