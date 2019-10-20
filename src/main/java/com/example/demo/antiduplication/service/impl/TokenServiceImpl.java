package com.example.demo.antiduplication.service.impl;

import com.example.demo.antiduplication.service.TokenService;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResponseCode;
import com.example.demo.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

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
public class TokenServiceImpl implements TokenService {

    @Autowired
    private RedisService redisService;

    private static final String TOKEN_PREFIX = "token_";

    private static final String TOKEN_NAME = "antitoken";

    @Override
    public String createToken() {
        String uuid = UUID.randomUUID().toString();
        String key = TOKEN_PREFIX + uuid;
        redisService.set(key,uuid);
        return uuid;
    }

    @Override
    public boolean checkToken(HttpServletRequest request) {
        String antiToken = request.getHeader(TOKEN_NAME);
        if (StringUtils.isEmpty(antiToken)){
            antiToken = request.getParameter(TOKEN_NAME);
        }

        if (StringUtils.isEmpty(antiToken)){
            throw new BusinessException(ResponseCode.FAIL.getCode(),"防重复token值不能为空");
        }

        //校验redis缓存中是否存在该token
        if (!redisService.exsit(TOKEN_PREFIX + antiToken)){
            throw new BusinessException(ResponseCode.FAIL.getCode(),"可能正在进行重复提交操作，请稍候操作");
        }

        //删除key
        List<Long> delResults = redisService.del(TOKEN_PREFIX + antiToken);
        //并发情况，如果超过2个以上线程都走到87行，则需要对删除结果进行校验，必须保证只有一次删除操作，
        if (!CollectionUtils.isEmpty(delResults) && delResults.get(0) <=0){
            throw new BusinessException(ResponseCode.FAIL.getCode(),"可能正在进行重复提交操作，请稍候操作");
        }


        return true;
    }
}


