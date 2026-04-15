package com.example.callblocker.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_calls")
public class BlockedCallLogEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String rawNumber;

    @NonNull
    public String matchedPrefix;

    public long timestamp;

    public BlockedCallLogEntity(@NonNull String rawNumber, @NonNull String matchedPrefix, long timestamp) {
        this.rawNumber = rawNumber;
        this.matchedPrefix = matchedPrefix;
        this.timestamp = timestamp;
    }
}
