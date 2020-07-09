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
//    private static final Jedis jedis = RedisPool.getJedis();

    // д��
    public static void write(String prefix,String key,String value)
    {
        String realKey = prefix + key;
        Jedis jedis = null;
        try
        {
            jedis = RedisPool.getJedis();
            jedis.set(realKey,value);
        }
        catch (Exception e)
        {
            System.out.println("д��redisʧ�� : " + e.getMessage());
        }
        finally
        {
            if(jedis != null)
                jedis.close();
        }
    }

    // ����
    public static String read(String prefix,String key)
    {
        String realKey = prefix + key;
        Jedis jedis = null;
        try
        {
            jedis = RedisPool.getJedis();
            return jedis.get(realKey);
        }
        catch (Exception e)
        {
            System.out.println("��redisʧ�ܣ�ʧ�ܵ�key [" + realKey + "] ��ʧ��ԭ�� : " + e.getMessage());
        }
        finally
        {
            if(jedis != null)
                jedis.close();
        }
        return null;
    }

    // ɾ��
    public static Long delete(String prefix,String key)
    {
        String realKey = prefix + key;
        Jedis jedis = null;
        try
        {
            jedis = RedisPool.getJedis();
            return jedis.del(realKey);
        }
        catch (Exception e)
        {
            System.out.println("ɾ��redisʧ�ܣ�ʧ�ܵ�key [" + realKey + "] ��ʧ��ԭ�� : " + e.getMessage());
        }
        finally
        {
            if(jedis != null)
                jedis.close();
        }
        return null;
    }
}
