package io.kroki.server.response;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import net.sourceforge.plantuml.code.AsciiEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Locale;
import java.security.MessageDigest;

public class Caching {

  private static final Logger logger = LoggerFactory.getLogger(Caching.class);

  private final long compileTime = 1567581178724L;
  private final String version;
  private final DateTimeFormatter httpHeaderFormatter = DateTimeFormatter
    .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    .withZone(ZoneId.of("GMT"));

  public Caching(String version) {
    this.version = version;
  }

  public void addHeaderForCache(HttpServerResponse response, String data) {
    long today = System.currentTimeMillis();
    final int maxAge = 3600 * 24 * 5;
    // Add http headers to force the browser to cache the image
    response.putHeader(HttpHeaders.EXPIRES, httpDate(today + 1000L * maxAge));
    response.putHeader(HttpHeaders.DATE, httpDate(today));
    response.putHeader(HttpHeaders.LAST_MODIFIED, httpDate(compileTime));
    response.putHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + maxAge);
    response.putHeader(HttpHeaders.ETAG, version + internalEtag(data));
  }

  private String httpDate(long millis) {
    return httpHeaderFormatter.format(Instant.ofEpochMilli(millis));
  }

  private String internalEtag(String data) {
    try {
      final AsciiEncoder coder = new AsciiEncoder();
      final MessageDigest msgDigest = MessageDigest.getInstance("MD5");
      msgDigest.update(data.getBytes("UTF-8"));
      final byte[] digest = msgDigest.digest();
      return coder.encode(digest);
    } catch (Exception e) {
      logger.warn("Unable to generate an Etag", e);
      return "NOETAG";
    }
  }
}
