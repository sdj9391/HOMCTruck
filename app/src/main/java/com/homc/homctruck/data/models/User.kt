package com.homc.homctruck.data.models

class User {
    var id: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var mobileNumber: String? = null
    var password: String? = null
    var panCardNumber: String? = null
    var aadharCardNumber: String? = null
    var role: String? = null
    var isUserVerified: Boolean? = null
    var firebaseAuthToken: String? = null
    var firebaseMessageToken: String? = null
    var createdDate: Long? = null
    var updatedDate: Long? = null

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
    }
}

fun User.getName(): String? {
    val name: StringBuilder = StringBuilder()
    firstName?.let {
        name.append(firstName)
    }
    if (!name.isBlank()) {
        name.append(" ")
    }
    lastName?.let {
        name.append(lastName)
    }
    return name.toString().trim()
}