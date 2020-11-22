package com.homc.homctruck.data.models

class Truck {
    var id: String? = null
    var ownerName: String? = null
    var chesseNumber: String? = null
    var type: String? = null
    var verificationStatus: String? = null
    var truckNumber: String? = null
    var ownerId: String? = null
    var createdAt: Long? = null
    var updatedAt: Long? = null

    var transactionDetails: TransactionDetails? = null
    var transactionStatus: String? = null
    var transactionDate: Long? = null

    companion object {
        const val TRUCK_STATUS_CONFIRMED = "confirmed"
        const val TRUCK_STATUS_PENDING = "pending"
        const val TRUCK_STATUS_REJECT = "reject"

        const val TRANSACTION_STATUS_SUCCESS = "success"
        const val TRANSACTION_STATUS_SUBMITTED = "submitted"
        const val TRANSACTION_STATUS_FAILURE = "failure"
    }
}

fun getTransactionStatusString(transactionStatus: TransactionStatus): String {
    return when (transactionStatus) {
        TransactionStatus.SUCCESS -> Truck.TRANSACTION_STATUS_SUCCESS
        TransactionStatus.SUBMITTED -> Truck.TRANSACTION_STATUS_SUBMITTED
        TransactionStatus.FAILURE -> Truck.TRANSACTION_STATUS_FAILURE
    }
}

fun getTransactionStatusCode(transactionStatusString: String): TransactionStatus? {
    return when (transactionStatusString) {
        Truck.TRANSACTION_STATUS_SUCCESS -> TransactionStatus.SUCCESS
        Truck.TRANSACTION_STATUS_SUBMITTED -> TransactionStatus.SUBMITTED
        Truck.TRANSACTION_STATUS_FAILURE -> TransactionStatus.FAILURE
        else -> null
    }
}

class TransactionDetails(
    val transactionId: String?,
    val responseCode: String?,
    val approvalRefNo: String?,
    val transactionStatus: TransactionStatus?,
    val transactionRefId: String?,
    val amount: String?
)

enum class TransactionStatus {
    FAILURE, SUCCESS, SUBMITTED
}