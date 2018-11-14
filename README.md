# RedisCmd

支持redis集群的简单redis查询维护工具

## 编译

```
mvn -Dmaven.test.skip=true install
```

编译完成后，`target/rediscmd.jar`就是我们需要的目标，可以将其复制到单独目录备用。

## 使用方法

在jar包当前目录下创建 `config` 目录，并创建 `config/application.properties`文件

在 `application.properties` 中设置redis信息

```
spring.redis.cluster.nodes=<IP地址>:<端口>
spring.redis.password=<密码>
```

然后执行 `java -jar rediscmd.jar`启动

使用方法类似于 redis-cli。`quit`或`exit`命令退出

## 限制

该工具目前只实现了部分redis命令。可用命令如下：

* `keys PATTERN` 
* `get KEY`
* `hget KEY FIELD`
* `hgetall KEY`
* `set KEY VALUE [EX seconds | PX milliseconds]`  这里需要注意，`set`命令不支持 `NX|XX`参数
* `hset KEY FIELD VALUE`
* `del KEY1 KEY2 ...`
* `hdel KEY FIELD1 FIELD2 ...`
* `ttl KEY`
