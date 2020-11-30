package com.homc.homctruck.views.activities

interface StatusChangedListener {
    fun onPending(dataItem: Any)
    fun onConfirmed(dataItem: Any)
    fun onRejected(dataItem: Any)
}

interface RefreshListener {
    fun onRefresh(dataItem: Any)
}