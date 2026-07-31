package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE isLastRead = 1 LIMIT 1")
    fun getLastRead(): Flow<Bookmark?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("DELETE FROM bookmarks WHERE surahId = :surahId AND ayahNumber = :ayahNumber AND isLastRead = 0")
    suspend fun deleteBookmarkByAyah(surahId: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahId = :surahId AND ayahNumber = :ayahNumber AND isLastRead = 0)")
    fun isBookmarked(surahId: Int, ayahNumber: Int): Flow<Boolean>

    @Query("UPDATE bookmarks SET isLastRead = 0 WHERE isLastRead = 1")
    suspend fun clearLastRead()
}
