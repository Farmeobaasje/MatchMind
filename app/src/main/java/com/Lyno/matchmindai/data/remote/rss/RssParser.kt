package com.Lyno.matchmindai.data.remote.rss

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Parser for RSS 2.0 feeds using Android's XmlPullParser.
 * This is a lightweight parser that doesn't require external dependencies.
 */
class RssParser {

    /**
     * Parses an RSS feed from the given InputStream.
     * Returns a list of [RssItem] or empty list if parsing fails.
     *
     * @param inputStream The InputStream containing RSS XML data
     * @param sourceName The name of the feed source (e.g., "BBC", "ESPN")
     * @return List of parsed RSS items
     */
    suspend fun parse(inputStream: InputStream, sourceName: String): List<RssItem> =
        withContext(Dispatchers.IO) {
            try {
                inputStream.use { stream ->
                    parseRssStream(stream, sourceName)
                }
            } catch (e: Exception) {
                // Log error in production, but return empty list to continue with other feeds
                emptyList()
            }
        }

    /**
     * Internal parsing logic using XmlPullParser.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun parseRssStream(inputStream: InputStream, sourceName: String): List<RssItem> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()

        // Skip to the channel tag
        parser.require(XmlPullParser.START_TAG, null, "rss")
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, null, "channel")

        val items = mutableListOf<RssItem>()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "item" -> {
                    val item = readItem(parser, sourceName)
                    if (item != null) {
                        items.add(item)
                    }
                }
                else -> skip(parser)
            }
        }

        return items
    }

    /**
     * Reads a single <item> element from the RSS feed.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser, sourceName: String): RssItem? {
        parser.require(XmlPullParser.START_TAG, null, "item")

        var title = ""
        var description = ""
        var pubDate = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            when (parser.name) {
                "title" -> title = readText(parser)
                "description" -> description = cleanHtml(readText(parser))
                "pubDate" -> pubDate = readText(parser)
                else -> skip(parser)
            }
        }

        // Only return item if it has at least a title
        return if (title.isNotBlank()) {
            RssItem(
                title = title.trim(),
                description = description.trim(),
                pubDate = pubDate.trim(),
                source = sourceName
            )
        } else {
            null
        }
    }

    /**
     * Reads the text content of a tag.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    /**
     * Skips tags we're not interested in.
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    /**
     * Removes HTML tags from description text.
     * Uses a simple regex approach - for production use a proper HTML parser if needed.
     */
    private fun cleanHtml(text: String): String {
        return text
            .replace(Regex("<[^>]*>"), " ")  // Remove HTML tags
            .replace(Regex("\\s+"), " ")     // Normalize whitespace
            .trim()
    }
}
