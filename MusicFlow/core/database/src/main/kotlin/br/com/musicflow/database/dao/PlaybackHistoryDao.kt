package br.com.musicflow.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.musicflow.database.entity.PlaybackHistoryEntity

@Dao
interface PlaybackHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: PlaybackHistoryEntity): Long

    @Query(
        """
        SELECT * FROM playback_history
        ORDER BY playedAt ASC, id ASC
        """,
    )
    suspend fun chronological(): List<PlaybackHistoryEntity>

    @Query(
        """
        SELECT trackId FROM playback_history
        GROUP BY trackId
        ORDER BY MAX(playedAt) DESC, trackId ASC
        LIMIT :limit
        """,
    )
    suspend fun recentlyPlayed(limit: Int): List<Long>

    @Query(
        """
        SELECT * FROM playback_history
        ORDER BY playedAt DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun latest(): PlaybackHistoryEntity?

    @Query("SELECT COUNT(*) FROM playback_history")
    suspend fun count(): Int

    @Query("DELETE FROM playback_history")
    suspend fun clear()
}
