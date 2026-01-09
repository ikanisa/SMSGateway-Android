package com.ikanisa.smsgateway.data.model

import kotlinx.serialization.Serializable

/**
 * Payer model representing a member in the system.
 */
@Serializable
data class Payer(
    val id: String,
    val phoneNumber: String,
    val name: String? = null,
    val balance: Double = 0.0,
    val currency: String = "RWF",
    val isActive: Boolean = true,
    val groupId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)