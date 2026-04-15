package com.example.callblocker.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.callblocker.model.BlockedPrefix;
import com.example.callblocker.util.NumberNormalizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrefixRepository {

    private static final String PREFS_NAME = "french_call_blocker_prefs";
    private static final String KEY_INITIALIZED = "initialized";
    private static final String KEY_PREFIXES = "prefixes_json";
    private static final String KEY_WHITELIST = "whitelist_json";

    private static volatile PrefixRepository instance;

    private final SharedPreferences sharedPreferences;

    private static final String[] DEFAULT_PREFIXES = new String[]{
            "0162", "0163", "0270", "0271", "0377", "0378",
            "0424", "0425", "0568", "0569", "0948", "0949"
    };

    private PrefixRepository(Context context) {
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeDefaultsIfNeeded();
    }

    public static PrefixRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (PrefixRepository.class) {
                if (instance == null) {
                    instance = new PrefixRepository(context);
                }
            }
        }
        return instance;
    }

    private void initializeDefaultsIfNeeded() {
        if (sharedPreferences.getBoolean(KEY_INITIALIZED, false)) {
            return;
        }

        List<BlockedPrefix> defaults = new ArrayList<>();
        for (String prefix : DEFAULT_PREFIXES) {
            defaults.add(new BlockedPrefix(prefix, true));
        }

        savePrefixes(defaults);
        saveWhitelist(new ArrayList<>());
        sharedPreferences.edit().putBoolean(KEY_INITIALIZED, true).apply();
    }

    public synchronized List<BlockedPrefix> getPrefixes() {
        List<BlockedPrefix> prefixes = loadPrefixes();
        prefixes.sort(Comparator.comparing(BlockedPrefix::getPrefix));
        return prefixes;
    }

    public synchronized boolean addPrefix(String input) {
        String normalizedPrefix = NumberNormalizer.normalizePrefix(input);
        if (normalizedPrefix.length() < 4) {
            return false;
        }

        List<BlockedPrefix> prefixes = loadPrefixes();
        for (BlockedPrefix item : prefixes) {
            if (item.getPrefix().equals(normalizedPrefix)) {
                return false;
            }
        }

        prefixes.add(new BlockedPrefix(normalizedPrefix, true));
        savePrefixes(prefixes);
        return true;
    }

    public synchronized void removePrefix(String prefix) {
        List<BlockedPrefix> prefixes = loadPrefixes();
        List<BlockedPrefix> updated = new ArrayList<>();
        for (BlockedPrefix item : prefixes) {
            if (!item.getPrefix().equals(prefix)) {
                updated.add(item);
            }
        }
        savePrefixes(updated);
    }

    public synchronized void setPrefixEnabled(String prefix, boolean enabled) {
        List<BlockedPrefix> prefixes = loadPrefixes();
        for (BlockedPrefix item : prefixes) {
            if (item.getPrefix().equals(prefix)) {
                item.setEnabled(enabled);
            }
        }
        savePrefixes(prefixes);
    }

    public synchronized List<String> getWhitelist() {
        List<String> whitelist = loadWhitelist();
        Collections.sort(whitelist);
        return whitelist;
    }

    public synchronized boolean addWhitelistNumber(String number) {
        String normalized = NumberNormalizer.normalizeForComparison(number);
        if (normalized.length() < 4) {
            return false;
        }

        List<String> whitelist = loadWhitelist();
        if (whitelist.contains(normalized)) {
            return false;
        }

        whitelist.add(normalized);
        saveWhitelist(whitelist);
        return true;
    }

    public synchronized void removeWhitelistNumber(String number) {
        List<String> whitelist = loadWhitelist();
        whitelist.remove(number);
        saveWhitelist(whitelist);
    }

    public synchronized boolean isWhitelisted(String normalizedIncomingNumber) {
        if (normalizedIncomingNumber == null || normalizedIncomingNumber.isEmpty()) {
            return false;
        }
        List<String> whitelist = loadWhitelist();
        return whitelist.contains(normalizedIncomingNumber);
    }

    public synchronized String findMatchingEnabledPrefix(String normalizedIncomingNumber) {
        if (normalizedIncomingNumber == null || normalizedIncomingNumber.isEmpty()) {
            return null;
        }

        List<BlockedPrefix> prefixes = loadPrefixes();
        // Longest prefix first to avoid ambiguous matching.
        prefixes.sort((a, b) -> Integer.compare(b.getPrefix().length(), a.getPrefix().length()));

        for (BlockedPrefix item : prefixes) {
            if (item.isEnabled() && normalizedIncomingNumber.startsWith(item.getPrefix())) {
                return item.getPrefix();
            }
        }
        return null;
    }

    public synchronized void resetPrefixesToDefaults() {
        List<BlockedPrefix> defaults = new ArrayList<>();
        for (String prefix : DEFAULT_PREFIXES) {
            defaults.add(new BlockedPrefix(prefix, true));
        }
        savePrefixes(defaults);
    }

    public synchronized void replaceAll(List<BlockedPrefix> prefixes, List<String> whitelist) {
        Set<String> uniquePrefixes = new HashSet<>();
        List<BlockedPrefix> sanitizedPrefixes = new ArrayList<>();
        for (BlockedPrefix item : prefixes) {
            String normalizedPrefix = NumberNormalizer.normalizePrefix(item.getPrefix());
            if (normalizedPrefix.length() >= 4 && uniquePrefixes.add(normalizedPrefix)) {
                sanitizedPrefixes.add(new BlockedPrefix(normalizedPrefix, item.isEnabled()));
            }
        }

        Set<String> uniqueWhitelist = new HashSet<>();
        List<String> sanitizedWhitelist = new ArrayList<>();
        for (String number : whitelist) {
            String normalizedNumber = NumberNormalizer.normalizeForComparison(number);
            if (normalizedNumber.length() >= 4 && uniqueWhitelist.add(normalizedNumber)) {
                sanitizedWhitelist.add(normalizedNumber);
            }
        }

        savePrefixes(sanitizedPrefixes);
        saveWhitelist(sanitizedWhitelist);
        sharedPreferences.edit().putBoolean(KEY_INITIALIZED, true).apply();
    }

    private List<BlockedPrefix> loadPrefixes() {
        String raw = sharedPreferences.getString(KEY_PREFIXES, "[]");
        List<BlockedPrefix> output = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String prefix = obj.optString("prefix", "");
                boolean enabled = obj.optBoolean("enabled", true);
                if (!prefix.isEmpty()) {
                    output.add(new BlockedPrefix(prefix, enabled));
                }
            }
        } catch (JSONException ignored) {
        }
        return output;
    }

    private void savePrefixes(List<BlockedPrefix> prefixes) {
        JSONArray array = new JSONArray();
        for (BlockedPrefix item : prefixes) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("prefix", item.getPrefix());
                obj.put("enabled", item.isEnabled());
                array.put(obj);
            } catch (JSONException ignored) {
            }
        }
        sharedPreferences.edit().putString(KEY_PREFIXES, array.toString()).apply();
    }

    private List<String> loadWhitelist() {
        String raw = sharedPreferences.getString(KEY_WHITELIST, "[]");
        List<String> output = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String number = array.optString(i, "");
                if (!number.isEmpty()) {
                    output.add(number);
                }
            }
        } catch (JSONException ignored) {
        }
        return output;
    }

    private void saveWhitelist(List<String> whitelist) {
        JSONArray array = new JSONArray();
        for (String number : whitelist) {
            array.put(number);
        }
        sharedPreferences.edit().putString(KEY_WHITELIST, array.toString()).apply();
    }
}
