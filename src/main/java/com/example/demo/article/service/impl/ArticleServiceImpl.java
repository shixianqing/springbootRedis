package com.example.demo.article.service.impl;

import com.example.demo.article.ArticleVo;
import com.example.demo.article.service.ArticleService;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResponseCode;
import com.example.demo.common.utils.SerializeUtil;
import com.example.demo.model.Article;
import com.example.demo.redis.RedisService;
import javafx.beans.binding.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.*;

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
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private RedisService redisService;
    
    private static final String ARTICLE_PREFIX = "arti:";

    private static final String ARTI_POST = "arti:post";

    /**
     * 投票数
     */
    private static final String VOTE_NUM = "vote:num";

    /**
     *评分集合key
     */
    private static final String SCORE_KEY = "scoreKey";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean postArticle(Article article) {
        //生成文章id
        Long id = redisService.incrby(ARTICLE_PREFIX);
        log.info("文章标题：{}，生成文章id：{}",article.getTitle(),id);
        article.setArticleId(id);
        long postTime = System.currentTimeMillis() / 1000;
        article.setPostTime(postTime);
        //将文章以hash数据结构存与redis中
        Map<Object,Object> param = new HashMap<>(1024);
        entityToMap(article,param);

        Boolean result = redisService.hmset(ARTICLE_PREFIX + id, param);
        log.info("文章id：{}，是否保存成功：{}",id,result);

        //保存文章发布时间
        redisService.zadd(ARTI_POST,postTime,ARTICLE_PREFIX + id);

        //自己给自己文章投一票
        result = voteToArticle(id,article.getUserId());

        return result;
    }

    /**
     *
     * @param sortField 排序变量 1-根据分数 2-发布时间
     * @param sortType 排序类型 1-正序 2-降序
     * @return
     */
    @Override
    public List<ArticleVo> findOrderByCondition(Integer sortField, Integer sortType) {
        Set<Object> members = null ;
        List<ArticleVo> articles = new ArrayList<>();
        if (sortField == 1){
            if (sortType == 2){
                members = redisService.zrevrange(SCORE_KEY);

            } else {
                members = redisService.zrange(SCORE_KEY);
            }
        } else {
            if (2 == sortType){
                members = redisService.zrevrange(ARTI_POST);

            } else {
                members = redisService.zrange(ARTI_POST);
            }

        }

        Iterator<Object> iterator = members.iterator();
        while (iterator.hasNext()){
            String member = iterator.next().toString();
            Integer articleId = Integer.valueOf(member.substring(member.indexOf(":") + 1));
            Map<Object, Object> objectObjectMap = redisService.hgetAll(ARTICLE_PREFIX + articleId);
            ArticleVo article = new ArticleVo();
            mapToEntity(article,objectObjectMap);
            Double zscore = redisService.zscore(SCORE_KEY, ARTICLE_PREFIX + articleId);
            article.setScore(zscore);
            Long num = (Long) redisService.hget(VOTE_NUM, ARTICLE_PREFIX + articleId);
            Set<Integer> smembers = redisService.smembers("vote:" + articleId);
            article.setVoteUsers(smembers);
            article.setVoteNum(num);
            articles.add(article);
        }
        return articles;
    }


    /**
     * 文章投票
     * @param articleId 文章id
     * @param userId 用户id
     *
     * redis.call(command,key,argv)
     *
     * 投一票，文章评分加100
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean voteToArticle(Long articleId, Integer userId) {

        Long time = 7 * 24 * 60 * 60L;

        String voteKey = "vote:"+articleId;

        //判断文章发布是否超过7天
        long postTime = (long) redisService.hget(ARTICLE_PREFIX + articleId, "postTime");
        long now = System.currentTimeMillis() / 1000;
        if (now - postTime > time){
            log.error("文章id：{}，该文章发布已超过7天了，不能在投票..........");
            throw new BusinessException(ResponseCode.FAIL.getCode(),
                    "文章id："+articleId+"，该文章发布已超过7天了，不能在投票");
        }

        //将当前用户与文章关联
//        String script = "if redis.call('sadd',KEYS[1],ARGV[1]) then return " +
//                "redis.call('expire',KEYS[1],ARGV[2]) else return 0 end";
//        Boolean result = redisService.eval(script,1,voteKey.getBytes(), SerializeUtil.serialize(userId),
//                SerializeUtil.serialize(time));
        Boolean result = redisService.sadd(voteKey, userId);

        log.info("文章id：{}，用户id：{}，投票是否成功：{}",articleId,userId,result);

        //投票成功，文章评分加100
        if (!result){
            log.info("文章id：{}，用户id：{}，该用户已投过票了.........",articleId,userId);
            throw new BusinessException(ResponseCode.FAIL.getCode(),
                    "文章id："+articleId+"，用户id："+userId+"，该用户已投过票了");
        }
        //判断当前文章是否已经存在
        String scoreKey = "scoreKey";
        //获取key下面的所有成员
        Set<Object> setResults = redisService.zrevrange(scoreKey);
        boolean flag = false;
        Iterator<Object> iterator = setResults.iterator();
        while (iterator.hasNext()){
            if (iterator.next().equals(ARTICLE_PREFIX+articleId)) {
                flag = true;
            }
        }
        //判断该文章是否已被投过票
        if (flag){
            //文章投票数加1
            redisService.hincrby(VOTE_NUM, ARTICLE_PREFIX + articleId, 1L);
            //文章评分加100
            redisService.zincrby(scoreKey,ARTICLE_PREFIX+articleId,100);
        } else {
            //文章投票数加1
            redisService.hset(VOTE_NUM, ARTICLE_PREFIX + articleId, 1L);
            result = redisService.zadd(scoreKey,100,ARTICLE_PREFIX+articleId);
        }

        throw new RuntimeException("故意额");
//        return result;
    }


    private void entityToMap(Article article, Map<Object, Object> param) {

        Field[] declaredFields = article.getClass().getDeclaredFields();
        for (Field field : declaredFields){
            field.setAccessible(true);
            try {
                param.put(field.getName(),field.get(article));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void mapToEntity(ArticleVo article, Map<Object, Object> param){

        for (Map.Entry<Object, Object> objectObjectEntry : param.entrySet()) {

            Object key = objectObjectEntry.getKey();
            Object value = objectObjectEntry.getValue();
            Field[] declaredFields = article.getClass().getDeclaredFields();
            for (Field field : declaredFields){
                field.setAccessible(true);
                if (field.getName().equals(key)){
                    try {
                        field.set(article,value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


