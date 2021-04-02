package edu.bupt.robot.config;

import edu.bupt.robot.dao.DeviceDao;
import edu.bupt.robot.pojo.Device;
import edu.bupt.robot.pojo.User;
import edu.bupt.robot.service.UserService;
import edu.bupt.robot.util.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class StaticScheduleTask {

    @Autowired
    DeviceDao deviceDao;
    @Autowired
    UserService userService;

    //3.添加定时任务
    @Scheduled(cron = "0/15 * * * * ?")
    //或直接指定时间间隔，例如：15秒
    //@Scheduled(fixedRate=15000)
    private void configureTasks() {
        //System.err.println("执行静态定时任务时间: " + LocalDateTime.now());

        try //添加异常处理
        {
            //1,查询超过30天且 没有发过短信的设备
            List<Device> list = deviceDao.findDevicesNeedSendMsg(30);
            //2,给所属用户发短信
            if(list.size()>0)
            {
                list.forEach(device -> {
                    Integer userid = device.getUserid();
                    User user = userService.getUserById(userid);
                    //发短信/邮件
                    MailUtil.Mail mail = new MailUtil.Mail();
                    try
                    {
                        mail.setFrom("hans_qh@163.com");
                        mail.setTo(user.getMail());
                        mail.setSubject("保修提醒");
                        mail.setContent("设备保修提醒！ ");
                        MailUtil.sendMsg("2253559277@qq.com", mail);
                        System.out.println("发送成功");
                    }
                    catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                });

            }
            //3,更新maintain=1 ，表示已经发过。
            deviceDao.updateDevicesSetMaintain(30);
        }
        catch (Exception e)
        {
            System.out.println("===============数据库连接失败，请检查数据库是否开启，连接地址是否正确！=================");
            System.out.println("===============数据库连接失败，请检查数据库是否开启，连接地址是否正确! =================");
            System.out.println("===============数据库连接失败，请检查数据库是否开启，连接地址是否正确！=================");
            e.printStackTrace();
        }

    }
}
