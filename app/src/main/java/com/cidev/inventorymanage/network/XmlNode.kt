package com.cidev.inventorymanage.network

/**
 * Minimal generic tree representation of a parsed XML element — stands in
 * for what ksoap2's SoapObject used to give us, without needing that
 * library at all.
 */
class XmlNode(val name: String) {
    var text: String = ""
    val children: MutableList<XmlNode> = mutableListOf()

    /** First direct child matching [childName], or null. */
    fun child(childName: String): XmlNode? = children.firstOrNull { it.name.equals(childName, ignoreCase = true) }

    /** All direct children matching [childName] (used for repeated/array elements). */
    fun childrenNamed(childName: String): List<XmlNode> = children.filter { it.name.equals(childName, ignoreCase = true) }

    /** Text of the first direct child matching [childName], or "" if absent. */
    fun childText(childName: String): String = child(childName)?.text.orEmpty()
}
