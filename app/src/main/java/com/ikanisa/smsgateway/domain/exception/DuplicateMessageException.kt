package com.ikanisa.smsgateway.domain.exception

/**
 * Exception thrown when attempting to save a duplicate SMS message.
 */
class DuplicateMessageException(message: String) : Exception(message)
