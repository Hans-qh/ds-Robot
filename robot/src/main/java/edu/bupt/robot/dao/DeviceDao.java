package edu.bupt.robot.dao;

import edu.bupt.robot.pojo.Device;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

@Repository
public interface DeviceDao extends Mapper<Device>
{
    /*
    Incorrect parameter count in the call to native function 'datediff'
    MySQL->DATEDIFF只计算到天,只有两个参数
    TIMESTAMPDIFF(MINUTE,visitor_time,now())<=30 可以指定单位。
     */
    //先查询（查询到了我需要挨个发短信）
    //开启时间>30天 而且 没有发过短信！
    @Select(" select * from device WHERE DATEDIFF(NOW(),DATE)> #{days} AND maintain=0 ;")
    public List<Device> findDevicesNeedSendMsg(Integer days);
    //再修改
    @Update("UPDATE device  SET maintain=1  WHERE DATEDIFF(NOW(),DATE)> #{days} AND maintain=0 ")
    public Integer updateDevicesSetMaintain(Integer days);
}