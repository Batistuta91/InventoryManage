package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.User
import com.cidev.inventorymanage.network.SoapClient
import org.ksoap2.serialization.SoapObject

/**
 * Wraps the CheckUserLogin2 SOAP call (chosen over CheckUserLogin because
 * the "2" suffix pattern in the legacy code base is consistently the
 * newer, fuller version that returns all the permission flags in one
 * round trip).
 *
 * NOT YET TESTED against the live server. Parameter names below
 * (loginName, password, deviceID) are a first guess based on the matching
 * field names found in the decompiled User class.
 */
class AuthRepository {

    suspend fun login(loginName: String, password: String, deviceId: String): Result<User> {
        return try {
            val params = linkedMapOf<String, Any?>(
                "loginName" to loginName,
                "password" to password,
                "deviceID" to deviceId
            )
            val soapResult: SoapObject = SoapClient.call("CheckUserLogin2", params)
            Result.success(mapToUser(soapResult))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUser(soap: SoapObject): User {
        fun str(name: String) = runCatching { soap.getPropertyAsString(name) }.getOrDefault("")
        fun bool(name: String) = str(name).equals("true", ignoreCase = true)

        return User(
            loginName = str("loginName"),
            sessionID = str("sessionID"),
            isValid = bool("isValid"),
            responseStatus = str("responseStatus"),
            responseMessage = str("responseMessage"),
            isOrderActive = bool("isOrderActive"),
            isTransactionActive = bool("isTransactionActive"),
            isInventoryCountActive = bool("isInventoryCountActive"),
            isInsertIntoStockActive = bool("isInsertIntoStockActive"),
            isOrdersCollectActive = bool("isOrdersCollectActive"),
            isInventoryCycleCountActive = bool("isInventoryCycleCountActive"),
            isDeliveryNoteDraftConfirmActive = bool("isDeliveryNoteDraftConfirmActive"),
            isProductsManageActive = bool("isProductsManageActive"),
            isReturnProductsToSupplyerActive = bool("isReturnProductsToSupplyerActive"),
            isReturnProductsFromClientActive = bool("isReturnProductsFromClientActive"),
            isProductionOrderActive = bool("isProductionOrderActive"),
            isInvGeneralEntryActive = bool("isInvGeneralEntryActive"),
            isInvGeneralExitActive = bool("isInvGeneralExitActive"),
            defaultWarehouseCode = str("defaultWarehouseCode"),
            applicationVersion = str("applicationVersion")
        )
    }
}
