package com.example.demo.hongbao.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.hongbao.service.GrabHongBaoService;
import com.example.demo.model.HongBao;
import com.example.demo.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * *                            _ooOoo_
 * *                           o8888888o
 * *                           88" . "88
 * *                           (| -_- |)
 * *                            O\ = /O
 * *                        ____/`---'\____
 * *                      .   ' \\| |// `.
 * *                       / \\||| : |||// \
 * *                    / _||||| -:- |||||- \
 * *                       | | \\\ - /// | |
 * *                     | \_| ''\---/'' | |
 * *                      \ .-\__ `-` ___/-. /
 * *                   ___`. .' /--.--\ `. . __
 * *                ."" '< `.___\_<|>_/___.' >'"".
 * *               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * *                 \ \ `-. \_ __\ /__ _/ .-` / /
 * *         ======`-.____`-.___\_____/___.-`____.-'======
 * *                            `=---='
 * *
 * *         .............................................
 * *                  佛祖保佑             永无BUG
 * *          佛曰:
 * *                  写字楼里写字间，写字间里程序员；
 * *                  程序人员写程序，又拿程序换酒钱。
 * *                  酒醒只在网上坐，酒醉还来网下眠；
 * *                  酒醉酒醒日复日，网上网下年复年。
 * *                  但愿老死电脑间，不愿鞠躬老板前；
 * *                  奔驰宝马贵者趣，公交自行程序员。
 * *                  别人笑我忒疯癫，我笑自己命太贱；
 * *                  不见满街漂亮妹，哪个归得程序员？
 *
 * @Author:shixianqing
 * @Date:2019/8/23 15:26
 * @Description:
 **/
@Service
@Slf4j
public class GrabHongBaoServiceImpl implements GrabHongBaoService {

    @Autowired
    private RedisService redisService;

    /**
     * 红包池
     */
    private static final String HONGBAO_POOL_KEY = "hongbao:pool";

    /**
     * 已抢红包用户key
     */
    private static final String  HONGBAO_GRAB_RECORD_KEY = "hongbao:grab:record";

    /**
     * 被抢红包详情
     */
    private static final String HONGBAO_USER = "hongbao:user";

    @Override
    public void afterPropertiesSet() throws Exception {
        //初始化红包
        log.info("................开始初始化20个红包到redis..............");
        initHongBaoPool();
        log.info("............初始化20个红包到redis结束..........");
    }

    /**
     * redis list类型存储
     * 1、一个红包实体转转成json，放到list里
     */
    private void initHongBaoPool() {

        redisService.del(HONGBAO_POOL_KEY);

        List<HongBao> hongBaos = new ArrayList<>();
        for (int i = 1; i < 21; i++){
            HongBao hongBao = HongBao.builder().id(i)
                    .money(new BigDecimal(Math.random()*100).setScale(2,BigDecimal.ROUND_HALF_UP))
                    .build();
            hongBaos.add(hongBao);
        }
        redisService.lpush(HONGBAO_POOL_KEY,hongBaos);
    }

    /**
     * 1、判断用户是否已抢过红包，抢过，直接返回
     * 2、没抢过，从红包池中弹出一个红包
     * 3、判断红包是否存在？存在，将当前用户标记已抢，并把用户与红包关联详情放在list队列上，不存在，直接返回
     *
     * redis.call(command,keys...,args...)
     * @param userId
     */
    @Override
    public void grabHongBao(String userId) {
        log.info("用户：{}，开始抢红包------------",userId);

        String script = "if redis.call('HEXISTS',KEYS[2],KEYS[3]) ~= 0 " +
                "then return cjson.encode({userId=cjson.decode(KEYS[3]),msg=\"已抢过红包\"});\n" +
                "else\n" +
                "  local hongbao = redis.call('rpop',KEYS[1]);\n" +
                "  if hongbao then\n" +
                "    local x = cjson.decode(hongbao);\n" +
                //将编码后的userId，解码
                "    x['userId'] = cjson.decode(KEYS[3]);\n" +
                "    local re = cjson.encode(x);\n" +
                "    redis.call('hset',KEYS[2],KEYS[3],1);\n" +
                "    redis.call('lpush',KEYS[4],re);\n" +
                "    return re;\n" +
                "  end\n" +
                "  return cjson.encode({userId=cjson.decode(KEYS[3]),msg=\"红包已被抢完\"});\n" +
                "end";
        List keys = Arrays.asList(HONGBAO_POOL_KEY,HONGBAO_GRAB_RECORD_KEY,userId,HONGBAO_USER);
        Object result = redisService.eval(script,keys);

        log.info("抢完红包，返回值：{}",JSONObject.toJSONString(result));

    }
}


