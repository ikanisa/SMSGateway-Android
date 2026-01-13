package com.ikanisa.smsgateway.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for storing parsed transaction data from SMS.
 * 
 * Linked to the original SMS message via foreign key.
 * Stores parsed fields like amount, sender name, transaction type, etc.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["sms_id"]),
        Index(value = ["transaction_id"], unique = true),
        Index(value = ["transaction_type"]),
        Index(value = ["parsed_at"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SmsMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["sms_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionEntity(
    /** Unique identifier for the parsed transaction */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** Reference to source SMS message */
    @ColumnInfo(name = "sms_id")
    val smsId: String,
    
    /** Unique transaction ID from the SMS (if available) */
    @ColumnInfo(name = "transaction_id")
    val transactionId: String? = null,
    
    /** Transaction type (e.g., PAYMENT_RECEIVED, PAYMENT_SENT, WITHDRAWAL) */
    @ColumnInfo(name = "transaction_type")
    val transactionType: String,
    
    /** Transaction amount in local currency */
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    /** Currency code (e.g., RWF, USD) */
    @ColumnInfo(name = "currency")
    val currency: String = "RWF",
    
    /** Name of the payer/sender (for received payments) */
    @ColumnInfo(name = "payer_name")
    val payerName: String? = null,
    
    /** Phone number of the payer/sender */
    @ColumnInfo(name = "payer_phone")
    val payerPhone: String? = null,
    
    /** Name of the payee/recipient (for sent payments) */
    @ColumnInfo(name = "payee_name")
    val payeeName: String? = null,
    
    /** Phone number of the payee/recipient */
    @ColumnInfo(name = "payee_phone")
    val payeePhone: String? = null,
    
    /** Transaction fee if applicable */
    @ColumnInfo(name = "fee")
    val fee: Double? = null,
    
    /** Remaining balance after transaction */
    @ColumnInfo(name = "balance")
    val balance: Double? = null,
    
    /** Timestamp when transaction occurred (from SMS) */
    @ColumnInfo(name = "transaction_at")
    val transactionAt: Long? = null,
    
    /** AI model used for parsing */
    @ColumnInfo(name = "model_used")
    val modelUsed: String? = null,
    
    /** Parsing confidence score (0.0 to 1.0) */
    @ColumnInfo(name = "confidence")
    val confidence: Double? = null,
    
    /** Raw extracted JSON from AI parsing */
    @ColumnInfo(name = "raw_json")
    val rawJson: String? = null,
    
    /** Timestamp when parsing was performed */
    @ColumnInfo(name = "parsed_at")
    val parsedAt: Long = System.currentTimeMillis()
)
