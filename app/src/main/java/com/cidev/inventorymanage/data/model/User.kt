package com.cidev.inventorymanage.data.model

/**
 * Mirrors InventoryManage.InvManageWebService.User from the legacy exe.
 * These *IsXxxActive flags are what the server uses to turn warehouse
 * modules on/off per user — the Android main menu should be built
 * dynamically from these, exactly like the original FrmMain does.
 */
data class User(
    var loginName: String = "",
    var password: String = "",
    var deviceID: String = "",
    var sessionID: String = "",
    var isValid: Boolean = false,

    var responseStatus: String = "",
    var responseMessage: String = "",
    var responseID: Int = 0,

    // ---- Module toggles (drive the main menu) ----
    var isOrderActive: Boolean = false,
    var isTransactionActive: Boolean = false,
    var isInventoryCountActive: Boolean = false,
    var isInsertIntoStockActive: Boolean = false,
    var isOrdersCollectActive: Boolean = false,
    var isInventoryCycleCountActive: Boolean = false,
    var isDeliveryNoteDraftConfirmActive: Boolean = false,
    var isProductsManageActive: Boolean = false,
    var isReturnProductsToSupplyerActive: Boolean = false,
    var isReturnProductsFromClientActive: Boolean = false,
    var isProductionOrderActive: Boolean = false,
    var isInvGeneralEntryActive: Boolean = false,
    var isInvGeneralExitActive: Boolean = false,

    // ---- Pick & Pack behaviour flags ----
    var isHandleStorageLocation: Boolean = false,
    var isPickPackCanCreateDeliveryNote: Boolean = false,
    var isPickPackCanCreateInvoice: Boolean = false,
    var isPickPackDocDefaultInvoice: Boolean = false,
    var isPickPackReturnToSearch: Boolean = false,
    var isPickPackShowPreEnteredBatchLocations: Boolean = false,
    var isPickPackShowOnlyPreEnteredBatchLocations: Boolean = false,
    var isPickPackDocUserCanChangeType: Boolean = false,
    var isPackUserCanAddProducts: Boolean = false,
    var isPickAndPackPasswordApprovalActive: Boolean = false,
    var isPackBlockNegativeStock: Boolean = false,
    var isLocationCanUseReservedStock: Boolean = false,
    var isPackMustEnterSurface: Boolean = false,
    var isPackQuantityCanBeMoreThanPick: Boolean = false,

    // ---- Defaults ----
    var defaultWarehouseCode: String = "",
    var defaultReturnPurchaseWarehouseCode: String = "",
    var defaultReturnFromClientWarehouseCode: String = "",
    var passwordApproval: String = "",
    var defaultAccountID: String = "",
    var defaultAccountName: String = "",
    var defaultProductQuantity: Double = 0.0,
    var productionWarehouseID: String = "",

    // ---- Search / scanning behaviour ----
    var isSearchAccountBarcodeActive: Boolean = false,
    var isSearchProductBarcodeActive: Boolean = false,
    var isStockEntrySearchOnlyBySupplyer: Boolean = false,
    var isRequiredScanProductForDocuments: Boolean = false,

    // ---- Inventory count behaviour ----
    var isInvCountLoadsQuantity: Boolean = false,
    var isInvCountMandatoryToFillAllLocations: Boolean = false,
    var isInvCountAutoShowSearchProductScreen: Boolean = false,
    var isInvCountCanSetDefaultLocation: Boolean = false,

    // ---- Inventory entry behaviour ----
    var isInvEntryCanCreateDraft: Boolean = false,
    var isInvEntryCanSelectReserveInvoice: Boolean = false,
    var isInvEntrySelectDefaultReserveInvoice: Boolean = false,
    var isInvEntryBatchMustEnterExpirationDate: Boolean = false,
    var isInvEntryCanAddProducts: Boolean = false,
    var isInvEntryCanSetDefaultLocation: Boolean = false,

    var isReturnFromClientCanCreateDocWithoutRef: Boolean = false,
    var isTransactionLoadDestinationLocations: Boolean = false,
    var isTransactionAddAccount: Boolean = false,

    var applicationVersion: String = "",
    var hasUpgrade: Boolean = false,
    var upgradeHash: String = ""
)
