package com.bankingsystem.mobile

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class Auth007PolicyTest {
    private val domains = listOf(
        "root", "file", "database", "sharedpref", "external",
        "device_root", "device_file", "device_database", "device_sharedpref"
    )

    @Test
    fun mainManifestDisablesBackupAndCleartext() {
        val app = xml("src/main/AndroidManifest.xml")
            .getElementsByTagName("application").item(0) as Element
        assertEquals("false", attr(app, "allowBackup"))
        assertEquals("@xml/backup_rules", attr(app, "fullBackupContent"))
        assertEquals("@xml/data_extraction_rules", attr(app, "dataExtractionRules"))
        assertEquals("false", attr(app, "usesCleartextTraffic"))
        assertEquals("", attr(app, "networkSecurityConfig"))
    }

    @Test
    fun legacyRulesExcludeAllDomains() {
        val root = xml("src/main/res/xml/backup_rules.xml").documentElement
        assertEquals("full-backup-content", root.tagName)
        assertExcluded(root)
    }

    @Test
    fun cloudAndDeviceTransferRulesExcludeAllDomains() {
        val root = xml("src/main/res/xml/data_extraction_rules.xml").documentElement
        val sections = children(root)
        assertEquals("data-extraction-rules", root.tagName)
        assertEquals(listOf("cloud-backup", "device-transfer"), sections.map { it.tagName })
        sections.forEach(::assertExcluded)
        assertEquals(0, root.getElementsByTagName("cross-platform-transfer").length)
    }

    @Test
    fun exactEmulatorHttpExceptionIsDebugOnly() {
        val app = appDirectory()
        assertFalse(File(app, "src/main/res/xml/network_security_config.xml").exists())
        assertFalse(File(app, "src/release/res/xml/network_security_config.xml").exists())
        val manifest = xml("src/debug/AndroidManifest.xml")
            .getElementsByTagName("application").item(0) as Element
        assertEquals("@xml/network_security_config", attr(manifest, "networkSecurityConfig"))
        assertEquals("", attr(manifest, "usesCleartextTraffic"))
        val policies = children(xml("src/debug/res/xml/network_security_config.xml").documentElement)
        assertEquals(listOf("base-config", "domain-config"), policies.map { it.tagName })
        assertEquals("false", policies[0].getAttribute("cleartextTrafficPermitted"))
        assertEquals("true", policies[1].getAttribute("cleartextTrafficPermitted"))
        val domain = children(policies[1]).single()
        assertEquals("domain", domain.tagName)
        assertEquals("10.0.2.2", domain.textContent.trim())
        assertEquals("false", domain.getAttribute("includeSubdomains"))
    }

    private fun assertExcluded(parent: Element) {
        val exclusions = (0 until parent.getElementsByTagName("exclude").length)
            .map { parent.getElementsByTagName("exclude").item(it) as Element }
        assertEquals(domains, exclusions.map { it.getAttribute("domain") })
        assertTrue(exclusions.all { it.getAttribute("path") == "." })
        assertEquals(0, parent.getElementsByTagName("include").length)
    }

    private fun children(element: Element): List<Element> =
        (0 until element.childNodes.length).mapNotNull { element.childNodes.item(it) as? Element }

    private fun attr(element: Element, name: String): String =
        element.getAttributeNS("http://schemas.android.com/apk/res/android", name)

    private fun xml(path: String) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        isXIncludeAware = false
        isExpandEntityReferences = false
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        setFeature("http://xml.org/sax/features/external-general-entities", false)
        setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    }.newDocumentBuilder().parse(File(appDirectory(), path))

    private fun appDirectory(): File {
        var current: File? = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        while (current != null) {
            if (File(current, "src/main/AndroidManifest.xml").isFile) return current
            current = current.parentFile
        }
        throw IllegalStateException("Cannot locate the app module")
    }
}
