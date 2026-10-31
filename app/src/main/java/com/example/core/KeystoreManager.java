package com.example.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class KeystoreManager {
    private static final String TAG = "KeystoreManager";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "RepoStudio_GitHub_Secret_Key";
    private static final String PREF_NAME = "repostudio_secure_prefs";
    private static final String PREF_ENCRYPTED_TOKEN = "encrypted_github_token";
    private static final String PREF_TOKEN_IV = "token_iv";
    private static final String PREF_GIT_USER_NAME = "git_user_name";
    private static final String PREF_GIT_USER_EMAIL = "git_user_email";
    private static final String PREF_CLIENT_ID = "github_oauth_client_id";

    private final SharedPreferences preferences;

    public KeystoreManager(Context context) {
        this.preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initKey();
    }

    private void initKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Android KeyStore key: " + e.getMessage());
        }
    }

    public synchronized boolean saveGitHubToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            preferences.edit().remove(PREF_ENCRYPTED_TOKEN).remove(PREF_TOKEN_IV).apply();
            return true;
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            if (secretKey == null) {
                initKey();
                secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            }

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = cipher.doFinal(token.trim().getBytes(StandardCharsets.UTF_8));

            String encodedToken = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
            String encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP);

            preferences.edit()
                    .putString(PREF_ENCRYPTED_TOKEN, encodedToken)
                    .putString(PREF_TOKEN_IV, encodedIv)
                    .apply();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting token: " + e.getMessage());
            return false;
        }
    }

    public synchronized String getGitHubToken() {
        String encodedToken = preferences.getString(PREF_ENCRYPTED_TOKEN, null);
        String encodedIv = preferences.getString(PREF_TOKEN_IV, null);

        if (encodedToken == null || encodedIv == null) {
            return null;
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            if (secretKey == null) return null;

            byte[] iv = Base64.decode(encodedIv, Base64.NO_WRAP);
            byte[] encryptedBytes = Base64.decode(encodedToken, Base64.NO_WRAP);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting token: " + e.getMessage());
            return null;
        }
    }

    public synchronized boolean hasValidToken() {
        String token = getGitHubToken();
        return token != null && !token.trim().isEmpty();
    }

    public synchronized void clearToken() {
        preferences.edit()
                .remove(PREF_ENCRYPTED_TOKEN)
                .remove(PREF_TOKEN_IV)
                .apply();
    }

    public String getGitAuthorName() {
        return preferences.getString(PREF_GIT_USER_NAME, "RepoStudio Developer");
    }

    public void setGitAuthorName(String name) {
        preferences.edit().putString(PREF_GIT_USER_NAME, name).apply();
    }

    public String getGitAuthorEmail() {
        return preferences.getString(PREF_GIT_USER_EMAIL, "developer@repostudio.local");
    }

    public void setGitAuthorEmail(String email) {
        preferences.edit().putString(PREF_GIT_USER_EMAIL, email).apply();
    }

    public String getOAuthClientId() {
        return preferences.getString(PREF_CLIENT_ID, "");
    }

    public void setOAuthClientId(String clientId) {
        preferences.edit().putString(PREF_CLIENT_ID, clientId).apply();
    }
}
