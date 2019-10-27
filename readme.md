#redis应用

## redis+token实现接口幂等（防重复）
### 实现
 - 用户发交易前先获取一个token，并存于redis
 - 发交易时，将获取到的token放在请求头中与交易报文一并传与后端
 - 后端，在拦截器里获取请求头里的token
 - 后端，判断请求头里的token是否在redis中存在，不存在，可能发生重复交易，存在则把token从redis中删除
 - 后端，判断token是否删除成功，防止并发情况（一个请求执行到删除token那一步，还没将token删除掉，另一个请求也执行到删除token这一步，我们必须删除动作只能被成功执行一次）

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
 
## 计数器
- incr方法，实现id自增