package graduate.util;

import graduate.redis.RedisPool;
import redis.clients.jedis.Jedis;

// ����redisʵ�֣�������fileʵ�ֻ�������ʵ�ַ�ʽ
public class StoreUtil
{
    /**
     * ��־��洢:(log)
     * key : IpAddrPort.log.logEntry.logIndex
     * value : logEntry
     * key : IpAddrPort.log.lastIndex
     * value : lastLogIndex
     *
     * ״̬���洢 : (log)
     * key : state + logindex
     * value : logentry
     * key : state + lastIndex
     * value : statemachine��lastLogEntryIndex
     */
    private static final Jedis jedis = RedisPool.getJedis();

    // д��
    public static void write(String prefix,String key,String value)
    {
        String realKey = prefix + key;
        jedis.set(realKey,value);
    }

    // ����
    public static String read(String prefix,String key)
    {
        String realKey = prefix + key;
        return jedis.get(realKey);
    }

    // ɾ��
    public static Long delete(String prefix,String key)
    {
        String realKey = prefix + key;
        return jedis.del(realKey);
    }
}
