package com.homc.homctruck.data.models

class TruckRoute {
    var id: String? = null
    var truckId: String? = null
    var truck: Truck? = null
    var startJourneyDate: Long? = null
    var endJourneyDate: Long? = null
    var fromPlace: Address? = null
    var toPlace: Address? = null
    var createdAt: Long? = null
    var updatedAt: Long? = null
}

// q = toCity, fromCity, toDate, fromDate