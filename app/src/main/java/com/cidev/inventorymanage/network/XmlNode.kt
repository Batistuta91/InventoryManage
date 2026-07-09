package com.cidev.inventorymanage.network

/** Minimal generic XML tree node used to walk SOAP responses without a library. */
class XmlNode(val name: String) {
    val children = mutableListOf<XmlNode>()
    var text: String = ""

    fun child(name: String): XmlNode? = children.firstOrNull { it.name.equals(name, ignoreCase = true) }
    fun childText(name: String): String = child(name)?.text.orEmpty()
    fun childrenNamed(name: String): List<XmlNode> = children.filter { it.name.equals(name, ignoreCase = true) }
}
