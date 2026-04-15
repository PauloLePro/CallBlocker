package com.example.callblocker.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.callblocker.db.AppDatabase;
import com.example.callblocker.db.BlockedCallLogDao;
import com.example.callblocker.db.BlockedCallLogEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockedCallLogRepository {

    private static volatile BlockedCallLogRepository instance;

    private final BlockedCallLogDao dao;
    private final ExecutorService executorService;

    private BlockedCallLogRepository(Context context) {
        dao = AppDatabase.getInstance(context).blockedCallLogDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static BlockedCallLogRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (BlockedCallLogRepository.class) {
                if (instance == null) {
                    instance = new BlockedCallLogRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public LiveData<List<BlockedCallLogEntity>> observeLogs() {
        return dao.observeAll();
    }

    public void insertLog(String rawNumber, String matchedPrefix, long timestamp) {
        executorService.execute(() -> dao.insert(new BlockedCallLogEntity(rawNumber, matchedPrefix, timestamp)));
    }

    public void clearLogs() {
        executorService.execute(dao::clear);
    }
}
