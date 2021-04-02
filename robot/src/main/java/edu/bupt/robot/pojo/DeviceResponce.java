package edu.bupt.robot.pojo;

import lombok.Getter;
import lombok.Setter;

//包含device的响应信息
@Setter
@Getter
public class DeviceResponce extends ResponseResult
{
    private Device device;
    public DeviceResponce()
    {
        super();
    }

    public DeviceResponce(Device device)
    {
        this.device = device;
    }

    @Override
    public String toString()
    {
        return "DeviceResponce{" + "user=" + device + ", success=" + success + ", message='" + message + '\'' + '}';
    }
}
