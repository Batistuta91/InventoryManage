package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.Product
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.network.XmlNode

class ProductRepository {

    /** Wraps the legacy SearchProduct SOAP method. NOT yet tested against the live server. */
    suspend fun searchProduct(sessionId: String, term: String): Result<List<Product>> {
        return try {
            val params = linkedMapOf<String, String?>(
                "sessionID" to sessionId,
                "searchTerm" to term
            )
            val result: XmlNode = SoapClient.call("SearchProduct", params)
            Result.success(mapProducts(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapProducts(root: XmlNode): List<Product> {
        // Result vector shape isn't confirmed yet — this assumes each
        // result item comes back as a child node (commonly named
        // "Product" or repeated under the result root). Adjust once we
        // see a real response.
        return root.children.map { node ->
            Product(
                prodID = node.childText("prodID"),
                prodBarcode = node.childText("prodBarcode"),
                mfrCatalogNum = node.childText("mfrCatalogNum"),
                prodDescription = node.childText("prodDescription"),
                prodUnit = node.childText("prodUnit"),
                prodStorageQuantity = node.childText("prodStorageQuantity").toDoubleOrNull() ?: 0.0,
                defaultLocation = node.childText("defaultLocation")
            )
        }
    }
}
