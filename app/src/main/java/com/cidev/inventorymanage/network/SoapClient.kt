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
 * (Service.asmx).
 *
 * Why not the ksoap2-android library: tried it first (via a JitPack fork,
 * since the official Maven coordinates don't resolve either). The actual
 * Codemagic build then failed with a real, verified error: ksoap2-android's
 * own POM depends on net.sourceforge.kxml:kxml:2.2.4,
 * net.sourceforge.kobjects:kobjects-j2me, and net.sourceforge.me4se —
 * ancient J2ME-era artifacts under groupIds that were never published to
 * Maven Central or any other currently-reachable repository. This isn't
 * fixable from our side without vendoring those jars manually, so a plain
 * SOAP 1.1 POST + XML parse (using OkHttp + Android's built-in
 * XmlPullParser, both fully supported) is the more reliable path.
 *
 * Status of what's actually verified vs. still assumed:
 * 1. NAMESPACE ("http://tempuri.org/") — verified from the decompiled exe.
 * 2. IIS virtual directory ("InventoryManage") — verified from the admin
 *    site's HTML source.
 * 3. The exact .asmx filename ("Service.asmx") — still an assumption.
 * 4. Exact SOAP parameter names/shapes — NOT verified. No successful live
 *    call has been made against the server yet.
 * 5. Device binding is real — deviceID must be stable across app launches.
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

    /** Low-level call — [paramsXml] is the raw XML inside `<ActionName>...</ActionName>`. */
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
                // ASMX returns HTTP 500 (not 200) for SOAP faults, but the
                // body is still well-formed <soap:Fault> — parse it either
                // way so callers get the real .NET exception message.
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

    /** Parses the SOAP envelope, returning the node inside `<ActionNameResult>` (or its parent if absent). */
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
            val faultString = fault.childText("faultstring").ifBlank { "Unknown SOAP fault" }
            throw SoapException("SOAP fault calling $action: $faultString")
        }

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
