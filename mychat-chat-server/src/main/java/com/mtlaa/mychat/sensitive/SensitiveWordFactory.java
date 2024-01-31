package com.mtlaa.mychat.sensitive;

import com.mtlaa.mychat.common.utils.sensitiveWord.IWordFactory;
import com.mtlaa.mychat.sensitive.dao.SensitiveWordDao;
import com.mtlaa.mychat.sensitive.domain.SensitiveWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create 2024/1/7 13:56
 */
@Service
public class SensitiveWordFactory implements IWordFactory {
    @Autowired
    private SensitiveWordDao sensitiveWordDao;
    @Override
    public List<String> getWordList() {
        return sensitiveWordDao.list().stream().map(SensitiveWord::getWord).collect(Collectors.toList());
    }
}
