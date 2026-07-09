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
 * IMPORTANT / TODO before this runs against the real server:
 * 1. NAMESPACE and SERVICE_URL below are best-effort guesses reconstructed
 *    from the decompiled binary (SOAP actions all live under
 *    http://tempuri.org/<MethodName>, which is the .NET default namespace
 *    for a web service that was never given a custom [WebService(Namespace=...)]
 *    attribute — so NAMESPACE is very likely correct as-is).
 * 2. SERVICE_URL's path ("/InvManageWebService/Service.asmx") could NOT be
 *    verified because the exe builds it from ServiceIP.txt at runtime and
 *    the literal path string isn't embedded anywhere I could find in the
 *    binary. Please confirm the real path — easiest way: open
 *    http://192.168.0.22/ in a browser on the warehouse LAN and look for
 *    the IIS virtual directory / .asmx file, or check the device registry /
 *    config on one of the working MC55A0 units.
 */
object SoapClient {

    private const val NAMESPACE = "http://tempuri.org/"

    // Updated from the "/InventoryManage/WebResource.axd" path seen in
    // MainMenu.aspx (the browser-based reports/admin site sitting on the
    // same server) — the IIS virtual directory is "InventoryManage", and
    // ASP.NET convention puts a web service at a subfolder matching its
    // C# namespace (InventoryManage.InvManageWebService.Service), so this
    // is now a much more confident guess than before. Still not confirmed
    // by actually hitting the URL.
    private var serviceUrl: String = "http://192.168.0.22/InventoryManage/InvManageWebService/Service.asmx"

    fun configureServer(ip: String, path: String = "/InventoryManage/InvManageWebService/Service.asmx") {
        serviceUrl = "http://$ip$path"
    }

    /**
     * Generic SOAP 1.1 call. [action] is the method name exactly as it
     * appears in the decompiled Service class (e.g. "CheckUserLogin2").
     * [params] is an ordered map of parameter name -> value, matching the
     * method's parameter order in the original .NET service.
     */
    suspend fun call(action: String, params: LinkedHashMap<String, Any?> = linkedMapOf()): SoapObject =
        withContext(Dispatchers.IO) {
            val request = SoapObject(NAMESPACE, action)
            params.forEach { (name, value) -> request.addProperty(name, value) }

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11).apply {
                dotNet = true // .NET-specific envelope quirks (matches the original client)
                setOutputSoapObject(request)
            }

            val transport = HttpTransportSE(serviceUrl, 30_000)
            transport.call(NAMESPACE + action, envelope)

            envelope.response as SoapObject
        }
}
