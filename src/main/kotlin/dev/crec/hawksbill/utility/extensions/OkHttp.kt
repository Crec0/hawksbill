package dev.crec.hawksbill.utility.extensions

import dev.crec.hawksbill.bot
import net.dv8tion.jda.api.utils.data.DataObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun Request.awaitResponse() = suspendCoroutine { continuation ->
    bot.httpClient.newCall(this@awaitResponse).enqueue(object : Callback {

        override fun onResponse(call: Call, response: Response) {
            val body = response.body
            if (body == null) {
                continuation.resumeWithException(
                    IOException("Response body is null. $response")
                )
            } else {
                continuation.resume(DataObject.fromJson(body.bytes()))
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }
    })
}
