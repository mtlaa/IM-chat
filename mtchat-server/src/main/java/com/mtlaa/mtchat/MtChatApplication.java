package com.mtlaa.mtchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// 实现群聊在线人数查询：在redis中使用一个string类型的key，记录当前在线的人数，在用户上线下线时的事件监听器中对缓存进行异步的处理，上线+1，下线-1
// 敏感词过滤：实现了DFA和AC自动机，可以忽略大小写，以及特殊字符欺骗。AC自动机加载速度慢35%，但是过滤速度更快，快大约30%（与敏感词有关）
// 线程池推送：统一管理项目线程池，通用线程池的bean使用@Primary注解，使其优先被使用。使用Spring提供的线程池，因为其提供了优雅停机的功能；
//              使用装饰器模式在不修改Spring线程工厂的前提下，创建自定义的线程工厂，设置线程的异常捕获
// 分布式锁注解：编程实现和注解声明式实现，编程实现更灵活，注解实现更方便。数据库表中使用幂等键，判断幂等键使用分布式锁，保证幂等性
// 限流注解：见语雀笔记
// 黑名单数据使用redis的set存储，实时更新不过期，避免缓存击穿。见 BlackCache


@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching
public class MtChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(MtChatApplication.class, args);
    }
}
