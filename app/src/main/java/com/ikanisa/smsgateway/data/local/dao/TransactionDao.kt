package com.ikanisa.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ikanisa.smsgateway.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for parsed transactions.
 */
@Dao
interface TransactionDao {
    
    /**
     * Observe all transactions ordered by parsed time (newest first).
     */
    @Query("SELECT * FROM transactions ORDER BY parsed_at DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions for a specific SMS message.
     */
    @Query("SELECT * FROM transactions WHERE sms_id = :smsId")
    suspend fun getBySmsId(smsId: String): List<TransactionEntity>
    
    /**
     * Get a transaction by its external transaction ID.
     */
    @Query("SELECT * FROM transactions WHERE transaction_id = :txnId LIMIT 1")
    suspend fun getByTransactionId(txnId: String): TransactionEntity?
    
    /**
     * Get transactions by type.
     */
    @Query("SELECT * FROM transactions WHERE transaction_type = :type ORDER BY parsed_at DESC")
    fun getByTypeFlow(type: String): Flow<List<TransactionEntity>>
    
    /**
     * Get transactions within a date range.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE parsed_at BETWEEN :startTime AND :endTime 
        ORDER BY parsed_at DESC
    """)
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<TransactionEntity>
    
    /**
     * Get total amount by transaction type.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE transaction_type = :type")
    suspend fun getTotalAmountByType(type: String): Double
    
    /**
     * Get transaction count.
     */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getCount(): Flow<Int>
    
    /**
     * Insert a new transaction.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    /**
     * Insert multiple transactions.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>
    
    /**
     * Update an existing transaction.
     */
    @Update
    suspend fun update(transaction: TransactionEntity)
    
    /**
     * Delete a transaction by ID.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Delete transactions for a specific SMS.
     */
    @Query("DELETE FROM transactions WHERE sms_id = :smsId")
    suspend fun deleteBySmsId(smsId: String)
    
    /**
     * Delete all transactions.
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
