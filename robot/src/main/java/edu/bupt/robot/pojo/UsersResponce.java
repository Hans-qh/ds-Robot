package edu.bupt.robot.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

//包含users(多个用户)的响应信息
@Setter
@Getter
public class UsersResponce extends ResponseResult
{
    private List<User> users;
    public UsersResponce()
    {
        super();
    }

    public UsersResponce(List<User> users)
    {
        this.users = users;
    }

    @Override
    public String toString()
    {
        return "UsersResponce{" + "users=" + users + ", success=" + success + ", message='" + message + '\'' + '}';
    }
}
