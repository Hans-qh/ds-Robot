package edu.bupt.robot;
/*
创建过程中遇到的问题：
1：@EnableSwagger2无法使用。原因：网络原因导致包满意下载完整，删除重新下
2：@RunWith 注解无法使用。导入junit包即可
3：无法创建UserDao。 解决：启动类上需要配置tk的包扫描 @MapperScan("edu.bupt.robot.dao")
4：Mapper无法导入。解决：引入tk的依赖包：mapper-spring-boot-starter
5：连接数据库失败。原因：配置文件中，用户名，密码，驱动名，不要使用IDEA提升的带下划线的那个
    正确的应该是 username,password,driverClassName
6: Swagger:swagger TypeError: Failed to fetch 解决：将127.0.0.1 改成 localhost


7:测试插入，存在会怎样
    //结果：DuplicateKeyException: 存在的话被报错！
    //因为用户名和电话设置了唯一属性！
8:明明set了roleId,数据库中也有该字段，可是就是写入失败：
    !!!找到原因了，原来tk mapper中字段不能是int类型，必须是Integer类型才行
9：MySQLSyntaxErrorException: Unknown column 'role_id' in 'field list'：
     不能写成RoleId,因为实际的sql语句会变成role_id。
     要么写成roleid，要么写成roleId同时用@column属性指明数据库的那一列！
10：分页查询失败，查询所有，没有分页：
    PageHelper.startPage(pageNo,pageSize)必须放在service方法中代码块的第一行，而且该代码的下一行必须是需要执行分页操作的select查询方法。
    SpringBoot项目不能引入pagehelper的普通maven依赖；必须引入pagehelper集成spring的起步依赖。

11:mysql 8.x使用高级的加密方式，使得sqlyong连接出问题，需要修改成普通的加密方式：
    ALTER USER 'root'@'localhost' IDENTIFIED BY 'xxxx' PASSWORD EXPIRE NEVER;

    ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'xxxx';

    FLUSH PRIVILEGES;
12：安装好mysql后，可以直接将robot数据库，复制到ubuntu上新的mysql服务器上。

13：关于mysql8.0使用jdbc的注意事项：
    1、jdbc的jar包更换 --mysql-connector-java-8.0.12
    3、驱动地址更改 :com.mysql.cj.jdbc.Driver
    4、建立连接时url的更改: ? characterEncoding=utf8 & useSSL=false & serverTimezone=UTC & rewriteBatchedStatements=true

14:com.mysql.cj.jdbc.Driver cannot resolve Driver ：Maven导包有问题，进入设置dependency，手动选择下载的jar包

15:set-Cookie前端拿到了，却没有设置到application里面去：
    原因：前端和后端的域名需要一致，比如都是localhost或者，一个是potal.leyou.com ,一个是manage.leyou.com，二级域名一致
    这样的话，cookie的domain就是.leyou.com，浏览器发现前端也是，于是才能成功设置。

16： SpringBoot返回JSON日期格式问题 处理方法：
    application.properties/yml文件中修改默认的format格式：
spring.jackson.date-format=yyyy-MM-dd

spring.jackson.time-zone=GMT+8

spring.jackson.serialization.write-dates-as-timestamps=false
    或者：
    @JsonFormat(pattern="yyyy年MM月dd日 HH时mm分ss秒",timezone = "GMT+8")

17：前端中发起PUT（修改）或DELETE（删除）请求，提示405、500错误或保存不成功：
    这两个方法会触发预检请求！OPTIONS

18:v2.0解决了日期格式问题，v3.0解决了设备的所属用户名。v4.0增加了异常捕获。
 */


public class Notice
{
}
