package edu.bupt.robot.pojo;

import lombok.Getter;
import lombok.Setter;

//包含user的响应信息
@Setter
@Getter
public class UserResponce extends ResponseResult
{
    private User user;
    public UserResponce()
    {
        super();
    }

    public UserResponce(User user)
    {
        this.user = user;
    }

    @Override
    public String toString()
    {
        return "UserResponce{" + "user=" + user + ", success=" + success + ", message='" + message + '\'' + '}';
    }
}
