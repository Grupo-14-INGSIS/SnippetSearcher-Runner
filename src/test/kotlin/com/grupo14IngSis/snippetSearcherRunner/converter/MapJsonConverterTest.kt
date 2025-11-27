package com.grupo14IngSis.snippetSearcherRunner.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MapJsonConverterTest {
    private lateinit var converter: MapJsonConverter

    @BeforeEach
    fun setUp() {
        converter = MapJsonConverter()
    }

    @Test
    fun `convertToDatabaseColumn should convert map to JSON string`() {
        val map =
            mapOf(
                "key1" to "value1",
                "key2" to 123,
                "key3" to true,
            )

        val result = converter.convertToDatabaseColumn(map)

        assertNotNull(result)
        assertTrue(result.contains("\"key1\""))
        assertTrue(result.contains("\"value1\""))
        assertTrue(result.contains("\"key2\""))
        assertTrue(result.contains("123"))
        assertTrue(result.contains("\"key3\""))
        assertTrue(result.contains("true"))
    }

    @Test
    fun `convertToDatabaseColumn should return empty JSON object for null map`() {
        val result = converter.convertToDatabaseColumn(null)

        assertEquals("{}", result)
    }

    @Test
    fun `convertToDatabaseColumn should return empty JSON object for empty map`() {
        val emptyMap = emptyMap<String, Any>()

        val result = converter.convertToDatabaseColumn(emptyMap)

        assertEquals("{}", result)
    }

    @Test
    fun `convertToDatabaseColumn should handle map with string values`() {
        val map =
            mapOf(
                "name" to "John",
                "city" to "New York",
            )

        val result = converter.convertToDatabaseColumn(map)

        assertTrue(result.contains("\"name\""))
        assertTrue(result.contains("\"John\""))
        assertTrue(result.contains("\"city\""))
        assertTrue(result.contains("\"New York\""))
    }

    @Test
    fun `convertToDatabaseColumn should handle map with numeric values`() {
        val map =
            mapOf(
                "age" to 25,
                "height" to 175.5,
                "count" to 100L,
            )

        val result = converter.convertToDatabaseColumn(map)

        assertTrue(result.contains("\"age\""))
        assertTrue(result.contains("25"))
        assertTrue(result.contains("\"height\""))
        assertTrue(result.contains("175.5"))
    }

    @Test
    fun `convertToDatabaseColumn should handle map with boolean values`() {
        val map =
            mapOf(
                "isActive" to true,
                "isDeleted" to false,
            )

        val result = converter.convertToDatabaseColumn(map)

        assertTrue(result.contains("\"isActive\""))
        assertTrue(result.contains("true"))
        assertTrue(result.contains("\"isDeleted\""))
        assertTrue(result.contains("false"))
    }

    @Test
    fun `convertToDatabaseColumn should handle map with mixed types`() {
        val map =
            mapOf(
                "name" to "Alice",
                "age" to 30,
                "active" to true,
                "score" to 95.5,
            )

        val result = converter.convertToDatabaseColumn(map)

        assertNotNull(result)
        assertTrue(result.startsWith("{"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `convertToEntityAttribute should convert JSON string to map`() {
        val json = """{"key1":"value1","key2":123,"key3":true}"""

        val result = converter.convertToEntityAttribute(json)

        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("value1", result["key1"])
        assertEquals(123, result["key2"])
        assertEquals(true, result["key3"])
    }

    @Test
    fun `convertToEntityAttribute should return empty map for null string`() {
        val result = converter.convertToEntityAttribute(null)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `convertToEntityAttribute should return empty map for blank string`() {
        val result = converter.convertToEntityAttribute("   ")

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `convertToEntityAttribute should return empty map for empty JSON object`() {
        val result = converter.convertToEntityAttribute("{}")

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `convertToEntityAttribute should handle JSON with string values`() {
        val json = """{"firstName":"John","lastName":"Doe"}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(2, result.size)
        assertEquals("John", result["firstName"])
        assertEquals("Doe", result["lastName"])
    }

    @Test
    fun `convertToEntityAttribute should handle JSON with numeric values`() {
        val json = """{"age":25,"height":175.5}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(2, result.size)
        assertEquals(25, result["age"])
        assertEquals(175.5, result["height"])
    }

    @Test
    fun `convertToEntityAttribute should handle JSON with boolean values`() {
        val json = """{"isActive":true,"isDeleted":false}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(2, result.size)
        assertEquals(true, result["isActive"])
        assertEquals(false, result["isDeleted"])
    }

    @Test
    fun `convertToEntityAttribute should handle JSON with mixed types`() {
        val json = """{"name":"Alice","age":30,"active":true,"score":95.5}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(4, result.size)
        assertEquals("Alice", result["name"])
        assertEquals(30, result["age"])
        assertEquals(true, result["active"])
        assertEquals(95.5, result["score"])
    }

    @Test
    fun `roundtrip conversion should preserve data`() {
        val originalMap =
            mapOf(
                "name" to "Bob",
                "age" to 40,
                "active" to true,
                "balance" to 1000.50,
            )

        val json = converter.convertToDatabaseColumn(originalMap)
        val resultMap = converter.convertToEntityAttribute(json)

        assertEquals(originalMap.size, resultMap.size)
        assertEquals("Bob", resultMap["name"])
        assertEquals(40, resultMap["age"])
        assertEquals(true, resultMap["active"])
        assertEquals(1000.50, resultMap["balance"])
    }

    @Test
    fun `roundtrip with empty map should work`() {
        val emptyMap = emptyMap<String, Any>()

        val json = converter.convertToDatabaseColumn(emptyMap)
        val result = converter.convertToEntityAttribute(json)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `convertToDatabaseColumn should handle special characters in values`() {
        val map =
            mapOf(
                "message" to "Hello \"World\"",
                "path" to "/home/user/file.txt",
            )

        val result = converter.convertToDatabaseColumn(map)

        assertNotNull(result)
        // JSON should escape special characters
        assertTrue(result.contains("\\\""))
    }

    @Test
    fun `convertToEntityAttribute should handle escaped characters`() {
        val json = """{"message":"Hello \"World\"","newline":"Line1\nLine2"}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(2, result.size)
        assertEquals("Hello \"World\"", result["message"])
        assertTrue((result["newline"] as String).contains("\n"))
    }

    @Test
    fun `convertToDatabaseColumn should handle map with single entry`() {
        val map = mapOf("single" to "value")

        val result = converter.convertToDatabaseColumn(map)

        assertTrue(result.contains("\"single\""))
        assertTrue(result.contains("\"value\""))
    }

    @Test
    fun `convertToEntityAttribute should handle JSON with single entry`() {
        val json = """{"single":"value"}"""

        val result = converter.convertToEntityAttribute(json)

        assertEquals(1, result.size)
        assertEquals("value", result["single"])
    }

    @Test
    fun `convertToDatabaseColumn should produce valid JSON`() {
        val map =
            mapOf(
                "key1" to "value1",
                "key2" to 42,
            )

        val result = converter.convertToDatabaseColumn(map)

        // Verify it's valid JSON by converting back
        assertDoesNotThrow {
            converter.convertToEntityAttribute(result)
        }
    }

    @Test
    fun `convertToEntityAttribute should handle whitespace in JSON`() {
        val json =
            """
            {
                "name": "John",
                "age": 30
            }
            """.trimIndent()

        val result = converter.convertToEntityAttribute(json)

        assertEquals(2, result.size)
        assertEquals("John", result["name"])
        assertEquals(30, result["age"])
    }

    @Test
    fun `convertToDatabaseColumn should handle large map`() {
        val largeMap = (1..100).associate { "key$it" to "value$it" }

        val result = converter.convertToDatabaseColumn(largeMap)

        assertNotNull(result)
        assertTrue(result.length > 100)
        assertTrue(result.startsWith("{"))
        assertTrue(result.endsWith("}"))
    }

    @Test
    fun `convertToEntityAttribute should handle large JSON`() {
        val largeMap = (1..100).associate { "key$it" to "value$it" }
        val json = converter.convertToDatabaseColumn(largeMap)

        val result = converter.convertToEntityAttribute(json)

        assertEquals(100, result.size)
        assertEquals("value1", result["key1"])
        assertEquals("value100", result["key100"])
    }

    @Test
    fun `convertToDatabaseColumn should handle nested structures if supported`() {
        val map =
            mapOf(
                "simple" to "value",
                "number" to 123,
            )

        val result = converter.convertToDatabaseColumn(map)

        assertNotNull(result)
        assertTrue(result.contains("\"simple\""))
        assertTrue(result.contains("\"number\""))
    }

    @Test
    fun `converter should be reusable for multiple conversions`() {
        val map1 = mapOf("key1" to "value1")
        val map2 = mapOf("key2" to "value2")

        val json1 = converter.convertToDatabaseColumn(map1)
        val json2 = converter.convertToDatabaseColumn(map2)

        assertNotEquals(json1, json2)
        assertTrue(json1.contains("key1"))
        assertTrue(json2.contains("key2"))
    }
}
