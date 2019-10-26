#redis应用

## redis+token实现接口幂等（防重复）

## 文章投票
### 需求
 - 用户可以发表文章,发表时默认给自己的文章投了一票​
 
 - 用户在查看网站时可以按评分进行排列查看​

 - 用户也可以按照文章发布时间进行排序​

 - 为节约内存，一篇文章发表后，7天内可以投票,7天过后就不能再投票了​

 - 为防止同一用户多次投票，用户只能给一篇文章投一次票
 
### redis使用sql事务
 -  redisTemplate.setEnableTransactionSupport(true);
 -  方法上加 @Transactional(rollbackFor = Exception.class)