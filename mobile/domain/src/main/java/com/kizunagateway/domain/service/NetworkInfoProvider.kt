package com.kizunagateway.domain.service

interface NetworkInfoProvider {
    /** Port the outbound REST API server listens on. */
    val serverPort: Int

    /** Site-local IPv4 address of this device (e.g. Wi-Fi LAN IP), or null if none is available. */
    fun getLocalIpAddress(): String?

    /** Public-facing IP address as seen from the internet, or null on failure/offline. */
    suspend fun getPublicIpAddress(): String?
}
