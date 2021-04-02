package edu.bupt.robot.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.bupt.robot.dao.DeviceDao;
import edu.bupt.robot.pojo.Device;
import edu.bupt.robot.pojo.PageResponce;
import edu.bupt.robot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class DeviceService
{
    @Autowired
    DeviceDao deviceDao;
    @Autowired
    UserService userService;

    //根据编号查询设备
    public Device getDeviceByNum(Long num)
    {
        Device device = new Device();
        device.setNum(num);
        Device device1 = deviceDao.selectOne(device);
        return device1;
    }
    //插入设备
    public Integer addDevice(Device device)
    {
        int count = deviceDao.insert(device); //前面已经检察过num，此时插入不会重复，不会报错！
        return count;
    }

    //更新设备
    public Integer updateDevice(Device device1)
    {
        int count = deviceDao.updateByPrimaryKey(device1);
        return count;
    }
    //删除某台设备，根据设备编号
    public Integer deleteDevice(Device device)
    {
        int count = deviceDao.delete(device);
        return count;
    }
    //删除多台设备
    public void deleteMultiDevice(List<Device> list) //已经确定存在
    {
        list.forEach(device->{deleteDevice(device);});
    }
    //根据用户id查询设备
    public List<Device> getDevicesByUserId(Integer id)
    {
        Device device = new Device();
        device.setUserid(id);
        List<Device> list = deviceDao.select(device);
        return list;
    }
    //根据条件分页查询
    public PageResponce<Device> getDevivcesByPage(Integer pageNum, Integer pageSize, String name, String location, Integer broken,Integer maintain, String username)
    {
        Example example = new Example(Device.class);
        Example.Criteria criteria = example.createCriteria();
        //添加模糊条件
        if(StringUtil.isNotEmpty(name))
        {
            criteria.andLike("name","%"+name+"%");
        }
        if(StringUtil.isNotEmpty(location))
        {
            criteria.andLike("location","%"+location+"%");
        }
        if(broken!=null)
        {
            criteria.andEqualTo("broken",broken);
        }
        if(maintain!=null)
        {
            criteria.andEqualTo("maintain",maintain);
        }
        if(StringUtil.isNotEmpty(username))
        {
            int userid =0 ;
            User user = userService.getUserByUsername(username);
            if(user!=null) userid = user.getId();
            //如有user为空，该用户不存在，那么userid设为0，让他查不出来！
            criteria.andEqualTo("userid",userid);
        }
        //开始分页
        PageHelper.startPage(pageNum,pageSize);
        List<Device> devices = deviceDao.selectByExample(example);
        devices.forEach((device)->{
            //根据userid查询设备所属客户,设置device的所属用户名。
            User userOfDev = userService.getUserById(device.getUserid());
            device.setUsername(userOfDev.getUsername());
        });
        //包装结果集
        PageInfo<Device> pageInfo = new PageInfo<>(devices);
        return new PageResponce<Device>(pageInfo.getList(),pageInfo.getTotal(),pageInfo.getPages());
    }
}
