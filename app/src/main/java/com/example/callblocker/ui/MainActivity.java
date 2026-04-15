package com.example.callblocker.ui;

import android.app.role.RoleManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callblocker.R;
import com.example.callblocker.adapter.BlockedCallLogAdapter;
import com.example.callblocker.adapter.PrefixAdapter;
import com.example.callblocker.adapter.WhitelistAdapter;
import com.example.callblocker.data.BlockedCallLogRepository;
import com.example.callblocker.data.PrefixRepository;

public class MainActivity extends AppCompatActivity {

    private PrefixRepository prefixRepository;
    private BlockedCallLogRepository blockedCallLogRepository;

    private TextView protectionIndicator;
    private TextView roleStatusText;
    private TextView emptyLogText;
    private EditText inputPrefix;
    private EditText inputWhitelist;

    private PrefixAdapter prefixAdapter;
    private WhitelistAdapter whitelistAdapter;
    private BlockedCallLogAdapter logAdapter;

    @Nullable
    private RoleManager roleManager;

    private ActivityResultLauncher<Intent> roleRequestLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefixRepository = PrefixRepository.getInstance(this);
        blockedCallLogRepository = BlockedCallLogRepository.getInstance(this);

        roleManager = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? getSystemService(RoleManager.class)
                : null;

        bindViews();
        applyWindowInsets();
        setupRoleLauncher();
        setupRecyclerViews();
        setupButtons();
        observeLogs();
        refreshLists();
        updateRoleStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLists();
        updateRoleStatus();
    }

    private void bindViews() {
        protectionIndicator = findViewById(R.id.protectionIndicator);
        roleStatusText = findViewById(R.id.roleStatusText);
        emptyLogText = findViewById(R.id.emptyLogText);
        inputPrefix = findViewById(R.id.inputPrefix);
        inputWhitelist = findViewById(R.id.inputWhitelist);
    }

    private void applyWindowInsets() {
        View root = findViewById(R.id.mainRoot);
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

    private void setupRoleLauncher() {
        roleRequestLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    updateRoleStatus();
                    if (isCallScreeningRoleHeld()) {
                        Toast.makeText(this, R.string.toast_role_granted, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.toast_role_not_granted, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupRecyclerViews() {
        RecyclerView recyclerPrefixes = findViewById(R.id.recyclerPrefixes);
        RecyclerView recyclerWhitelist = findViewById(R.id.recyclerWhitelist);
        RecyclerView recyclerLog = findViewById(R.id.recyclerLog);

        prefixAdapter = new PrefixAdapter(new PrefixAdapter.Listener() {
            @Override
            public void onPrefixEnabledChanged(String prefix, boolean enabled) {
                prefixRepository.setPrefixEnabled(prefix, enabled);
                refreshLists();
            }

            @Override
            public void onPrefixDelete(String prefix) {
                prefixRepository.removePrefix(prefix);
                refreshLists();
                Toast.makeText(MainActivity.this, R.string.toast_prefix_removed, Toast.LENGTH_SHORT).show();
            }
        });

        whitelistAdapter = new WhitelistAdapter(number -> {
            prefixRepository.removeWhitelistNumber(number);
            refreshLists();
            Toast.makeText(MainActivity.this, R.string.toast_whitelist_removed, Toast.LENGTH_SHORT).show();
        });

        logAdapter = new BlockedCallLogAdapter();

        recyclerPrefixes.setLayoutManager(new LinearLayoutManager(this));
        recyclerPrefixes.setNestedScrollingEnabled(false);
        recyclerPrefixes.setAdapter(prefixAdapter);

        recyclerWhitelist.setLayoutManager(new LinearLayoutManager(this));
        recyclerWhitelist.setNestedScrollingEnabled(false);
        recyclerWhitelist.setAdapter(whitelistAdapter);

        recyclerLog.setLayoutManager(new LinearLayoutManager(this));
        recyclerLog.setNestedScrollingEnabled(false);
        recyclerLog.setAdapter(logAdapter);
    }

    private void setupButtons() {
        findViewById(R.id.btnRequestRole).setOnClickListener(v -> requestCallScreeningRole());

        findViewById(R.id.btnOpenSystemSettings).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS));
            } catch (Exception e) {
                Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnOpenSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btnAddPrefix).setOnClickListener(v -> {
            String input = inputPrefix.getText() != null ? inputPrefix.getText().toString() : "";
            if (prefixRepository.addPrefix(input)) {
                inputPrefix.setText("");
                refreshLists();
                Toast.makeText(this, R.string.toast_prefix_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_prefix_invalid, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnAddWhitelist).setOnClickListener(v -> {
            String input = inputWhitelist.getText() != null ? inputWhitelist.getText().toString() : "";
            if (prefixRepository.addWhitelistNumber(input)) {
                inputWhitelist.setText("");
                refreshLists();
                Toast.makeText(this, R.string.toast_whitelist_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_whitelist_invalid, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnClearLog).setOnClickListener(v -> {
            blockedCallLogRepository.clearLogs();
            Toast.makeText(this, R.string.toast_log_cleared, Toast.LENGTH_SHORT).show();
        });
    }

    private void observeLogs() {
        blockedCallLogRepository.observeLogs().observe(this, logs -> {
            logAdapter.submitList(logs);
            emptyLogText.setVisibility(logs == null || logs.isEmpty() ? TextView.VISIBLE : TextView.GONE);
        });
    }

    private void refreshLists() {
        prefixAdapter.submitList(prefixRepository.getPrefixes());
        whitelistAdapter.submitList(prefixRepository.getWhitelist());
    }

    private void requestCallScreeningRole() {
        if (!isRoleRequestAvailable()) {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent roleIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
        roleRequestLauncher.launch(roleIntent);
    }

    private boolean isRoleRequestAvailable() {
        if (roleManager == null) {
            return false;
        }
        return roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING);
    }

    private boolean isCallScreeningRoleHeld() {
        if (roleManager == null) {
            return false;
        }
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
    }

    private void updateRoleStatus() {
        boolean active = isCallScreeningRoleHeld();

        protectionIndicator.setText(active ? R.string.status_active : R.string.status_inactive);
        roleStatusText.setText(active ? R.string.role_status_active : R.string.role_status_inactive);

        int color = ContextCompat.getColor(this, active ? R.color.active_green : R.color.inactive_red);
        Drawable background = protectionIndicator.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        }
    }
}
