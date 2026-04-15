package com.example.callblocker.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.callblocker.R;
import com.example.callblocker.data.PrefixRepository;
import com.example.callblocker.model.BlockedPrefix;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private PrefixRepository prefixRepository;

    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String[]> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefixRepository = PrefixRepository.getInstance(this);

        applyWindowInsets();
        setupLaunchers();
        setupButtons();
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.settingsRoot);
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    baseLeft + systemBarsInsets.left,
                    baseTop + systemBarsInsets.top,
                    baseRight + systemBarsInsets.right,
                    baseBottom + systemBarsInsets.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupLaunchers() {
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                this::exportToUri
        );

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                this::importFromUri
        );
    }

    private void setupButtons() {
        findViewById(R.id.btnExportJson).setOnClickListener(v ->
                exportLauncher.launch("french-call-blocker-config.json"));

        findViewById(R.id.btnImportJson).setOnClickListener(v ->
                importLauncher.launch(new String[]{"application/json", "text/json", "*/*"}));

        findViewById(R.id.btnResetDefaults).setOnClickListener(v -> {
            prefixRepository.resetPrefixesToDefaults();
            Toast.makeText(this, R.string.toast_defaults_reset, Toast.LENGTH_SHORT).show();
        });
    }

    private void exportToUri(Uri uri) {
        if (uri == null) {
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                throw new IllegalStateException("OutputStream null");
            }

            JSONObject root = new JSONObject();
            JSONArray prefixes = new JSONArray();
            for (BlockedPrefix item : prefixRepository.getPrefixes()) {
                JSONObject obj = new JSONObject();
                obj.put("prefix", item.getPrefix());
                obj.put("enabled", item.isEnabled());
                prefixes.put(obj);
            }

            JSONArray whitelist = new JSONArray();
            for (String number : prefixRepository.getWhitelist()) {
                whitelist.put(number);
            }

            root.put("prefixes", prefixes);
            root.put("whitelist", whitelist);
            root.put("exportedAt", System.currentTimeMillis());

            outputStream.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            Toast.makeText(this, R.string.toast_export_done, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void importFromUri(Uri uri) {
        if (uri == null) {
            return;
        }

        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IllegalStateException("InputStream null");
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            String json = buffer.toString(StandardCharsets.UTF_8.name());
            JSONObject root = new JSONObject(json);

            JSONArray prefixesArray = root.optJSONArray("prefixes");
            JSONArray whitelistArray = root.optJSONArray("whitelist");

            List<BlockedPrefix> prefixes = new ArrayList<>();
            List<String> whitelist = new ArrayList<>();

            if (prefixesArray != null) {
                for (int i = 0; i < prefixesArray.length(); i++) {
                    JSONObject item = prefixesArray.optJSONObject(i);
                    if (item != null) {
                        String prefix = item.optString("prefix", "");
                        boolean enabled = item.optBoolean("enabled", true);
                        prefixes.add(new BlockedPrefix(prefix, enabled));
                    }
                }
            }

            if (whitelistArray != null) {
                for (int i = 0; i < whitelistArray.length(); i++) {
                    String number = whitelistArray.optString(i, "");
                    if (!number.isEmpty()) {
                        whitelist.add(number);
                    }
                }
            }

            prefixRepository.replaceAll(prefixes, whitelist);
            Toast.makeText(this, R.string.toast_import_done, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_import_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
