package com.grupo14IngSis.snippetSearcherRunner.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class AssetServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${app.bucket.url}") private val bucket: String,
) {
    private val logger = LoggerFactory.getLogger(AssetServiceClient::class.java)
    private val baseURL = "$bucket/v1/asset"

    private fun url(
        container: String,
        key: String,
    ): String {
        return "$baseURL/$container/$key"
    }

    private fun defaultHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        return headers
    }

    fun getAsset(
        container: String,
        key: String,
    ): String? {
        return try {
            val response =
                restTemplate.exchange(
                    url(container, key),
                    HttpMethod.GET,
                    HttpEntity<Void>(defaultHeaders()),
                    String::class.java,
                )
            response.body
        } catch (ex: HttpClientErrorException.NotFound) {
            logger.warn("Asset not found $container/$key")
            null
        }
    }

    fun postAsset(
        container: String,
        key: String,
        content: String,
    ): Int {
        return try {
            val entity = HttpEntity<String>(content, defaultHeaders())
            val response =
                restTemplate.exchange(
                    url(container, key),
                    HttpMethod.PUT,
                    entity,
                    String::class.java,
                )
            response.statusCode.value()
        } catch (e: Exception) {
            logger.error("Error uploading asset $container/$key", e)
            500
        }
    }

    fun deleteAsset(
        container: String,
        key: String,
    ): Int {
        return try {
            val response =
                restTemplate.exchange(
                    url(container, key),
                    HttpMethod.DELETE,
                    HttpEntity<Void>(defaultHeaders()),
                    String::class.java,
                )
            response.statusCode.value()
        } catch (e: HttpClientErrorException.NotFound) {
            logger.warn("Asset does not exist $container/$key")
            404
        } catch (e: Exception) {
            logger.error("Error deleting asset $container/$key", e)
            500
        }
    }
}
