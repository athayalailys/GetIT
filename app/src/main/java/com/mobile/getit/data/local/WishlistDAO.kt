package com.mobile.getit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_table WHERE userId = :uid")
    fun getWishlistByUser(uid: String): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist_table WHERE id = :id AND userId = :uid")
    suspend fun getWishlistById(id: String, uid: String): WishlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWishlist(wishlist: WishlistEntity)

    // Pastikan kolom 'notes' ada di WishlistEntity
    @Query("UPDATE wishlist_table SET notes = :notes WHERE id = :id AND userId = :uid")
    suspend fun updateNotes(id: String, notes: String, uid: String)

    @Delete
    suspend fun removeFromWishlist(wishlist: WishlistEntity)
}
