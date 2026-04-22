package com.my.ai.cursor.knowledge.application.strategy;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpPdfDocumentFetcher implements DocumentFetcherStrategy {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    @Override
    public byte[] fetch(String sourceUrl) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(sourceUrl))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/pdf,*/*")
                .GET()
                .build();
            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Download pdf failed, status=" + response.statusCode() + ", url=" + sourceUrl);
            }
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (StringUtils.hasText(contentType) && !contentType.toLowerCase().contains("pdf")) {
                throw new IllegalStateException("The source is not a PDF file, contentType=" + contentType);
            }
            byte[] bytes = response.body();
            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException("Downloaded pdf is empty, url=" + sourceUrl);
            }
            return bytes;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to fetch PDF from url: " + sourceUrl, ex);
        }
    }
}
