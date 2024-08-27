package com.mtlaa.mtchat.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@AllArgsConstructor
@Getter
public enum BlackTypeEnum {
    IP(1),
    UID(2),
    ;

    private final Integer type;

    private static final Map<Integer, BlackTypeEnum> cache;

    static {
        cache = Arrays.stream(BlackTypeEnum.values()).collect(Collectors.toMap(BlackTypeEnum::getType, Function.identity()));
    }

    public static BlackTypeEnum of(Integer type) {
        return cache.get(type);
    }

}
