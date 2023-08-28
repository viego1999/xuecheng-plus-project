# xuecheng-plus-project118

#### 介绍
学成在线项目

#### 软件架构
软件架构说明


#### 安装教程

1.  创建数据，导入数据库文件；
2.  开启 vue 项目：`npm run serve`;
3.  开启 nacos ：`.\startup.cmd -m standalone`;
4.  开启 minio：`.\minio.exe server D:\lessons\Xuecheng\minio_data\data1 D:\lessons\Xuecheng\minio_data\data2 D:\lessons\Xuecheng\minio_data\data3 D:\lessons\Xuecheng\minio_data\data4`;
5.  开启 xxlJob：执行 `mvn package`命令将 admin 项目打包成 jar 包，再进入到 `cd D:\IdeaProjects\xxl-job-2.3.1\xxl-job-admin\target\`，然后执行`java -jar .\xxl-job-admin-2.3.1.jar `
6.  开启 nginx：进入 ` cd D:\lessons\Xuecheng\nginx-1.23.1` 执行 `start .\nginx.exe`；
7.  开启 es 和 kibana：`cd 'D:\SoftServer\elasticsearch-7.9.1\bin\'` ，然后执行 `.\elasticsearch.bat`；进入 `cd 'D:\SoftServer\kibana-7.9.1\bin\'`，执行 `.\kibana.bat`，访问 `http://127.0.0.1:5601`;
8.  开启 rabbitmq 服务：安装好 `erlang` 并配置好环境变量后，执行 `cd 'D:\Program Files\RabbitMQ Server\rabbitmq_server-3.11.8\sbin'` ，然后执行 `rabbitmq-plugins enable rabbitmq_management` ，开启服务：`rabbitmq-server start`，访问 `http://localhost:15672`，账号密码默认为 `guest`;
9.  开启 frp：`cd D:\frps\frp4` 然后执行 `frpc.exe -c frpc.ini`;
10.  开启 redis：`cd d:\Program Files\Redis`，执行命令：`redis-server redis.windows.conf`。

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)