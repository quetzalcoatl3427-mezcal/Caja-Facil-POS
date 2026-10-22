
package com.cajafacil.pos;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.ValueCallback;
import android.webkit.JavascriptInterface;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebViewClient;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int PICK_IMAGE = 42;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.addJavascriptInterface(new FirebaseBridge(), "AndroidFirebase");
        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView view,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(
                    Intent.createChooser(intent, "Elige foto del producto"),
                    PICK_IMAGE
                );
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private class FirebaseBridge {
        @JavascriptInterface
        public void login(String email, String password) {
            runOnUiThread(() -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(result -> {
                        String uid = auth.getCurrentUser().getUid();

                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()
                                        || !Boolean.TRUE.equals(doc.getBoolean("activo"))) {
                                    auth.signOut();
                                    sendResult(false, "Usuario no autorizado o inactivo.");
                                    return;
                                }

                                String role = doc.getString("rol");
                                String name = doc.getString("nombre");

                                try {
                                    JSONObject data = new JSONObject();
                                    data.put("ok", true);
                                    data.put("uid", uid);
                                    data.put("nombre", name == null ? "" : name);
                                    data.put("rol", role == null ? "" : role);
                                    webView.evaluateJavascript(
                                        "window.onFirebaseLoginResult && window.onFirebaseLoginResult("
                                        + data.toString() + ")",
                                        null
                                    );
                                } catch (Exception e) {
                                    sendResult(false, "Error al leer el perfil.");
                                }
                            })
                            .addOnFailureListener(e -> {
                                auth.signOut();
                                sendResult(false, "No se pudo verificar el perfil en Firestore.");
                            });
                    })
                    .addOnFailureListener(e ->
                        sendResult(false, "No se pudo iniciar sesión. Revisa tus datos.")
                    );
            });
        }

        @JavascriptInterface
        public void logout() {
            runOnUiThread(() -> {
                auth.signOut();
                sendResult(false, "Sesión cerrada.");
            });
        }
    }

    private void sendResult(boolean ok, String message) {
        try {
            JSONObject data = new JSONObject();
            data.put("ok", ok);
            data.put("message", message);
            webView.evaluateJavascript(
                "window.onFirebaseLoginResult && window.onFirebaseLoginResult("
                + data.toString() + ")",
                null
            );
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && fileCallback != null) {
            Uri[] result = resultCode == RESULT_OK
                    && data != null && data.getData() != null
                    ? new Uri[]{data.getData()} : null;

            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
