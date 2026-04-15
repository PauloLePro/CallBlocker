package com.example.callblocker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BlockedCallLogDao {

    @Query("SELECT * FROM blocked_calls ORDER BY timestamp DESC")
    LiveData<List<BlockedCallLogEntity>> observeAll();

    @Insert
    void insert(BlockedCallLogEntity item);

    @Query("DELETE FROM blocked_calls")
    void clear();
}
