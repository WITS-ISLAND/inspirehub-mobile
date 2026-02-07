package io.github.witsisland.inspirehub.data.source

import io.github.witsisland.inspirehub.data.dto.TagDto

class MockTagDataSource : TagDataSource {

    private val tags: List<TagDto> = listOf(
        TagDto(id = "tag_1", name = "AI", usageCount = 25, createdAt = "2026-01-10T00:00:00Z"),
        TagDto(id = "tag_2", name = "モバイル", usageCount = 18, createdAt = "2026-01-10T00:00:00Z"),
        TagDto(id = "tag_3", name = "教育", usageCount = 15, createdAt = "2026-01-11T00:00:00Z"),
        TagDto(id = "tag_4", name = "ヘルスケア", usageCount = 12, createdAt = "2026-01-12T00:00:00Z"),
        TagDto(id = "tag_5", name = "フードテック", usageCount = 10, createdAt = "2026-01-13T00:00:00Z"),
        TagDto(id = "tag_6", name = "地域活性化", usageCount = 9, createdAt = "2026-01-14T00:00:00Z"),
        TagDto(id = "tag_7", name = "リモートワーク", usageCount = 8, createdAt = "2026-01-15T00:00:00Z"),
        TagDto(id = "tag_8", name = "シニア", usageCount = 7, createdAt = "2026-01-16T00:00:00Z"),
        TagDto(id = "tag_9", name = "IoT", usageCount = 6, createdAt = "2026-01-17T00:00:00Z"),
        TagDto(id = "tag_10", name = "サステナビリティ", usageCount = 5, createdAt = "2026-01-18T00:00:00Z")
    )

    override suspend fun getPopularTags(limit: Int): List<TagDto> {
        return tags
            .sortedByDescending { it.usageCount ?: 0 }
            .take(limit)
    }

    override suspend fun suggestTags(query: String, limit: Int): List<TagDto> {
        if (query.isBlank()) return emptyList()
        return tags
            .filter { it.name.contains(query, ignoreCase = true) }
            .take(limit)
    }
}
