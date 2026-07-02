package com.kizunagateway.core.network

import com.kizunagateway.domain.service.NetworkInfoProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkInfoProviderImpl @Inject constructor(
    private val okHttpClient: OkHttpClient
) : NetworkInfoProvider {

    override val serverPort: Int = HttpGatewayServer.DEFAULT_PORT

    override fun getLocalIpAddress(): String? {
        return try {
            var fallback: String? = null
            for (intf in NetworkInterface.getNetworkInterfaces()) {
                if (!intf.isUp || intf.isLoopback) continue
                for (addr in intf.inetAddresses) {
                    if (addr.isLoopbackAddress || addr !is Inet4Address) continue
                    val host = addr.hostAddress ?: continue
                    // Prefer a site-local (LAN) address such as 192.168.x.x / 10.x.x.x
                    if (addr.isSiteLocalAddress) return host
                    if (fallback == null) fallback = host
                }
            }
            fallback
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPublicIpAddress(): String? = withContext(Dispatchers.IO) {
        try {
            val client = okHttpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url("https://api.ipify.org").build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string()?.trim()?.ifBlank { null } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
