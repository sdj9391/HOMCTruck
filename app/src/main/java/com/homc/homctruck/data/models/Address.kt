package com.homc.homctruck.data.models

import com.google.gson.annotations.SerializedName

class Address {
    var line1: String? = null
    var line2: String? = null
    var city: String? = null
    var taluka: String? = null
    var district: String? = null
    var state: String? = null
    var pinCode: String? = null
    var lat: String? = null
    var lng: String? = null
}


fun Address.getFullAddress(): String {
    val addressString: StringBuilder = StringBuilder()
    line1?.let {
        addressString.append(line1)
    }
    line2?.let {
        if (!addressString.isBlank()) {
            addressString.append(", ")
        }
        addressString.append(line2)
    }
    city?.let {
        if (!addressString.isBlank()) {
            addressString.append(", ")
        }
        addressString.append(city)
    }
    taluka?.let {
        if (!addressString.isBlank()) {
            addressString.append(", ")
        }
        addressString.append(taluka)
    }
    district?.let {
        if (!addressString.isBlank()) {
            addressString.append(", ")
        }
        addressString.append(district)
    }
    state?.let {
        if (!addressString.isBlank()) {
            addressString.append(", ")
        }
        addressString.append(state)
    }
    pinCode?.let {
        if (!addressString.isBlank()) {
            addressString.append(" - ")
        }
        addressString.append(pinCode)
    }
    return addressString.toString().trim()
}

class ResponsePostalAddress {
    @SerializedName("Message")
    var message: String? = null
    @SerializedName("Status")
    var status: String? = null
    @SerializedName("PostOffice")
    var postalAddressList: MutableList<PostalAddress>? = null
}

class PostalAddress {
    @SerializedName("Block")
    var taluka: String? = null
    @SerializedName("District")
    var district: String? = null
    @SerializedName("State")
    var state: String? = null
}