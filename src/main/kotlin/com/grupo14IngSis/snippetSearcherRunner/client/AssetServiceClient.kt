package com.grupo14IngSis.snippetSearcherRunner.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetServiceClient(
  private val restTemplate: RestTemplate,
  @Value("\${app.bucket.url}") private val bucket: String,
) {

  /*
###################
##### M O C K #####
######## | ########
######## V ########
###################
 */

  fun getAsset(container: String, snippet: String): String {
    return snippet
  }

  /*
###################
######## ^ ########
######## | ########
##### M O C K #####
###################
*/

  /*
    private val logger = LoggerFactory.getLogger(AssetServiceClient::class.java)
    private val baseURL = "$bucket/v1/asset"

  private fun url(container: String, key: String): String {
    return "$baseURL/$container/$key"
  }

  private fun defaultHeaders(): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = MediaType.TEXT_PLAIN
    return headers
  }

  fun getAsset(container: String, key: String): String? {
    return try {
      val response = restTemplate.exchange(
        url(container, key),
        HttpMethod.GET,
        HttpEntity<Void>(defaultHeaders()),
        String::class.java
      )
      response.body
    } catch(ex: HttpClientErrorException.NotFound) {
      logger.warn("Asset not found $container/$key")
      null
    }
  }

  fun postAsset(container: String, key: String, content: String): Boolean? {
    return try {
      val entity = HttpEntity<String>(content, defaultHeaders())
      val response = restTemplate.exchange(
        url(container, key),
        HttpMethod.POST,
        entity,
        String::class.java
      )
      response.statusCode.is2xxSuccessful
    } catch (e: Exception) {
      logger.error("Error uploading asset $container/$key", e)
      false
    }
  }

  fun deleteAsset(container: String, key: String): Boolean {
    return try {
      val response = restTemplate.exchange(
        url(container, key),
        HttpMethod.DELETE,
        HttpEntity<Void>(defaultHeaders()),
        String::class.java
      )
      response.statusCode.is2xxSuccessful
    } catch (e: HttpClientErrorException.NotFound) {
      logger.warn("Asset does not exist $container/$key")
      false
    } catch (e: Exception) {
      logger.error("Error deleting asset $container/$key", e)
      false
    }
  }
   */
}
