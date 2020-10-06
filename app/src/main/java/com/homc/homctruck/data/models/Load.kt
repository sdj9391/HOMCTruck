package com.homc.homctruck.data.models

class Load {
    var id: String? = null
    var ownerId: String? = null
    var nameOfGoods: String? = null
    var typeOfMaterial: String? = null
    var fromCity: String? = null
    var toCity: String? = null
    var typeOfTruck: String? = null
    var perTonRate: Float? = null
    var totalLoadInTons: Float? = null
    var totalAmount: Float? = null
    var transitDaysForTruck: Int? = null
    var expectedPickUpDate: Long? = null
    var createdAt: Long? = null
    var updatedAt: Long? = null
}

// q = toCity, fromCity, pickUpDate