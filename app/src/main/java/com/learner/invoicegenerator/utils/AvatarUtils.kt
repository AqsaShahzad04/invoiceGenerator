package com.learner.invoicegenerator.utils

object AvatarUtils {

    private val avatarColors = listOf(
        "#876B5F",
        "#A87C5F",
        "#7C8E70",
        "#8E6B70",
        "#5F6F87"
    )

    fun getLetter(name: String): String {
        if (name.isBlank()) return "?"

        val words = name.trim().split(" ").filter { it.isNotBlank() }

        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            words.size == 1 -> words[0].first().uppercase()
            else -> "?"
        }
    }

    fun getColor(name: String): String {
        val colorIndex = Math.abs(name.hashCode() % avatarColors.size)
        return avatarColors[colorIndex]
    }
}