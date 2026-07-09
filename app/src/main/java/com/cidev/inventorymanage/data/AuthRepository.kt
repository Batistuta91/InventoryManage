package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.User
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.network.XmlNode

/**
 * Wraps the CheckUserLogin2 SOAP call (chosen over CheckUserLogin because
 * the "2" suffix pattern in the legacy code base is consistently the
 * newer, fuller version that returns all the permission flags in one
 * round trip).
 *
 * CONFIRMED (2026-07) from a live SOAP fault: the server signature is
 * `CheckUserLogin2(ref User user)` — a single complex `User` parameter,
 * not flat scalar arguments. Field names inside <user> are still inferred
 * from the User model (not yet confirmed one-by-one) — if the server
 * still errors, the next most likely fix is a field name/order mismatch
 * inside the <user> element.
 */
class AuthRepository {

    suspend fun login(loginName: String, password: String, deviceId: String): Result<User> {
        return try {
            val userFields = linkedMapOf<String, String?>(
                "loginName" to loginName,
                "password" to password,
                "deviceID" to deviceId
            )
            val result: XmlNode = SoapClient.callComplex("CheckUserLogin2", "user", userFields)
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
