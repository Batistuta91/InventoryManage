package com.cidev.inventorymanage.data.model

data class Product(
    var prodID: String = "",
    var prodBarcode: String = "",
    var mfrCatalogNum: String = "",
    var prodDescription: String = "",
    var prodUnit: String = "",
    var warehouseID: String = "",
    var remark: String = "",
    var defaultQuantity: Double = 0.0,
    var prodQuantity: Double = 0.0,
    var prodPrice: Double = 0.0,
    var currency: String = "",
    var prodQuantityInOrder: Double = 0.0,
    var prodQuantityCount: Double = 0.0,
    var prodStorageQuantity: Double = 0.0,
    var prodMinQuantity: Double = 0.0,
    var baseQty: Double = 0.0,
    var isNewProduct: Boolean = false,
    var isRequireSerialNum: Boolean = false,
    var isRequireBatchNum: Boolean = false,
    var isStockItem: Boolean = false,
    var isMultiplePackagingTypes: Boolean = false,
    var defaultLocation: String = "",
    var isEditable: Boolean = true,
    var responseStatus: String = "",
    var responseMessage: String = ""
)

data class ApiResponse(
    var responseID: Int = 0,
    var status: ResponseStatus = ResponseStatus.FAILURE,
    var message: String = ""
)

enum class ResponseStatus { SUCCESS, FAILURE }
