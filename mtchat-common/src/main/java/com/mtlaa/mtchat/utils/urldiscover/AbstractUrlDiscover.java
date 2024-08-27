package com.mtlaa.mtchat.utils.urldiscover;


import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import com.mtlaa.mtchat.domain.urldiscover.UrlInfo;
import com.mtlaa.mtchat.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.util.Pair;

import javax.annotation.Nullable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO 读

/**
 * Create 2024/1/4 11:14
 */
@Slf4j
public abstract class AbstractUrlDiscover implements UrlTitleDiscover{
    // 匹配链接的正则表达式
    private static final Pattern PATTERN = Pattern.compile(
            "((http|https)://)?(www.)?([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");

    @Nullable
    @Override
    public Map<String, UrlInfo> getUrlContentMap(String content) {
        if(StrUtil.isBlank(content)){
            return new HashMap<>();
        }
        List<String> urls = ReUtil.findAll(PATTERN, content, 0);

        // 并行请求
        List<CompletableFuture<Pair<String, UrlInfo>>> futures = urls.stream().map(url -> CompletableFuture.supplyAsync(()->{
            UrlInfo urlInfo = getContent(url);
            return Objects.isNull(urlInfo) ? null : Pair.of(url, urlInfo);
        })).collect(Collectors.toList());
        CompletableFuture<List<Pair<String, UrlInfo>>> future = FutureUtils.sequenceNonNull(futures);
        //结果组装
        return future.join().stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a));
    }

    @Nullable
    @Override
    public UrlInfo getContent(String url) {
        url = assemble(url);
        Document document = getUrlDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        return UrlInfo.builder()
                .title(getTitle(document))
                .image(getImage(url, document))
                .description(getDescription(document))
                .build();
    }

    private String assemble(String url) {

        if (!StrUtil.startWith(url, "http")) {
            return "http://" + url;
        }

        return url;
    }

    protected Document getUrlDocument(String matchUrl) {
        try {
            Connection connect = Jsoup.connect(matchUrl);
            connect.timeout(2000);
            return connect.get();
        } catch (Exception e) {
            log.error("find error:url:{}", matchUrl, e);
        }
        return null;
    }

    /**
     * 判断链接是否有效
     * 输入链接
     * 返回true或者false
     */
    public static boolean isConnect(String href) {
        //请求地址
        URL url;
        //请求状态码
        int state;
        //下载链接类型
        String fileType;
        try {
            url = new URL(href);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            state = httpURLConnection.getResponseCode();
            fileType = httpURLConnection.getHeaderField("Content-Disposition");
            //如果成功200，缓存304，移动302都算有效链接，并且不是下载链接
            if ((state == 200 || state == 302 || state == 304) && fileType == null) {
                return true;
            }
            httpURLConnection.disconnect();
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
