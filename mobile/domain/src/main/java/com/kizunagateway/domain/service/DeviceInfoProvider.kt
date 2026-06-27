package com.kizunagateway.domain.service

import com.kizunagateway.domain.model.DeviceInfo
import com.kizunagateway.domain.model.SimInfo

interface DeviceInfoProvider {
    fun getDeviceInfo(): DeviceInfo
    fun getSimInfo(simSlot: Int): SimInfo
}
