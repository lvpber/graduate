package graduate.util;

import graduate.redis.RedisPool;
import redis.clients.jedis.Jedis;

// 先用redis实现，将来用file实现或者其他实现方式
public class StoreUtil
{
    /**
     * 日志项存储:(log)
     * key : IpAddrPort.log.logEntry.logIndex
     * value : logEntry
     * key : IpAddrPort.log.lastIndex
     * value : lastLogIndex
     *
     * 状态机存储 : (log)
     * key : state + logindex
     * value : logentry
     * key : state + lastIndex
     * value : statemachine的lastLogEntryIndex
     */
    private static final Jedis jedis = RedisPool.getJedis();

    // 写入
    public static void write(String prefix,String key,String value)
    {
        String realKey = prefix + key;
        jedis.set(realKey,value);
    }

    // 读出
    public static String read(String prefix,String key)
    {
        String realKey = prefix + key;
        return jedis.get(realKey);
    }

    // 删除
    public static Long delete(String prefix,String key)
    {
        String realKey = prefix + key;
        return jedis.del(realKey);
    }
}
