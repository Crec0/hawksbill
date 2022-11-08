package dev.crec.hawksbill.utility.requests

import dev.crec.hawksbill.config.BotConfig
import dev.crec.hawksbill.utility.extensions.awaitResponse
import okhttp3.HttpUrl
import okhttp3.Request

class TenorAPI(private val config: BotConfig) {

    private val searchEndPointBuilder = HttpUrl.Builder().apply {
        scheme("https")
        host("tenor.googleapis.com")
        addPathSegments("v2/search")
        addQueryParameter("key", config.tenorApiKey)
        addQueryParameter("client_key", "HawksBill")
    }

    suspend fun getRandomGifUrl(query: String): String {
        val url = searchEndPointBuilder.apply {
            addQueryParameter("q", query)
            addQueryParameter("media_filter", "gif")
            addQueryParameter("random", "true")
            addQueryParameter("limit", "1")
        }
        val request = Request.Builder().url(url.build()).get().build()
        val responseObject = request.awaitResponse()

        return responseObject
            .getArray("results")
            .getObject(0)
            .getObject("media_formats")
            .getObject("gif")
            .getString("url")
    }
}
