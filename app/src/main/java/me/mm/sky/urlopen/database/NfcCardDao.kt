package me.mm.sky.urlopen.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NfcCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nfcCard: NfcCard)

    @Update
    suspend fun update(nfcCard: NfcCard)

    @Delete
    suspend fun delete(nfcCard: NfcCard)

    @Query("SELECT * FROM `nfc-card` WHERE id = :id")
    suspend fun getNfcCardById(id: Int): NfcCard?

    @Query("SELECT * FROM `nfc-card`")
    fun getAllNfcCards(): Flow<List<NfcCard>>

    @Query("DELETE FROM `nfc-card`")
    suspend fun deleteAll()
}
