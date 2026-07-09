package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.User
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.network.XmlNode

/**
 * Wraps the CheckUserLogin2 SOAP call. NOT yet tested against the live
 * server. Parameter names below (loginName, password, deviceID) are a
 * first guess based on the matching field names in the decompiled User
 * class — if the real call fails with a SOAP fault about an unrecognized
 * parameter, or comes back with isValid=false on known-good credentials,
 * that's the first thing to check and adjust.
 */
class AuthRepository {

    suspend fun login(loginName: String, password: String, deviceId: String): Result<User> {
        return try {
            val params = linkedMapOf<String, String?>(
                "loginName" to loginName,
                "password" to password,
                "deviceID" to deviceId
            )
            val result: XmlNode = SoapClient.call("CheckUserLogin2", params)
            Result.success(mapToUser(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUser(node: XmlNode): User {
        fun str(name: String) = node.childText(name)
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
