package com.jinse.utils;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jinse.constant.RedisConstants;
import com.jinse.entity.Flower;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class RedisClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 缓存设置工具（可传入任何类型的value——因此参数列表有 Object value）
     * @param key
     * @param value
     */
    public void set(String key, Object value, Long time, TimeUnit unit){
        //将value序列化为Json字符串
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 设置缓存，无过期时间
     */
    public void set(String key, Object value) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value));
    }

    /**
     * 获取缓存（返回原始JSON字符串）
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 缓存设置工具（包含逻辑过期时间）
     * @param key       key的前缀
     * @param value     缓存的值
     * @param time      过期时间
     * @param unit      时间单位
     */
    public void setWithLogicalExpire(String key,Object value,Long time,TimeUnit unit){
        //设置逻辑过期时间
        //RedisData类里封装了逻辑过期时间字段，因此可以将value存入redisData对象并给其中的过期时间字段赋值
        RedisData redisData=new RedisData();
        redisData.setData(JSONUtil.toJsonStr(value));
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //将redisData传入redis（本质上就是给value套层壳，使其带上逻辑过期时间）
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }

//    /**
//     * 缓存穿透解决方法
//     * @param id
//     * @return
//     */
//    //参数列表：
//        //R是返回值类型，由于要返回调用者所需要的类型，因此需要调用者将它所需的返回类型传入，因此设置泛型R
//        //keyPrefix:key的前缀，拼接上id后形成完整的key（由于不同应用情境下key的前缀不同，因此要单独传入工具类方法）
//        //ID类型：由于id的类型可能是多种多样的，不一定只是Long，因此要使用泛型ID
//        //从redis中查到的数据是Json字符串类型，由于要返回调用者所需要的类型，因此需要将Json字符串反序列化为R类型并返回，这里用type作为Class<R>
//        //dbFallback：定义一个函数，用于根据id查询数据库，返回R类型数据，因此要传入一个函数，函数的参数为ID类型，返回值为R类型
//    public <R,ID> R queryWithPassThrough(
//            String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback, Long time, TimeUnit util){
//        //构建key
//        String key=keyPrefix+id;
//        //从redis查询缓存
//        String Json=stringRedisTemplate.opsForValue().get(key);
//
//        //判断缓存中是否存在数据【StrUtil.isNotBlank的作用是判断字符串是否不为空且不只包含空格字符】
//        if(StrUtil.isNotBlank(Json)){
//
//            //3.存在，则直接返回
//            log.info("缓存使用成功");
//            log.info("json:{}",Json);
//
//            //判断是否为数组类型【Json.trim()方法用于去掉字符串首尾的空格】
//            if (Json != null && Json.trim().startsWith("[")) {
//                // 2. 使用parseArray方法解析
//                JSONArray jsonArray = JSONUtil.parseArray(Json);
//                // 3. 转换为List<R>
//                 List<R> list = jsonArray.toList(type);
//                 log.info("json:{}",list);
//                 return (R) list;
//            } else {
//                return JSONUtil.toBean(Json,type);//将Json字符串转换为R类对象
//            }
//
//        }
//
//        //如果查找到的数据存在，而没有进入前面的判断中则说明数据为空白字符串或只有空格字符,返回错误
//        if(Json!=null){
//            return null;
//        }
//
//        //如果缓存中查找不到数据，说明未命中，要查询MySQL数据库，根据id查询数据库
//        //由于数据库存储的数据类型多种多样，不确定返回哪一种，因此难以统一，需要用户将相应的数据库查询逻辑函数传入
//        //定义Function<ID,R> dbFallback参数，其中ID表示函数的参数类型，R表示函数的返回类型
//        R data=dbFallback.apply(id);
//        //数据库也不存在则返回错误，并将空值写入redis，防止缓存穿透
//        if(data==null){
//            //为防止内存压力，空值的过期时间设置得短一些
//            stringRedisTemplate.opsForValue().set(keyPrefix+id,"",2,TimeUnit.MINUTES);
//            return null;
//        }
//        //存在于数据库，则写入redis
//        this.setWithLogicalExpire(key,data,time,util);
//        //将数据库查到的信息返回
//        return data;
//    }

    /**
     * 互斥锁解决缓存击穿问题 （双重校验锁 + 递归深度约束）
     * @param id
     * @return
     */
    public <R,ID> R queryWithMutex(
            String keyPrefix,ID id,Class<R> type,Function<ID,R>dbFallback,Long time,TimeUnit util,int retryCount){
        String key=keyPrefix+id;
        //1.从redis查询缓存
        String Json=stringRedisTemplate.opsForValue().get(key);
        //2.判断是否存在
        if(StrUtil.isNotBlank(Json)){
            RedisData redisData = JSONUtil.toBean(Json, RedisData.class);
            //3.如果存在且逻辑未过期，则直接返回
            if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
                log.info("缓存命中，key={}",key);
                return JSONUtil.toBean((String)redisData.getData(),type);
            }
            //4.逻辑已过期，但仍有旧数据，先返回旧数据，然后异步重建缓存
            log.info("缓存逻辑过期，返回旧数据，key={}",key);
            R oldData = JSONUtil.toBean((String)redisData.getData(),type);
            //开启新线程异步重建缓存
            rebuildCache(key,id,dbFallback,time,util);
            return oldData;
        }
        //判断命中的是否为空值,如果不是，则表明打过来是之前被redis设置为空对象的key，可以放行，否则返回错误
        if(Json!=null){
            log.info("缓存为空，key={}",key);
            return null;
        }

        //4.未命中缓存，实现缓存重建
        //4.1获取互斥锁
        String lockKey="lock:"+key;
        try {
            boolean isLock=tryLock(lockKey);
            //4.2判断是否获取成功
            if(!isLock){
                log.info("获取互斥锁失败，key={}",key);
                //4.3如果失败，则休眠一下再重新获取
                Thread.sleep(50);
                //递归重新获取锁,并且递归后会重新判断一次是否获取锁成功
                if(retryCount<RedisConstants.MAX_RETRY_COUNT){ //递归次数不能超过最大次数
                    return queryWithMutex(keyPrefix,id,type,dbFallback,time,util,retryCount+1);
                }
                throw new RuntimeException("系统繁忙，请稍后再试");
            }

            //双重校验（在获取锁期间，可能已被其他线程更新，因此进行二次检查）
            String doubleCheckJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(doubleCheckJson)) {
                RedisData redisData = JSONUtil.toBean(doubleCheckJson, RedisData.class);
                if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
                    return JSONUtil.toBean((String)redisData.getData(), type);
                }
            }

            //4.4如果成功，根据id查询数据库
            log.info("key={}",key);
            log.info("获取互斥锁成功，根据id查询数据库:{}",key,id);
            R data2=dbFallback.apply(id);
            //5.数据库中也不存在则返回错误，并将空值写入redis，防止缓存穿透
            if(data2==null){
                //为防止内存压力，空值的过期时间设置得短一些
                stringRedisTemplate.opsForValue().set(key,"", RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }
            //6.存在于数据库，则写入redis（使用逻辑过期保证读写格式一致）
            this.setWithLogicalExpire(key,data2,time,util);
            //8.将数据库查到的信息返回
            return data2;
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            //7.释放锁
            unLock(lockKey);
        }
    }

    /**
     * 异步重建缓存
     */
    private <R,ID> void rebuildCache(String key,ID id,Function<ID,R>dbFallback,Long time,TimeUnit util){
        new Thread(()->{
            String lockKey="lock:"+key;
            try {
                boolean isLock=tryLock(lockKey);
                if(!isLock){
                    return;
                }
                //再次检查，防止重复重建
                String doubleCheckJson = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(doubleCheckJson)) {
                    RedisData redisData = JSONUtil.toBean(doubleCheckJson, RedisData.class);
                    if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
                        return;
                    }
                }
                R data2=dbFallback.apply(id);
                if(data2!=null){
                    this.setWithLogicalExpire(key,data2,time,util);
                    log.info("缓存重建成功，key={}",key);
                }
            }finally {
                unLock(lockKey);
            }
        }).start();
    }


//    /**
//     * 互斥锁解决缓存击穿问题 （双重校验锁 + 递归深度约束）
//     * @param
//     * @return
//     */
//    public <R,T> R queryWithMutexAndDTO(
//            String keyPrefix,T queryParams,Class<R> type,
//            Function<T,R>dbFallback,Long time,TimeUnit util,int retryCount){
//
//        //构建key
//        String key=KeyGenerator.generateKey(keyPrefix,queryParams);
//
//        //1.从redis查询缓存
//        String Json=stringRedisTemplate.opsForValue().get(key);
//        //2.判断是否存在
//        if(StrUtil.isNotBlank(Json)){
//            RedisData redisData = JSONUtil.toBean(Json, RedisData.class);
//            //3.如果存在且逻辑未过期，则直接返回
//            if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
//                return JSONUtil.toBean(Json,type);
//            }
//        }
//        //判断命中的是否为空值,如果不是，则表明打过来是之前被redis设置为空对象的key，可以放行，否则返回错误
//        if(Json!=null){
//            return null;
//        }
//
//        //4.未命中缓存，实现缓存重建
//        //4.1获取互斥锁
//        String lockKey="lock:"+key;
//        try {
//            //获取锁【如果获取成功，则返回true，否则返回false】
//            boolean isLock=tryLock(lockKey);
//            //4.2判断是否获取成功
//            if(!isLock){
//                //4.3如果失败，则休眠一下再重新获取
//                Thread.sleep(50);
//                //递归重新获取锁,并且递归后会重新判断一次是否获取锁成功
//                if(retryCount<RedisConstants.MAX_RETRY_COUNT){ //递归次数不能超过最大次数
//                    return queryWithMutex(keyPrefix,queryParams,type,dbFallback,time,util,retryCount+1);
//                }
//                throw new RuntimeException("系统繁忙，请稍后再试");
//            }
//
//            //双重校验（在获取锁期间，可能已被其他线程更新，因此进行二次检查）
//            String doubleCheckJson = stringRedisTemplate.opsForValue().get(key);
//            if (StrUtil.isNotBlank(doubleCheckJson)) {
//                //如果缓存已被其他线程更新，则直接返回缓存数据
//                return JSONUtil.toBean(doubleCheckJson, type);
//            }
//
//            //4.4如果成功，根据id查询数据库
//            R data2=dbFallback.apply(queryParams);
//            //5.数据库中也不存在则返回错误，并将空值写入redis，防止缓存穿透
//            if(data2==null){
//                //为防止内存压力，空值的过期时间设置得短一些
//                stringRedisTemplate.opsForValue().set(key,"", RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
//                return null;
//            }
//            //6.存在于数据库，则写入redis（使用逻辑过期保证读写格式一致）
//            this.setWithLogicalExpire(key,data2,time,util);
//            //8.将数据库查到的信息返回
//            return data2;
//        }catch(InterruptedException e){
//            throw new RuntimeException(e);
//        }finally {
//            //7.释放锁
//            unLock(lockKey);
//        }
//    }


    //获取锁
    private boolean tryLock(String key){ //这里的key是自定义的锁key
        //setIfAbsent方法对应的是redis的senx指令，意思是只有没有这个key时才设置成功，如果已经有这个key，则设置失败
        //如果设置成功，则返回true，表明成功获取锁，否则返回false，表明已经存在这个key，设置失败，对应获取锁失败
        //这本质上是利用了setnx的互斥特性，实现的一种互斥锁（这种互斥的原理是：对于同一个key，只能存在一个，如果已经存在了就不能再次设置这个key了）
        //如果设置锁成功，则返回true，否则返回false
        Boolean flag=stringRedisTemplate.opsForValue().setIfAbsent(key,"1",10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag); //使用isTrue方法判空，避免自动装箱产生空值
    }

    //释放锁
    private void unLock(String key){
        stringRedisTemplate.delete(key);
    }

//    /**
//     * 删除单个缓存
//     */
//    public void delete(String key) {
//        stringRedisTemplate.delete(key);
//    }

    /**
     * 按模式批量删除缓存（供 Service 层写操作后清理缓存使用）
     * @param pattern 匹配模式，如 "flower:*"
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keySet = new HashSet<>();
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    keySet.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return keySet;
        });
        
        log.info("删除缓存：{}", keys);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }


}
