package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* JADX INFO: loaded from: classes5.dex */
public class KeystoreManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "RepoStudio_GitHub_Secret_Key";
    private static final String PREF_CLIENT_ID = "github_oauth_client_id";
    private static final String PREF_ENCRYPTED_TOKEN = "encrypted_github_token";
    private static final String PREF_GIT_USER_EMAIL = "git_user_email";
    private static final String PREF_GIT_USER_NAME = "git_user_name";
    private static final String PREF_NAME = "repostudio_secure_prefs";
    private static final String PREF_SAVED_EXTERNAL_PATHS = "saved_external_repo_paths";
    private static final String PREF_TOKEN_IV = "token_iv";
    private static final String TAG = "KeystoreManager";
    private final SharedPreferences preferences;

    public KeystoreManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, 0);
        initKey();
    }

    private void initKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE);
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KEY_ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setKeySize(256).build();
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Android KeyStore key: " + e.getMessage());
        }
    }

    public synchronized boolean saveGitHubToken(String token) {
        if (token != null) {
            if (!token.trim().isEmpty()) {
                try {
                    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
                    keyStore.load(null);
                    SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
                    if (secretKey == null) {
                        initKey();
                        secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
                    }
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    cipher.init(1, secretKey);
                    byte[] iv = cipher.getIV();
                    byte[] encryptedBytes = cipher.doFinal(token.trim().getBytes(StandardCharsets.UTF_8));
                    String encodedToken = Base64.encodeToString(encryptedBytes, 2);
                    String encodedIv = Base64.encodeToString(iv, 2);
                    this.preferences.edit().putString(PREF_ENCRYPTED_TOKEN, encodedToken).putString(PREF_TOKEN_IV, encodedIv).apply();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error encrypting token: " + e.getMessage());
                    return false;
                }
            }
        }
        this.preferences.edit().remove(PREF_ENCRYPTED_TOKEN).remove(PREF_TOKEN_IV).apply();
        return true;
    }

    public synchronized String getGitHubToken() {
        String encodedToken = this.preferences.getString(PREF_ENCRYPTED_TOKEN, null);
        String encodedIv = this.preferences.getString(PREF_TOKEN_IV, null);
        if (encodedToken == null || encodedIv == null) {
            return null;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            if (secretKey == null) {
                return null;
            }
            byte[] iv = Base64.decode(encodedIv, 2);
            byte[] encryptedBytes = Base64.decode(encodedToken, 2);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(2, secretKey, spec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting token: " + e.getMessage());
            return null;
        }
    }

    public synchronized boolean hasValidToken() {
        String token;
        token = getGitHubToken();
        return (token == null || token.trim().isEmpty()) ? false : true;
    }

    public synchronized void clearToken() {
        this.preferences.edit().remove(PREF_ENCRYPTED_TOKEN).remove(PREF_TOKEN_IV).apply();
    }

    public String getGitAuthorName() {
        return this.preferences.getString(PREF_GIT_USER_NAME, "RepoStudio Developer");
    }

    public void setGitAuthorName(String name) {
        this.preferences.edit().putString(PREF_GIT_USER_NAME, name).apply();
    }

    public String getGitAuthorEmail() {
        return this.preferences.getString(PREF_GIT_USER_EMAIL, "developer@repostudio.local");
    }

    public void setGitAuthorEmail(String email) {
        this.preferences.edit().putString(PREF_GIT_USER_EMAIL, email).apply();
    }

    public String getOAuthClientId() {
        return this.preferences.getString(PREF_CLIENT_ID, "");
    }

    public void setOAuthClientId(String clientId) {
        this.preferences.edit().putString(PREF_CLIENT_ID, clientId).apply();
    }

    public synchronized Set<String> getSavedExternalRepoPaths() {
        Set<String> set;
        set = this.preferences.getStringSet(PREF_SAVED_EXTERNAL_PATHS, null);
        return set != null ? new HashSet(set) : new HashSet();
    }

    public synchronized void addSavedExternalRepoPath(String path) {
        if (path != null) {
            if (!path.trim().isEmpty()) {
                Set<String> set = getSavedExternalRepoPaths();
                set.add(path.trim());
                this.preferences.edit().putStringSet(PREF_SAVED_EXTERNAL_PATHS, set).apply();
            }
        }
    }

    public synchronized void removeSavedExternalRepoPath(String path) {
        if (path == null) {
            return;
        }
        Set<String> set = getSavedExternalRepoPaths();
        set.remove(path.trim());
        this.preferences.edit().putStringSet(PREF_SAVED_EXTERNAL_PATHS, set).apply();
    }
}
