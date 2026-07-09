package com.cidev.inventorymanage.data

import com.cidev.inventorymanage.data.model.Product
import com.cidev.inventorymanage.network.SoapClient
import org.ksoap2.serialization.SoapObject

class ProductRepository {

    /**
     * Wraps the legacy SearchProduct SOAP method. NOT yet tested against
     * the live server.
     */
    suspend fun searchProduct(sessionId: String, term: String): Result<List<Product>> {
        return try {
            val params = linkedMapOf<String, Any?>(
                "sessionID" to sessionId,
                "searchTerm" to term
            )
            val soapResult: SoapObject = SoapClient.call("SearchProduct", params)
            Result.success(mapProducts(soapResult))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapProducts(root: SoapObject): List<Product> {
        val results = mutableListOf<Product>()
        for (i in 0 until root.propertyCount) {
            val prop = root.getProperty(i)
            if (prop is SoapObject) {
                results += Product(
                    prodID = str(prop, "prodID"),
                    prodBarcode = str(prop, "prodBarcode"),
                    mfrCatalogNum = str(prop, "mfrCatalogNum"),
                    prodDescription = str(prop, "prodDescription"),
                    prodUnit = str(prop, "prodUnit"),
                    prodStorageQuantity = str(prop, "prodStorageQuantity").toDoubleOrNull() ?: 0.0,
                    defaultLocation = str(prop, "defaultLocation")
                )
            }
        }
        return results
    }

    private fun str(obj: SoapObject, name: String) =
        runCatching { obj.getPropertyAsString(name) }.getOrDefault("")
}
