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

    companion object {
        const val TRUCK_STATUS_CONFIRMED = "confirmed"
        const val TRUCK_STATUS_PENDING = "pending"
        const val TRUCK_STATUS_REJECT = "reject"
    }
}