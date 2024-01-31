package com.mtlaa.mychat.common.utils.discover;

import com.mtlaa.mychat.common.utils.discover.domain.UrlInfo;
import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Create 2024/1/4 11:09
 */
public interface UrlTitleDiscover {
    @Nullable
    Map<String, UrlInfo> getUrlContentMap(String content);
    @Nullable
    UrlInfo getContent(String url);

    @Nullable
    String getTitle(Document document);

    @Nullable
    String getDescription(Document document);

    @Nullable
    String getImage(String url, Document document);
}
