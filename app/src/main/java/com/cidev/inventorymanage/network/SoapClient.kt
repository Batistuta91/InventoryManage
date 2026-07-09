package com.cidev.inventorymanage.network

import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper around ksoap2-android that talks to the SAME
 * InvManageWebService (Service.asmx) the Windows Mobile client uses.
 *
 * Status of what's actually verified vs. still assumed:
 *
 * 1. NAMESPACE ("http://tempuri.org/") — verified from the decompiled exe:
 *    every SOAP action string embedded in the binary uses this namespace.
 * 2. IIS virtual directory name ("InventoryManage") — verified from the
 *    admin web page's HTML source (WebResource.axd loads from
 *    "/InventoryManage/").
 * 3. The exact .asmx filename ("Service.asmx") — still an assumption, not
 *    verified anywhere. Confirm by browsing
 *    http://192.168.0.22/InventoryManage/ directly.
 * 4. Exact SOAP parameter names/shapes for any method — NOT verified. No
 *    live call has actually been made against the server yet. Treat
 *    parameter names in AuthRepository / ProductRepository as a first
 *    guess to test, not fact.
 * 5. Device binding — verified real: the admin site has a grid mapping
 *    each login to a device GUID with a "ResetDevices" action, so
 *    deviceID sent from Android must be stable across app launches.
 */
object SoapClient {

    private const val NAMESPACE = "http://tempuri.org/"
    private var serviceUrl: String = "http://192.168.0.22/InventoryManage/Service.asmx"

    fun configureServer(ip: String, path: String = "/InventoryManage/Service.asmx") {
        serviceUrl = "http://$ip$path"
    }

    /**
     * Generic SOAP 1.1 call. [action] is the method name exactly as it
     * appears in the decompiled Service class (e.g. "CheckUserLogin2").
     * [params] is an ordered map of parameter name -> value. NOT yet
     * tested against the live server.
     */
    suspend fun call(action: String, params: LinkedHashMap<String, Any?> = linkedMapOf()): SoapObject =
        withContext(Dispatchers.IO) {
            val request = SoapObject(NAMESPACE, action)
            params.forEach { (name, value) -> request.addProperty(name, value) }

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
                dotNet = true
                setOutputSoapObject(request)
            }

            val transport = HttpTransportSE(serviceUrl, 30_000)
            transport.call(NAMESPACE + action, envelope)

            envelope.response as SoapObject
        }
}
