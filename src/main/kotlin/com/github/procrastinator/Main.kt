package com.github.procrastinator

import org.javacord.api.DiscordApiBuilder

private const val BOT_TOKEN = ""

fun main() {
    DiscordApiBuilder()
        .addListener(ProcrastinatorListener())
        .setToken(BOT_TOKEN)
        .login()
        .join()
}