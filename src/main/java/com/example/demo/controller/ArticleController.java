package com.example.demo.controller;

import com.example.demo.article.ArticleVo;
import com.example.demo.article.service.ArticleService;
import com.example.demo.common.response.ResponseVo;
import com.example.demo.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping("/post")
    public ResponseVo postArticle(@RequestBody Article article){
        boolean isSuccess = articleService.postArticle(article);
        if (isSuccess){
            return ResponseVo.success("文章发布成功！");
        }

        return ResponseVo.error("文章发布失败！");
    }


    @GetMapping("/vote")
    public ResponseVo voteToArticle(Long articleId,Integer userId){

        Boolean isSuccess = articleService.voteToArticle(articleId, userId);
        if (isSuccess){
            return ResponseVo.success("文章投票成功！");
        }

        return ResponseVo.error("文章投票失败！");
    }


    @GetMapping("/list")
    public ResponseVo findArticleList(Integer sortField,Integer sortType){
        List<ArticleVo> orderByCondition = articleService.findOrderByCondition(sortField, sortType);
        return ResponseVo.success(orderByCondition);
    }
}


