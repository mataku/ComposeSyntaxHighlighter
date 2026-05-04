package com.mataku.composesyntaxhighlighter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform