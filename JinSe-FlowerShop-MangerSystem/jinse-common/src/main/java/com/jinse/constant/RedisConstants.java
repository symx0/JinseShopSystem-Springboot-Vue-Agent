package com.jinse.constant;


public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;
    public static final String COMMENT_KEY = "comment:";
    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
    public static final String KEY="SHOP_STATUS";
    public static final String DELIVERY_DAYS_KEY = "DELIVERY_DAYS";
    public static final String DELIVERY_FEE_KEY = "DELIVERY_FEE";
    public static final String DELIVERY_FREE_THRESHOLD_KEY = "DELIVERY_FREE_THRESHOLD";
    public static final String PACK_FEE_KEY = "PACK_FEE";

    public static final String ALIPAY_APP_ID = "ALIPAY_APP_ID";
    public static final String ALIPAY_PRIVATE_KEY = "ALIPAY_PRIVATE_KEY";
    public static final String ALIPAY_PUBLIC_KEY = "ALIPAY_PUBLIC_KEY";
    public static final String ALIPAY_NOTIFY_URL = "ALIPAY_NOTIFY_URL";
    public static final String ALIPAY_RETURN_URL = "ALIPAY_RETURN_URL";
    public static final String ALIPAY_SUBJECT_PREFIX = "ALIPAY_SUBJECT_PREFIX";

    public static final String PAYMENT_MODE_KEY = "PAYMENT_MODE";

    public static final String ORDER_SUBMIT_LOCK_KEY = "ORDER_SUBMIT_LOCK:";
    public static final Long ORDER_SUBMIT_LOCK_TTL = 10L;
    public static final String ORDER_SEQ_KEY = "ORDER_SEQ:";

    public static final String ACTIVITY_KEY = "activity:";
    public static final String ACTIVITY_PAGE_KEY = "activity:page:";
    public static final String ACTIVITY_SALE_BY_FLOWER_KEY = "activity:sale:";
    public static final String ACTIVITY_SALE_PAGE_KEY = ":page:";
    public static final String ACTIVITY_STOCK_KEY = ":stock:";

    public static final Long CACHE_ACTIVITY_TTL = 10L;
    public static final Long CACHE_ACTIVITY_PAGE_TTL = 10L;

    public static final String FLOWER_KEY = "flower:";
    public static final String FLOWER_NULL_KEY = "flower:null:";
    public static final Long CACHE_NULL_TTL_FLOWER = 2L;
    public static final String FLOWER_PAGE_KEY = "flower:page:";
    public static final String FLOWER_CATEGORY_KEY = "category:";

    public static final Long CACHE_FLOWER_TTL = 10L;
    public static final Long CACHE_FLOWER_PAGE_TTL = 600L;

    public static final int INIT_RETRY_COUNT = 0; // 初始缓存重建重试次数
    public static final int MAX_RETRY_COUNT = 3; // 最大缓存重建重试次数

}
