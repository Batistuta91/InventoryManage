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
 * IMPORTANT / TODO before this runs fully against the real server:
 * 1. SERVICE_URL is CONFIRMED (2026-07) — http://192.168.0.22/InventoryManage/Service.asmx
 * 2. CONFIRMED (2026-07): several methods (e.g. CheckUserLogin2) take a
 *    single strongly-typed object parameter (e.g. `User`) rather than flat
 *    scalar parameters — the .NET method signature is
 *    `CheckUserLogin2(ref User user)`. This was discovered from a
 *    NullReferenceException server-side when we originally sent flat
 *    params. Use [callComplex] with [buildComplexParam] for these; use
 *    [call] only for methods confirmed to take flat scalar parameters.
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

    /** Calls SOAP method [action] with flat scalar [params]. */
    suspend fun call(action: String, params: LinkedHashMap<String, String?> = linkedMapOf()): XmlNode {
        val paramsXml = params.entries.joinToString(separator = "") { (name, value) ->
            "<$name>${escapeXml(value.orEmpty())}</$name>"
        }
        return callXml(action, paramsXml)
    }

    /**
     * Calls SOAP method [action] where the single body parameter is a
     * complex/strongly-typed object (e.g. `User`, `Product`). [paramName]
     * is the parameter name the server expects (usually the lowercase
     * type name, e.g. "user"); [fields] are that object's properties.
     *
     * Build [fields] with LinkedHashMap so field order matches the
     * decompiled data class order — some .NET services are picky about
     * element order inside a complex type.
     */
    suspend fun callComplex(action: String, paramName: String, fields: LinkedHashMap<String, String?>): XmlNode {
        val paramsXml = buildComplexParam(paramName, fields)
        return callXml(action, paramsXml)
    }

    fun buildComplexParam(paramName: String, fields: LinkedHashMap<String, String?>): String {
        val inner = fields.entries.joinToString(separator = "") { (name, value) ->
            "<$name>${escapeXml(value.orEmpty())}</$name>"
        }
        return "<$paramName>$inner</$paramName>"
    }

    /** Low-level call — [paramsXml] is the raw XML that goes inside `<ActionName>...</ActionName>`. */
    suspend fun callXml(action: String, paramsXml: String): XmlNode =
        withContext(Dispatchers.IO) {
            val envelope = buildEnvelope(action, paramsXml)
            val mediaType = "text/xml; charset=utf-8".toMediaType()

            val request = Request.Builder()
                .url(serviceUrl)
                .addHeader("SOAPAction", "\"$NAMESPACE$action\"")
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .post(envelope.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                // Note: ASMX returns HTTP 500 (not 200) for SOAP faults, but
                // the body is still a well-formed <soap:Fault> — parse it
                // either way so the person gets the actual .NET exception
                // message instead of a raw HTTP-error dump.
                if (bodyStr.isBlank()) {
                    throw SoapException("HTTP ${response.code} calling $action with no response body")
                }
                parseSoapResponse(bodyStr, action)
            }
        }

    private fun buildEnvelope(action: String, paramsXml: String): String {
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

        val body = findDeep(root, "Body")
            ?: throw SoapException("HTTP error calling $action, response wasn't SOAP: ${xml.take(500)}")

        findDeep(body, "Fault")?.let { fault ->
            // faultstring on .NET ASMX faults is normally the full nested
            // exception message/stack — genuinely useful for debugging,
            // so surface it as-is rather than truncating.
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
