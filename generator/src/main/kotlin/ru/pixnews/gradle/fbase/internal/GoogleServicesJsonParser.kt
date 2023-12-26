/*
 * Copyright (c) 2023, the fbase-config-generator-gradle-plugin project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

// Based on google-services-plugin/src/main/kotlin/com/google/gms/googleservices/GoogleServicesTask.kt
// Licensed under the Apache License, Version 2.0

package ru.pixnews.gradle.fbase.internal

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.GradleException
import java.io.File

private const val STATUS_ENABLED = "2"
private const val OAUTH_CLIENT_TYPE_WEB = "3"

internal class GoogleServicesJson(
    val projectInfo: ProjectInfo,
    val clients: List<Client>,
)

internal class ProjectInfo(
    val projectNumber: String,
    val firebaseUrl: String?,
    val projectId: String?,
    val storageBucket: String?,
)

internal class Client(
    val mobileSdkAppId: String?,
    val packageName: String?,
    val trackingId: String?,
    val googleApiKey: String?,
    val defaultWebClientId: String?,
)

internal class JsonParseException : GradleException {
    internal constructor(msg: String) : super(msg)
    internal constructor(msg: String, cause: Throwable) : super(msg, cause)
}

@Suppress("UnusedPrivateProperty", "LOCAL_VARIABLE_EARLY_DECLARATION")
internal fun parseGoogleServicesFile(
    file: File,
): GoogleServicesJson {
    val root = JsonParser.parseReader(file.bufferedReader()) as? JsonObject ?: throw JsonParseException(
        "Malformed root json at ${file.absolutePath}",
    )
    val projectInfo = parseProjectInfo(root)
    val clientInfos = parseClientInfos(root)

    if (clientInfos.isEmpty()) {
        throw JsonParseException("No clients in ${file.absolutePath}")
    }

    return GoogleServicesJson(projectInfo, clientInfos)
}

private fun parseProjectInfo(root: JsonObject): ProjectInfo {
    val projectInfo = root.getAsJsonObject("project_info")
        ?: throw JsonParseException("Missing project_info object")
    return ProjectInfo(
        projectNumber = projectInfo.getStringOrThrow("project_info", "project_number"),
        projectId = projectInfo.getStringOrThrow("project_info", "project_id"),
        firebaseUrl = projectInfo.getStringOrNull("firebase_url"),
        storageBucket = projectInfo.getStringOrNull("storage_bucket"),
    )
}

private fun parseClientInfos(root: JsonObject): List<Client> {
    val array: JsonArray = root.getAsJsonArray("client") ?: return emptyList()
    return array.mapNotNull {
        if (it is JsonObject) {
            parseClient(it)
        } else {
            null
        }
    }
}

private fun parseClient(clientRoot: JsonObject): Client {
    val clientInfo = clientRoot.getAsJsonObject("client_info")
        ?: throw JsonParseException("Client does not have client info")
    val mobileSdkAppId = clientInfo.getStringOrThrow("client_info", "mobilesdk_app_id")
    val packageName = clientInfo.getAsJsonObject("android_client_info")
        ?.getStringOrNull("package_name")

    val trackingId = getService(clientRoot, "analytics_service")
        ?.getAsJsonObject("analytics_property")
        ?.getStringOrNull("tracking_id")

    val googleApiKey = clientRoot.getAsJsonArray("api_key")?.let { keys ->
        keys.firstNotNullOfOrNull { (it as? JsonObject)?.getStringOrNull("current_key") }
    } ?: throw JsonParseException("Client does not have api_key")

    val defaultWebClientId = clientRoot.getAsJsonArray("oauth_client")?.let { clients ->
        clients
            .mapNotNull { it as? JsonObject }
            .firstNotNullOfOrNull { oauthClient ->
                if (oauthClient.getStringOrNull("client_type") == OAUTH_CLIENT_TYPE_WEB) {
                    oauthClient.getStringOrNull("client_id")
                } else {
                    null
                }
            }
    }

    return Client(
        mobileSdkAppId = mobileSdkAppId,
        packageName = packageName,
        trackingId = trackingId,
        googleApiKey = googleApiKey,
        defaultWebClientId = defaultWebClientId,
    )
}

private fun getService(client: JsonObject, serviceName: String): JsonObject? {
    val service = client.getAsJsonObject("services")
        ?.getAsJsonObject(serviceName)
        ?: return null
    return service.takeIf {
        service.getStringOrNull("status") == STATUS_ENABLED
    }
}

private fun JsonObject.getStringOrThrow(group: String, key: String): String {
    val value = this.getAsJsonPrimitive(key) ?: throw JsonParseException("Missing $group/$key object")
    return value.asString
}

private fun JsonObject.getStringOrNull(key: String): String? = getAsJsonPrimitive(key)?.asString
