package io.github.witsisland.inspirehub.data.auth

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.CoreCrypto.*

/**
 * iOS版 PKCE Code Challenge生成
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun generateCodeChallengeInternal(codeVerifier: String): String {
    val data = codeVerifier.encodeToByteArray()

    // SHA256ハッシュ計算
    val hash = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    data.usePinned { dataPinned ->
        hash.usePinned { hashPinned ->
            CC_SHA256(
                dataPinned.addressOf(0),
                data.size.toUInt(),
                hashPinned.addressOf(0).reinterpret()
            )
        }
    }

    // Base64 URL-safe エンコード
    val nsData = hash.toNSData()
    val base64 = nsData.base64EncodedStringWithOptions(0uL)

    // URL-safe に変換（+ → -, / → _, = を削除）
    return base64
        .replace("+", "-")
        .replace("/", "_")
        .replace("=", "")
}

/**
 * ByteArray を NSData に変換
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this.size.toULong()
        )
    }
}
