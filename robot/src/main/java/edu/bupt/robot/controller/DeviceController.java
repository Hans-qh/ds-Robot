package edu.bupt.robot.controller;

import edu.bupt.robot.pojo.*;
import edu.bupt.robot.service.DeviceService;
import edu.bupt.robot.service.RoleService;
import edu.bupt.robot.service.UserService;
import edu.bupt.robot.util.MailUtil;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/dev")
@Api(tags="设备管理")
public class DeviceController
{
    @Autowired
    RoleService roleService;
    @Autowired
    DeviceService deviceService;
    @Autowired
    UserService userService;

    //功能1：（管理员）添加add设备device
    @PostMapping("/add")
    @ApiOperation("管理员 添加设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "设备编号"), @ApiImplicitParam(name = "name", value = "设备名称"), @ApiImplicitParam(name = "date", value = "开启时间(不填默认当前时间，格式：yyyy-MM-dd HH:mm:ss)"), @ApiImplicitParam(name = "location", value = "地理位置"), @ApiImplicitParam(name = "username", value = "所属客户"),

    })
    public ResponseEntity<DeviceResponce> addDevive(HttpServletRequest request, @RequestParam Long num, @RequestParam String name, @RequestParam(required = false) String date, @RequestParam String location, @RequestParam String username)
    {
        DeviceResponce deviceResponce = new DeviceResponce();
        //1，不是管理员的登录用户，不让添加
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role == null || (!role.getRolename().equalsIgnoreCase("管理员")))//非管理员或者调试人员，不允许查询所有用户 (双| 和 单&)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("非管理员，不允许添加设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        //2,开始处理数据
        Date date1 = null;
        if (date == null)
        {
            date1 = new Date();
        }
        else
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                date1 = simpleDateFormat.parse(date);
            }
            catch (ParseException e)
            {
                deviceResponce.setSuccess(false);
                deviceResponce.setMessage("日期格式错误！正确格式：yyyy-MM-dd HH:mm:ss");
                return ResponseEntity.ok(deviceResponce);
            }
        }
        Device device = deviceService.getDeviceByNum(num);
        if (device != null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此编号的设备已经存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        User user = userService.getUserByUsername(username);
        if (user == null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此用户不存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        //3，全部参数处理完毕，新增设备
        Device device1 = new Device();
        device1.setNum(num);
        device1.setName(name);
        device1.setBroken(0); //默认正常
        device1.setMaintain(0); //默认不需要保养
        device1.setDate(date1);
        device1.setLocation(location);
        device1.setUserid(user.getId());
        Integer count = deviceService.addDevice(device1);
        if (count > 0)
        {
            deviceResponce.setSuccess(true);
            deviceResponce.setMessage("新增设备成功！");
            deviceResponce.setDevice(device1);
        }
        return ResponseEntity.ok(deviceResponce);
    }

    //功能2：根据设备编号num查询设备device(用于前端回显)
    @GetMapping("/get/{num}")
    @ApiOperation("根据设备编号查询设备(用于前端回显)")
    public ResponseEntity<DeviceResponce> getDeviveByNum(HttpServletRequest request, @PathVariable Long num)
    {
        DeviceResponce deviceResponce = new DeviceResponce();
        Device device = new Device();
        //1，只有管理员才能查询，或者只能查询属于自己的设备
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role != null && (role.getRolename().equalsIgnoreCase("管理员")))
        {
            device = deviceService.getDeviceByNum(num);
            if (device == null)
            {
                deviceResponce.setSuccess(false);
                deviceResponce.setMessage("此编号的设备不存在！");
                return ResponseEntity.ok(deviceResponce);
            }
            //根据userid查询设备所属客户,设置device的所属用户名。
            User userOfDev = userService.getUserById(device.getUserid());
            device.setUsername(userOfDev.getUsername());

            deviceResponce.setSuccess(true);
            deviceResponce.setMessage("查询成功！");
            deviceResponce.setDevice(device);
            return ResponseEntity.ok(deviceResponce);

        }
        else //不是管理员，还要看是否就是登录用户本身
        {
            device = deviceService.getDeviceByNum(num);
            if (device == null)
            {
                deviceResponce.setSuccess(false);
                deviceResponce.setMessage("此编号的设备不存在！");
                return ResponseEntity.ok(deviceResponce);
            }
            User actUser = userService.getUserById(device.getUserid()); //该设备真正的主人
            if (actUser != null && actUser.getId() == loginUser.getId())
            {
                //如果是自己的设备，才允许查询
                device.setUsername(actUser.getUsername());
                deviceResponce.setSuccess(true);
                deviceResponce.setMessage("查询成功！");
                deviceResponce.setDevice(device);
                return ResponseEntity.ok(deviceResponce);
            }
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("不是管理员并且该设备不属于你！");
            return ResponseEntity.ok(deviceResponce);
        }

    }

    //功能3：（管理员）修改设备device
    @PutMapping("/update")
    @ApiOperation("管理员 修改设备（包含故障提交功能）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "设备编号(需要修改哪台设备，必填)"), @ApiImplicitParam(name = "name", value = "设备名称（必填）"), @ApiImplicitParam(name = "date", value = "开启时间(不填默认当前时间，格式：yyyy-MM-dd HH:mm:ss)"), @ApiImplicitParam(name = "location", value = "地理位置（必填）"), @ApiImplicitParam(name = "username", value = "所属客户（必填）"), @ApiImplicitParam(name = "broken", value = "是否故障0/1，（必填）设为1则会提交故障，发送邮件")

    })
    public ResponseEntity<DeviceResponce> updateDevive(HttpServletRequest request, @RequestParam Long num, @RequestParam String name, @RequestParam(required = false) String date, @RequestParam String location, @RequestParam String username, @RequestParam Integer broken)
    {
        DeviceResponce deviceResponce = new DeviceResponce();
        //1，不是管理员的登录用户，不让修改
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role == null || (!role.getRolename().equalsIgnoreCase("管理员")))//非管理员
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("非管理员，不允许修改设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        //2,开始处理数据
        Date date1 = null;
        if (date == null)
        {
            date1 = new Date();
        }
        else
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try
            {
                date1 = simpleDateFormat.parse(date);
            }
            catch (ParseException e)
            {
                deviceResponce.setSuccess(false);
                deviceResponce.setMessage("日期格式错误！正确格式：yyyy-MM-dd HH:mm:ss");
                return ResponseEntity.ok(deviceResponce);
            }
        }
        Device device = deviceService.getDeviceByNum(num);
        if (device == null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此编号的设备尚不存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        //User oldUser = userService.getUserById(device.getUserid()); //没有用
        User user = userService.getUserByUsername(username);
        if (user == null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此用户不存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        if (broken > 0)
        {
            broken = 1;
            //发送邮件 ：给新主人发送（如果没有改变主人，那就是新主人也是原主人）
            MailUtil.Mail mail = new MailUtil.Mail();
            try
            {
                mail.setFrom("hans_qh@163.com");
                mail.setTo(user.getMail());
                mail.setSubject("测试");
                mail.setContent("设备故障提醒！ ");
                MailUtil.sendMsg(user.getMail(), mail);
                System.out.println("发送成功");
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else broken = 0;
        //3，全部参数处理完毕，修改设备
        device.setNum(num);
        device.setName(name);
        device.setBroken(broken);
        device.setMaintain(0); //默认不需要保养，只有发送过提醒，才会值置位1.
        device.setDate(date1);
        device.setLocation(location);
        device.setUserid(user.getId());
        Integer count = deviceService.updateDevice(device);
        if (count > 0)
        {
            deviceResponce.setSuccess(true);
            deviceResponce.setMessage("修改设备成功！");
            deviceResponce.setDevice(device);
        }
        return ResponseEntity.ok(deviceResponce);
    }

    //功能4：根据设备编号num删除设备device
    @DeleteMapping("/del/{num}")
    @ApiOperation("（管理员）根据设备编号删除设备")
    public ResponseEntity<DeviceResponce> deleteDevive(HttpServletRequest request, @PathVariable Long num)
    {
        DeviceResponce deviceResponce = new DeviceResponce();
        Device device = new Device();
        //1，只有管理员才能删除
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role == null || (!role.getRolename().equalsIgnoreCase("管理员")))
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("非管理员不允许删除设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        device = deviceService.getDeviceByNum(num);
        if (device == null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此编号的设备不存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        Integer count = deviceService.deleteDevice(device);
        if (count > 0)
        {
            deviceResponce.setSuccess(true);
            deviceResponce.setMessage("删除成功！");
            deviceResponce.setDevice(device);
        }
        return ResponseEntity.ok(deviceResponce);
    }

    //功能4：根据设备编号num删除多台设备device
    @DeleteMapping("/del/")
    @ApiOperation("（管理员）根据设备编号删除多台设备")
    public ResponseEntity<DeviceResponce> deleteMultiDevive(HttpServletRequest request, @RequestParam Long... num)
    {
        //num 是一个数组，请求格式：curl -X DELETE "http://localhost:10086/dev/del/?num=1&num=2&num=3"
        System.out.println(Arrays.toString(num));
        DeviceResponce deviceResponce = new DeviceResponce();
        //1，只有管理员才能删除
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role == null || (!role.getRolename().equalsIgnoreCase("管理员")))
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("非管理员不允许删除设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        ArrayList<Long> list = new ArrayList<>();
        ArrayList<Device> list2 = new ArrayList<>();
        for (int i = 0; i < num.length; i++)
        {
            Device device = deviceService.getDeviceByNum(num[i]);
            if (device == null)
            {
                list.add(num[i]);
            }
            else
            {
                list2.add(device);
            }
        }
        if (list.size() > 0)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("编号:" + list.toString() + "的设备不存在！");
            return ResponseEntity.ok(deviceResponce);
        }
        deviceService.deleteMultiDevice(list2);
        deviceResponce.setSuccess(true);
        deviceResponce.setMessage("删除成功！");
        return ResponseEntity.ok(deviceResponce);

    }

    //功能5：根据用户名删除该用户的多台设备device
    @DeleteMapping("/delByUser/")
    @ApiOperation("（管理员）根据用户名删除该用户的多台设备")
    public ResponseEntity<DeviceResponce> deleteMultiDeviveByUser(HttpServletRequest request, @RequestParam String username)
    {
        DeviceResponce deviceResponce = new DeviceResponce();
        //1，只有管理员才能删除
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("未登录！");
            return ResponseEntity.ok(deviceResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if (role == null || (!role.getRolename().equalsIgnoreCase("管理员")))
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("非管理员不允许删除设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        User user = userService.getUserByUsername(username);
        if (user == null)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("不存在此用户！");
            return ResponseEntity.ok(deviceResponce);
        }
        List<Device> list2 = deviceService.getDevicesByUserId(user.getId());
        if (list2.size() == 0)
        {
            deviceResponce.setSuccess(false);
            deviceResponce.setMessage("此用户不存在设备！");
            return ResponseEntity.ok(deviceResponce);
        }
        deviceService.deleteMultiDevice(list2);
        deviceResponce.setSuccess(true);
        deviceResponce.setMessage("删除成功！");
        return ResponseEntity.ok(deviceResponce);

    }

    //功能5：根据条件分页查询设备
    @GetMapping("/get/")
    @ApiOperation("根据条件分页查询设备，包括地理位置信息")
    @ApiImplicitParams({@ApiImplicitParam(name="pageNum",value="第几页"),
                        @ApiImplicitParam(name="pageSize",value="每页几条"),
                        @ApiImplicitParam(name="name",value="设备名（模糊查询，可选填）"),
                        @ApiImplicitParam(name="location",value="设备位置（模糊查询，可选填）"),
                        @ApiImplicitParam(name="broken",value="是否损坏：0正常1损坏（可选填）"),
                        @ApiImplicitParam(name="maintain",value="是否需要保修：0不需要1需要（可选填）"),
                        @ApiImplicitParam(name="username",value="用户名（非管理员必须填写自己）"),
    })
    public ResponseEntity<PageResponce<Device>> getDevivcesByPage(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "2") Integer pageSize, @RequestParam(required = false) String name, @RequestParam(required = false) String location, @RequestParam(required = false) Integer broken,@RequestParam(required = false) Integer maintain, @RequestParam(required = false) String username)
    {
        PageResponce<Device> pageResponce = new PageResponce<>();
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            pageResponce.setSuccess(false);
            pageResponce.setMessage("未登录！");
            return ResponseEntity.ok(pageResponce);
        }
        //1,如果不是管理员，而且又查询了其他用户的设备，那是不可以的！
        Role role = roleService.getRoleByUser(loginUser);
        if ((role==null || (!role.getRolename().equalsIgnoreCase("管理员")))
                && !loginUser.getUsername().equalsIgnoreCase(username)  )
        {
            pageResponce.setSuccess(false);
            pageResponce.setMessage("非管理员不能查询其他用户的设备！");
            return ResponseEntity.ok(pageResponce);
        }

        //2：开始分页查询
        PageResponce<Device> pageResponce1 = deviceService.getDevivcesByPage(pageNum,pageSize,name,location,broken,maintain,username);
        pageResponce1.setSuccess(true);
        pageResponce1.setMessage("查询成功！");
        return ResponseEntity.ok(pageResponce1);

    }
    //功能5：手动查询需要保修的设备（管理员查询所有，用户只能查询自己的）（后台有定时任务自动查询，发送邮件）
    @GetMapping("/get/maintain")
    @ApiOperation("手动（后台有定时任务自动查询，发送邮件）查询需要保修的设备 （管理员查询所有，用户只能查询自己的）")
    @ApiImplicitParams({@ApiImplicitParam(name="pageNum",value="第几页"),
                        @ApiImplicitParam(name="pageSize",value="每页几条")
    })
    public ResponseEntity<PageResponce<Device>> getDevivcesNeedMaintain(HttpServletRequest request, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "2") Integer pageSize)
    {
        PageResponce<Device> pageResponce = new PageResponce<>();
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) //未登录
        {
            pageResponce.setSuccess(false);
            pageResponce.setMessage("未登录！");
            return ResponseEntity.ok(pageResponce);
        }
        //1,如果是管理员，就让username为空，查询所有。否则，让username=当前登录用户
        String username = null;
        Role role = roleService.getRoleByUser(loginUser);
        if (role!=null && (role.getRolename().equalsIgnoreCase("管理员")))
        {
            username=null;
        }else{username =loginUser.getUsername(); }

        //2：开始分页查询需要保修的
        PageResponce<Device> pageResponce1 = deviceService.getDevivcesByPage(pageNum,pageSize,null,null,null,1,username);
        pageResponce1.setSuccess(true);
        pageResponce1.setMessage("查询成功！");
        return ResponseEntity.ok(pageResponce1);

    }
}