package com.pharmax.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pharmax.model.ScrapedArticle;

/**
 * ScraperService — fetches and filters articles from the DW (Deutsche Welle)
 * public RSS feed using Jsoup XML parsing.
 *
 * Source: https://rss.dw.com/rdf/rss-en-all
 * Format: RDF/RSS 1.0 XML — fully static, no JavaScript required.
 *
 * Each {@code <item>} element exposes:
 *   &lt;title&gt;       — headline
 *   &lt;link&gt;        — article URL
 *   &lt;description&gt; — short teaser text
 *   &lt;dc:subject&gt;  — category (used as image fallback label)
 *
 * Features:
 *   - Keyword filtering across title + description (case-insensitive)
 *   - Max {@value #MAX_RESULTS} results per query
 *   - In-memory cache (ConcurrentHashMap) — same keyword never refetches
 *   - Safe element access — no NullPointerException on missing nodes
 *   - Text normalisation: trimmed, HTML entities decoded by Jsoup
 */
public class ScraperService {

    private static final Logger LOG = Logger.getLogger(ScraperService.class.getName());

    /** Maximum articles returned per keyword search. */
    private static final int MAX_RESULTS = 10;

    /** Multiple English RSS feeds for comprehensive health coverage */
    private static final String[] FEED_URLS_EN = {
        "https://www.theguardian.com/society/health/rss",
        "https://feeds.bbci.co.uk/news/health/rss.xml"
    };
    
    /** Multiple French RSS feeds for comprehensive health coverage */
    private static final String[] FEED_URLS_FR = {
        "https://www.santemagazine.fr/feeds/rss",
        "https://www.lemonde.fr/sante/rss.xml"
    };

    /** Source label attached to every scraped article. */
    private static final String SOURCE_NAME_EN = "The Guardian Health";
    private static final String SOURCE_NAME_FR = "Santé Magazine";
    
    // Note: Keywords are passed as parameters to scrapeArticles()

    /** Realistic browser User-Agent to avoid request blocks. */
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36";

    /** HTTP connection timeout (ms). */
    private static final int TIMEOUT_MS = 10_000;

    // ─── In-memory cache: normalised keyword → result list ───────────────────
    private final Map<String, List<ScrapedArticle>> cache = new ConcurrentHashMap<>();

    // ─── Singleton ───────────────────────────────────────────────────────────

    private static ScraperService instance;

    public static synchronized ScraperService getInstance() {
        if (instance == null) {
            instance = new ScraperService();
        }
        return instance;
    }

    private ScraperService() {}

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Returns up to {@value #MAX_RESULTS} articles whose title or
     * description contains the given keyword (case-insensitive).
     *
     * @param keyword search term, trimmed internally
     * @param language language code ("en" for English, "fr" for French), defaults to "en"
     * @return immutable-safe list of previews, never null
     */
    public List<ScrapedArticle> scrapeArticles(String keyword) {
        return scrapeArticles(keyword, "en");
    }

    /**
     * Returns up to {@value #MAX_RESULTS} articles whose title or
     * description contains the given keyword (case-insensitive) in the specified language.
     *
     * @param keyword search term, trimmed internally
     * @param language language code ("en" for English, "fr" for French)
     * @return immutable-safe list of previews, never null
     */
    public List<ScrapedArticle> scrapeArticles(String keyword, String language) {
        if (keyword == null || keyword.trim().isEmpty()) {
            LOG.warning("ScraperService: keyword is null or empty — returning empty list.");
            return Collections.emptyList();
        }

        String key = keyword.trim().toLowerCase();
        String lang = (language != null ? language.toLowerCase() : "en");
        String cacheKey = key + "_" + lang;

        // ── Cache hit ────────────────────────────────────────────────────────
        if (cache.containsKey(cacheKey)) {
            LOG.log(Level.INFO, String.format(
                "ScraperService [CACHE HIT] keyword=\"%s\" language=\"%s\" → %d result(s)",
                key, language, cache.get(cacheKey).size()));
            return cache.get(cacheKey);
        }

        // ── Live fetch ───────────────────────────────────────────────────────
        LOG.log(Level.INFO, String.format(
            "ScraperService [FETCH] keyword=\"%s\" language=\"%s\"", key, language));
        List<ScrapedArticle> results = fetchAndFilter(key, language);

        if (!results.isEmpty()) {
            cache.put(cacheKey, results);
            LOG.log(Level.INFO, String.format(
                "ScraperService: cached %d result(s) for keyword=\"%s\" language=\"%s\"",
                results.size(), key, language));
        } else {
            LOG.log(Level.WARNING, String.format(
                "ScraperService: no results found for keyword=\"%s\" language=\"%s\"",
                key, language));
        }

        return results;
    }

    /** Removes all cached results — forces a fresh fetch on the next call. */
    public void clearCache() {
        cache.clear();
        LOG.info("ScraperService: in-memory cache cleared.");
    }

    /** Returns the number of distinct keywords currently in cache. */
    public int getCacheSize() {
        return cache.size();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * Downloads multiple RSS feeds and returns all items whose title or
     * description contains the keyword. Capped at {@value #MAX_RESULTS}.
     */
    private List<ScrapedArticle> fetchAndFilter(String keyword, String language) {
        List<ScrapedArticle> results = new ArrayList<>();

        // Choose RSS feeds based on language
        String[] feedUrls = FEED_URLS_EN;
        if ("fr".equalsIgnoreCase(language)) {
            feedUrls = FEED_URLS_FR;
        }

        for (String feedUrl : feedUrls) {
            if (results.size() >= MAX_RESULTS) break;

            try {
                LOG.log(Level.INFO, String.format(
                    "ScraperService: connecting to %s for language: %s", feedUrl, language));

                // Parse as XML — Jsoup handles both HTML and XML
                Document doc = Jsoup.connect(feedUrl)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            // Each article is an <item> element inside the RDF document
                Elements items = doc.select("item");
                LOG.log(Level.INFO, String.format(
                    "ScraperService: RSS feed contains %d items total.", items.size()));

                for (Element item : items) {
                    if (results.size() >= MAX_RESULTS) break;

                    ScrapedArticle article = parseItem(item, keyword, language);
                    if (article != null) {
                        results.add(article);
                    }
                }

            } catch (IOException e) {
                LOG.log(Level.SEVERE,
                        "ScraperService: network error connecting to " + feedUrl, e);
            } catch (Exception e) {
                LOG.log(Level.SEVERE,
                        "ScraperService: unexpected error parsing RSS feed.", e);
            }
        }

        return results;
    }

    /**
     * Parses a single RSS {@code <item>} element.
     * Returns null if the item doesn't match the keyword or lacks a title.
     */
    private ScrapedArticle parseItem(Element item, String keyword, String language) {
        try {
            String title       = safeText(item, "title");
            String description = safeText(item, "description");
            String link        = safeText(item, "link");
            // Note: subject (category) not used in current implementation

            // Skip items with no usable title
            if (title.isEmpty()) return null;

            // ── Keyword filter (title OR description must contain keyword) ───
            String titleLower = removeAccents(title.toLowerCase());
            String descLower  = removeAccents(description.toLowerCase());
            String normalizedKeyword = removeAccents(keyword);
            if (!titleLower.contains(normalizedKeyword) && !descLower.contains(normalizedKeyword)) {
                return null;
            }

            // ── Normalise source URL ─────────────────────────────────────────
            // RSS <link> for DW items is already absolute; strip tracking param
            String sourceUrl = stripTrackingParam(link);

            // ── Image: RSS feed has no thumbnail; leave blank (safe default) ─
            String imageUrl = "";

            // ── Truncate very long descriptions to ~200 chars ────────────────
            String shortDesc = truncate(description, 200);
            
            // Choose source name based on language
            String sourceName = "fr".equalsIgnoreCase(language) ? SOURCE_NAME_FR : SOURCE_NAME_EN;

            return new ScrapedArticle(title, shortDesc, imageUrl, sourceName, sourceUrl);

        } catch (Exception e) {
            LOG.log(Level.WARNING, "ScraperService: failed to parse item — skipping.", e);
            return null;
        }
    }

    // ─── CSS / text helpers ──────────────────────────────────────────────────

    /**
     * Selects the first element matching {@code cssSelector} inside
     * {@code parent} and returns its trimmed text, or "" if absent.
     */
    private String safeText(Element parent, String cssSelector) {
        Element el = parent.selectFirst(cssSelector);
        if (el == null) return "";
        return el.text().trim();
    }

    /**
     * Strips the DW maca tracking parameter from a URL.
     * e.g. "https://www.dw.com/en/…/a-123?maca=en-rss-…" → "https://www.dw.com/en/…/a-123"
     */
    private String stripTrackingParam(String url) {
        if (url == null) return "";
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    /**
     * Remove accents from a string (é → e, à → a, etc.)
     * Uses direct string replacement to avoid encoding issues
     */
    private String removeAccents(String text) {
        if (text == null) return "";
        
        // Direct string replacement for common accented characters
        return text.replace("é", "e")
                  .replace("è", "e")
                  .replace("ê", "e")
                  .replace("ë", "e")
                  .replace("É", "E")
                  .replace("È", "E")
                  .replace("Ê", "E")
                  .replace("Ë", "E")
                  .replace("á", "a")
                  .replace("à", "a")
                  .replace("â", "a")
                  .replace("ä", "a")
                  .replace("ã", "a")
                  .replace("å", "a")
                  .replace("Á", "A")
                  .replace("À", "A")
                  .replace("Â", "A")
                  .replace("Ä", "A")
                  .replace("Ã", "A")
                  .replace("Å", "A")
                  .replace("í", "i")
                  .replace("ì", "i")
                  .replace("î", "i")
                  .replace("ï", "i")
                  .replace("Í", "I")
                  .replace("Ì", "I")
                  .replace("Î", "I")
                  .replace("Ï", "I")
                  .replace("ó", "o")
                  .replace("ò", "o")
                  .replace("ô", "o")
                  .replace("ö", "o")
                  .replace("õ", "o")
                  .replace("ø", "o")
                  .replace("Ó", "O")
                  .replace("Ò", "O")
                  .replace("Ô", "O")
                  .replace("Ö", "O")
                  .replace("Õ", "O")
                  .replace("Ø", "O")
                  .replace("ú", "u")
                  .replace("ù", "u")
                  .replace("û", "u")
                  .replace("ü", "u")
                  .replace("Ú", "U")
                  .replace("Ù", "U")
                  .replace("Û", "U")
                  .replace("Ü", "U")
                  .replace("ç", "c")
                  .replace("Ç", "C")
                  .replace("ñ", "n")
                  .replace("Ñ", "N")
                  .replace("ý", "y")
                  .replace("ÿ", "y")
                  .replace("Ý", "Y")
                  .replace("Ÿ", "Y")
                  .replace("ß", "ss");
    }

    /**
     * Truncates {@code text} to at most {@code max} characters,
     * appending "…" if cut. Never returns null.
     */
    private String truncate(String text, int max) {
        if (text == null || text.length() <= max) return text == null ? "" : text;
        return text.substring(0, max).trim() + "…";
    }
}
