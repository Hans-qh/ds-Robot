package edu.bupt.robot;

import com.sun.org.apache.xpath.internal.SourceTree;
import edu.bupt.robot.dao.DeviceDao;
import edu.bupt.robot.pojo.Device;
import edu.bupt.robot.pojo.Role;
import edu.bupt.robot.pojo.User;
import edu.bupt.robot.service.RoleService;
import edu.bupt.robot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sound.midi.Soundbank;
import java.util.Date;
import java.util.List;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest
class RobotApplicationTests
{

    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    DeviceDao deviceDao;

    @Test
    void testLogin()
    {
        User user = userService.getUserByUsernameAndPassword("zhangsan", "12");
        if (user == null)
        {
            System.out.println("查询失败");
        }
        else
        {
            System.out.println("查询到的用户电话是" + user.getTel());
        }
    }

    @Test //测试插入，存在会怎样
    //结果：DuplicateKeyException: 存在的话被报错！
    //因为用户名和电话设置了唯一属性！
    void testInsert()
    {
        User user = new User();
        user.setUsername("lisi");
        user.setPassword("456");
        user.setTel("13386457856");
        user.setRoleid(1);  //MySQLSyntaxErrorException: Unknown column 'role_id' in 'field list'
       // user.setSum(222); //???表里都没sum
        //!!!找到原因了，原来tk mapper中字段不能是int类型，必须是Integer类型才行
        Integer count = userService.addUser(user);
        System.out.println("===================");
        System.out.println("count== "+count);
    }

    @Test //如果user没有设置roleId
    //roleid需要与数据库保持一致，设置为Integer
    //而且 不能写成RoleId,因为实际的sql语句会变成role_id
    void testroleisNull()
    {
        User user = new User();
        user.setUsername("lisi");
        user.setPassword("456");
        user.setTel("13386457856");
        Role role = roleService.getRoleByUser(user);
        System.out.println("===================");
        System.out.println("role="+role);
    }

    @Test
    //@Transactional 不能加事务！先查询是0，然后修改成1，如果修改成功，那就是 不可重复读！
    void testSelect()
    {
        List<Device> list = deviceDao.findDevicesNeedSendMsg(30);
        System.out.println(list);
        Integer count = deviceDao.updateDevicesSetMaintain(30);
        System.out.println(count);
    }

    //MockMvc 由org.springframework.boot.test包提供，实现了对Http请求的模拟，一般用于我们测试 controller 层。
    @Autowired
    MockMvc mockMvc; //需要@AutoConfigureMockMvc才能注入！



}
