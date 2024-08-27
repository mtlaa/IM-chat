package com.mtlaa.mtchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;

/**
 * Redis的配置类。主要配置基于redis的3个限流Lua脚本
 */
@Configuration
public class RedisConfig {
    /**
     * 令牌桶Lua脚本的哈希值
     * @see RedisConfig#Token_Bucket_Script
     */
    public static String TokenBucketScriptSha;
    /**
     * 固定时间窗口Lua脚本的哈希值
     * @see RedisConfig#Fixed_Time_Script
     */
    public static String FixedTimeScriptSha;
    /**
     * 滑动窗口脚本的哈希值
     * @see RedisConfig#SLIDING_WINDOW_SCRIPT
     */
    public static String SlidingWindowScriptSha;
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        FixedTimeScriptSha = Objects.requireNonNull(redisTemplate.getConnectionFactory())
                .getConnection().scriptLoad(Fixed_Time_Script.getBytes());
        SlidingWindowScriptSha =
                redisTemplate.getConnectionFactory().getConnection().scriptLoad(SLIDING_WINDOW_SCRIPT.getBytes());
        TokenBucketScriptSha =
                redisTemplate.getConnectionFactory().getConnection().scriptLoad(Token_Bucket_Script.getBytes());

        return redisTemplate;
    }

    /**
     * 固定时间窗口限流 LUA 脚本
     * <p>keys: key
     * <p>Args: time, count
     * <p>return: 当前计数。如果返回的当前计数大于count，则超过限制</p>
     */
    private static final String Fixed_Time_Script =
            "local current = redis.call('get', KEYS[1])\n" +
            "if current and tonumber(current) > tonumber(ARGV[2]) then\n" +   // current最大为 count+1
            "   return tonumber(current)\n" +
            "end\n" +
            "current = redis.call('incr', KEYS[1])\n" +         // 'incr' 命令在key不存在时会创建key，并把value设为1
            "if tonumber(current) == 1 then\n" +
            "   redis.call('pexpire', KEYS[1], ARGV[1])\n" +    // 只有key创建的时候设置过期时间
            "end\n" +
            "return tonumber(current)";

    /**
     * 滑动窗口限流 LUA 脚本。基于ZSet，其中 score: current time, member: uuid （避免重复）
     * <p>keys: key</p>
     * <p>Args: time, start, now, uuid, count</p>
     * <p>return: 当前时间窗口内的访问次数</p>
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "redis.call('zadd', KEYS[1], ARGV[3], ARGV[4])\n" +             // 在ZSet中添加当前
            "redis.call('zremrangebyscore', KEYS[1], 0, ARGV[2])\n" +       // 移除时间窗口之外的记录
            "redis.call('pexpire', KEYS[1], ARGV[1])\n" +               // 当前key被访问，刷新ZSet的过期时间
            "local ret = tonumber(redis.call('zcard', KEYS[1]));\n" +   // 统计当前时间窗口内ZSet的记录数，即当前时间窗口内的访问次数
            "if tonumber(ret) > tonumber(ARGV[5]) then\n" +          // 如果当前时间窗口内记录数大于count，删除一条记录
            "   redis.call('zremrangebyrank', KEYS[1], 0, 0)\n" +    // 删除一条记录后ZSet内总数一定等于count。避免添加过多记录占满内存
            "end\n" +
            "return tonumber(ret)";

    /**
     * 令牌桶限流算法 LUA 脚本。基于 hash数据类型，一个key中存储: last_ms, cur_tokens
     * <p>通过存储的上一次增加token的时间last_ms 和 当前时间 cur_ms，可以计算出期间可以产生多少个token，然后更新当前token数 以及 last_ms</p>
     * <p>注意：last_ms只有在这期间生成了至少一个token时才更新</p>
     * <p>keys: key</p>
     * <p>Args: count, rate, timeout</p>
     * <p>return: 1 - 通过；0 - 限流</p>
     */
    private static final String Token_Bucket_Script =
        "redis.replicate_commands();" +  // 开启redis的命令复制模式，保证主从复制和AOF日志的数据一致性。（因为后面的写命令存在不确定性）
        "local times = redis.call('TIME');" +  // 在不同时间执行该Lua脚本，这里获取到的当前时间不同，因此该脚本的写命令存在不确定性。（'TIME' 命令返回 秒 和 微秒）
        "local cur_ms = times[1] * 1000 + math.floor(times[2] / 1000);" +        // 计算当前 毫秒 时间戳
        "local info = redis.call('HMGET', KEYS[1], 'last_ms', 'cur_tokens');" +  // 获取当前key的上一次增加token的时间，以及当前剩余的token
        "if info[1] == false then" +  // 如果 last_ms 为nil不存在，说明是第一次创建该key，接下来初始化key：增加token的时间、当前token数、key的过期时间
        "   redis.call('HMSET', KEYS[1], 'last_ms', cur_ms, 'cur_tokens', ARGV[1]-1);" + // 当前token的数量为token总数-1，本次使用了一个
        "   redis.call('pexpire', KEYS[1], ARGV[3]);" +     // 设置过期时间，避免浪费内存
        "   return 1;" +                                    // 返回 1，代表没有被限流
        "else " +                                           // key已经存在，刷新key，生产token
        "   redis.call('pexpire', KEYS[1], ARGV[3]);" +     // 刷新过期时间，避免key过期导致临界问题
        "   local prod_tokens = math.floor((cur_ms - info[1])/1000 * ARGV[2]);" + // 计算从上一次增加token到现在的时间，可以生产token数
        "   local cur_tokens = info[2] + prod_tokens;" +    // 增加后的总token数，可能超过了令牌桶容量count，需要截断
        "   if prod_tokens >= 1 then" +                     // 只有 prod_tokens>=1，实际生产了token，才刷新上一次token生成的时间
        "       redis.call('HSET', KEYS[1], 'last_ms', info[1] + (prod_tokens/ARGV[2]) * 1000);" +
        "   end;" +
        "   cur_tokens = math.min(cur_tokens, ARGV[1]);" +  // 截断token总数
        "   if cur_tokens >= 1 then" +                      // token总数 >= 1，说明有token可用，返回 1，通过
        "       redis.call('HMSET', KEYS[1], 'cur_tokens', cur_tokens-1);" +  // 更新token数，-1为本次使用一个
        "       return 1;" +
        "   else" +                                         // token总数为0，无token可用，被限流
        "       redis.call('HSET', KEYS[1], 'cur_tokens', 0);" +
        "       return 0;" +                                // 返回 0，被限流
        "   end;" +
        "end;";
}
