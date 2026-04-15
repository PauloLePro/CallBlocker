package com.example.callblocker.service;

import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import com.example.callblocker.data.BlockedCallLogRepository;
import com.example.callblocker.data.PrefixRepository;
import com.example.callblocker.util.NumberNormalizer;

public class CallBlockerScreeningService extends CallScreeningService {

    private static final String TAG = "CallBlockerService";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (callDetails == null) {
            return;
        }

        try {
            String rawNumber = extractIncomingNumber(callDetails);
            String normalized = NumberNormalizer.normalizeForComparison(rawNumber);

            if (normalized.isEmpty()) {
                allowCall(callDetails);
                return;
            }

            PrefixRepository prefixRepository = PrefixRepository.getInstance(getApplicationContext());
            if (prefixRepository.isWhitelisted(normalized)) {
                allowCall(callDetails);
                return;
            }

            String matchedPrefix = prefixRepository.findMatchingEnabledPrefix(normalized);
            if (matchedPrefix != null) {
                blockCall(callDetails);
                BlockedCallLogRepository.getInstance(getApplicationContext())
                        .insertLog(rawNumber, matchedPrefix, System.currentTimeMillis());
            } else {
                allowCall(callDetails);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur de filtrage d'appel", e);
            try {
                allowCall(callDetails);
            } catch (Exception ignored) {
            }
        }
    }

    private String extractIncomingNumber(Call.Details details) {
        Uri handle = details.getHandle();
        if (handle == null) {
            return "";
        }

        String number = handle.getSchemeSpecificPart();
        return number != null ? number : "";
    }

    private void blockCall(Call.Details details) {
        CallResponse response = new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build();
        respondToCall(details, response);
    }

    private void allowCall(Call.Details details) {
        CallResponse response = new CallResponse.Builder()
                .setDisallowCall(false)
                .build();
        respondToCall(details, response);
    }
}
