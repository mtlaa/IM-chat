package com.mtlaa.mtchat.utils.urldiscover;

import cn.hutool.core.util.StrUtil;
import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Create 2024/1/4 11:17
 */
public class PrioritizedUrlDiscover extends AbstractUrlDiscover{
    private final List<UrlTitleDiscover> urlDiscovers = new ArrayList<>(2);
    public PrioritizedUrlDiscover(){
        urlDiscovers.add(new WxUrlDiscover());
        urlDiscovers.add(new CommonUrlDiscover());
    }
    @Nullable
    @Override
    public String getTitle(Document document) {
        for (UrlTitleDiscover urlDiscover : urlDiscovers) {
            String urlTitle = urlDiscover.getTitle(document);
            if (StrUtil.isNotBlank(urlTitle)) {
                return urlTitle;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getDescription(Document document) {
        for (UrlTitleDiscover urlDiscover : urlDiscovers) {
            String urlDescription = urlDiscover.getDescription(document);
            if (StrUtil.isNotBlank(urlDescription)) {
                return urlDescription;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        for (UrlTitleDiscover urlDiscover : urlDiscovers) {
            String urlImage = urlDiscover.getImage(url, document);
            if (StrUtil.isNotBlank(urlImage)) {
                return urlImage;
            }
        }
        return null;
    }
}
