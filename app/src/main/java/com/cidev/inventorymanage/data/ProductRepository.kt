package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.Product
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.network.XmlNode

class ProductRepository {

    /**
     * Wraps the legacy SearchProduct SOAP method. Results come back as a
     * repeated element under the result node — each match becomes one
     * child XmlNode, which mapProducts() turns into a Product.
     *
     * NOTE: CheckUserLogin2 turned out to take a single complex `User`
     * parameter rather than flat scalars (see AuthRepository) — it's
     * plausible SearchProduct follows the same pattern (e.g. takes a
     * `Product` object with prodBarcode/sessionID-like fields set). Left
     * as flat params for now since it's untested; if this throws a
     * NullReferenceException-style SOAP fault, switch to
     * `SoapClient.callComplex("SearchProduct", "product", ...)` the same
     * way AuthRepository does.
     */
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
        // Individual product nodes are usually named "Product" under a
        // wrapping array/list element — fall back to direct children if
        // the server doesn't wrap them.
        val productNodes = root.childrenNamed("Product").ifEmpty { root.children }

        return productNodes.map { prop ->
            Product(
                prodID = prop.childText("prodID"),
                prodBarcode = prop.childText("prodBarcode"),
                mfrCatalogNum = prop.childText("mfrCatalogNum"),
                prodDescription = prop.childText("prodDescription"),
                prodUnit = prop.childText("prodUnit"),
                prodStorageQuantity = prop.childText("prodStorageQuantity").toDoubleOrNull() ?: 0.0,
                defaultLocation = prop.childText("defaultLocation")
            )
        }
    }
}
