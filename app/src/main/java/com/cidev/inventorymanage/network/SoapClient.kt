package com.cidev.inventorymanage.network

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.concurrent.TimeUnit

/**
 * Small hand-rolled SOAP 1.1 client for the legacy InvManageWebService
 * (Service.asmx). Deliberately doesn't use any SOAP library — ksoap2's
 * dependency chain (kxml/kobjects/me4se) is dead 2004-era code that no
 * longer resolves from any Maven repo, and a raw SOAP 1.1 request/response
 * is simple enough not to need a library at all.
 *
 * IMPORTANT / TODO before this runs against the real server:
 * 1. SERVICE_URL is CONFIRMED (2026-07) — browsing directly to
 *    http://192.168.0.22/InventoryManage/Service.asmx from a phone on the
 *    warehouse Wi-Fi showed the auto-generated service description page
 *    listing all 64 methods, matching exactly what was extracted from the
 *    decompiled exe.
 * 2. Parameter names for each SOAP method are still inferred from the
 *    matching field names in the decompiled data classes (User, Product,
 *    etc) — NOT yet confirmed against the live server. If a call fails
 *    with a SOAP fault about an unrecognized parameter, that's the most
 *    likely fix.
 */
object SoapClient {

    private const val NAMESPACE = "http://tempuri.org/"
    private var serviceUrl: String = "http://192.168.0.22/InventoryManage/Service.asmx"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun configureServer(ip: String, path: String = "/InventoryManage/Service.asmx") {
        serviceUrl = "http://$ip$path"
    }

    /**
     * Calls SOAP method [action] with ordered simple string [params], and
     * returns the parsed `<ActionNameResult>` (or equivalent) body as an
     * [XmlNode] tree.
     */
    suspend fun call(action: String, params: LinkedHashMap<String, String?> = linkedMapOf()): XmlNode =
        withContext(Dispatchers.IO) {
            val envelope = buildEnvelope(action, params)
            val mediaType = "text/xml; charset=utf-8".toMediaType()

            val request = Request.Builder()
                .url(serviceUrl)
                .addHeader("SOAPAction", "\"$NAMESPACE$action\"")
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .post(envelope.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw SoapException("HTTP ${response.code} calling $action: ${bodyStr.take(500)}")
                }
                parseSoapResponse(bodyStr, action)
            }
        }

    private fun buildEnvelope(action: String, params: LinkedHashMap<String, String?>): String {
        val paramsXml = params.entries.joinToString(separator = "") { (name, value) ->
            "<$name>${escapeXml(value.orEmpty())}</$name>"
        }
        return """<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <$action xmlns="$NAMESPACE">$paramsXml</$action>
  </soap:Body>
</soap:Envelope>"""
    }

    private fun escapeXml(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    /**
     * Parses the full SOAP envelope and returns the node one level inside
     * the `<ActionNameResponse>` element (i.e. the actual result payload —
     * typically `<ActionNameResult>` for standard .NET ASMX services, but
     * we just return whatever is there so callers can navigate it either way).
     */
    private fun parseSoapResponse(xml: String, action: String): XmlNode {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        val root = XmlNode("root")
        val stack = ArrayDeque<XmlNode>()
        stack.addLast(root)

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val node = XmlNode(localName(parser.name))
                    stack.last().children.add(node)
                    stack.addLast(node)
                }
                XmlPullParser.TEXT -> {
                    val t = parser.text?.trim().orEmpty()
                    if (t.isNotEmpty()) stack.last().text += t
                }
                XmlPullParser.END_TAG -> {
                    if (stack.size > 1) stack.removeLast()
                }
            }
            event = parser.next()
        }

        val body = findDeep(root, "Body") ?: throw SoapException("No <soap:Body> in response for $action")
        findDeep(body, "Fault")?.let { fault ->
            val faultString = fault.childText("faultstring").ifBlank { "Unknown SOAP fault" }
            throw SoapException("SOAP fault calling $action: $faultString")
        }

        // Standard ASMX shape: Body > ActionNameResponse > ActionNameResult
        val responseNode = findDeep(body, "${action}Response")
            ?: body.children.firstOrNull()
            ?: throw SoapException("Empty <soap:Body> in response for $action")

        return responseNode.child("${action}Result") ?: responseNode
    }

    private fun localName(qname: String): String = qname.substringAfterLast(':')

    private fun findDeep(node: XmlNode, name: String): XmlNode? {
        if (node.name.equals(name, ignoreCase = true)) return node
        for (child in node.children) {
            findDeep(child, name)?.let { return it }
        }
        return null
    }
}

class SoapException(message: String) : Exception(message)
