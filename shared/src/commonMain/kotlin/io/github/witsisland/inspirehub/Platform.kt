package io.github.witsisland.inspirehub

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform