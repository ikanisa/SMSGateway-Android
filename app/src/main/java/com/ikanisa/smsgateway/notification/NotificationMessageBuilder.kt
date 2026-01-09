package com.ikanisa.smsgateway.notification

import com.ikanisa.smsgateway.data.model.NotificationType

/**
 * Builder for creating notification messages based on type and parameters.
 */
object NotificationMessageBuilder {
    
    /**
     * Build a notification message based on type and parameters.
     */
    fun buildMessage(type: NotificationType, parameters: Map<String, String> = emptyMap()): String {
        return when (type) {
            NotificationType.DAILY_REMINDER -> buildDailyReminderMessage(parameters)
            NotificationType.BALANCE_UPDATE -> buildBalanceUpdateMessage(parameters)
            NotificationType.BURIMUNSI_PRODUCTION -> buildBurimunsiProductionMessage(parameters)
            NotificationType.PAYMENT_RECEIVED -> buildPaymentReceivedMessage(parameters)
            NotificationType.PAYMENT_ALLOCATED -> buildPaymentAllocatedMessage(parameters)
            NotificationType.GENERAL -> parameters["message"] ?: "Notification"
        }
    }
    
    private fun buildDailyReminderMessage(parameters: Map<String, String>): String {
        val memberName = parameters["memberName"] ?: "Member"
        val groupName = parameters["groupName"] ?: "the group"
        val amount = parameters["amount"] ?: "your contribution"
        val currency = parameters["currency"] ?: "RWF"
        
        return """Muraho $memberName,
        
Mwongere gukorana na $groupName. Twakumenyeza ko mwongere gutanga $amount $currency.
        
Murakoze!
Burimunsi Production"""
    }
    
    private fun buildBalanceUpdateMessage(parameters: Map<String, String>): String {
        val memberName = parameters["memberName"] ?: "Member"
        val balance = parameters["balance"] ?: "0"
        val currency = parameters["currency"] ?: "RWF"
        val paymentAmount = parameters["paymentAmount"]
        val transactionType = parameters["transactionType"] ?: "transaction"
        
        val baseMessage = """Muraho $memberName,
        
Balance yawe ni: $balance $currency"""
        
        return if (paymentAmount != null) {
            "$baseMessage\n\nPayment ya $paymentAmount $currency yarakoreshejwe. Balance y'ishyuye: $balance $currency"
        } else {
            "$baseMessage\n\nAfter $transactionType, balance y'ishyuye ni: $balance $currency"
        } + "\n\nMurakoze!\nBurimunsi Production"
    }
    
    private fun buildBurimunsiProductionMessage(parameters: Map<String, String>): String {
        val memberName = parameters["memberName"] ?: "Member"
        val message = parameters["message"] ?: "Twakumenyeza amakuru mashya kubijyanye na Burimunsi Production."
        val details = parameters["details"]
        
        val baseMessage = """Muraho $memberName,
        
$message"""
        
        return if (details != null) {
            "$baseMessage\n\n$details"
        } else {
            baseMessage
        } + "\n\nMurakoze!\nBurimunsi Production"
    }
    
    private fun buildPaymentReceivedMessage(parameters: Map<String, String>): String {
        val memberName = parameters["memberName"] ?: "Member"
        val amount = parameters["amount"] ?: "0"
        val currency = parameters["currency"] ?: "RWF"
        val from = parameters["from"] ?: "client"
        
        return """Muraho $memberName,
        
Twakiriye payment ya $amount $currency kuva $from.
        
Murakoze!
Burimunsi Production"""
    }
    
    private fun buildPaymentAllocatedMessage(parameters: Map<String, String>): String {
        val memberName = parameters["memberName"] ?: "Member"
        val allocatedAmount = parameters["allocatedAmount"] ?: "0"
        val currency = parameters["currency"] ?: "RWF"
        val newBalance = parameters["newBalance"] ?: "0"
        
        return """Muraho $memberName,
        
Payment ya $allocatedAmount $currency yarakoreshejwe. Balance y'ishyuye ni: $newBalance $currency.
        
Murakoze!
Burimunsi Production"""
    }
}