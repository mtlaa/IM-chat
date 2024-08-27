package com.mtlaa.mtchat.utils.sensitive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sensitive-word")
public class SensitiveWordProperties {
    private FilterType type;

    @Getter
    @AllArgsConstructor
    public enum FilterType{
        DFA("DFA"),
        AC("AC");
        private final String type;
    }
}
