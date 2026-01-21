package com.example.inspirehub_mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform