package edu.bupt.robot.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import edu.bupt.robot.dao.UserDao;
import edu.bupt.robot.pojo.PageResponce;
import edu.bupt.robot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService
{
    @Autowired
    UserDao userDao;

    public  User getUserByUsernameAndPassword(String username, String password)
    {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        List<User> userList = userDao.select(user);
        return userList.size()==0? null: userList.get(0);
    }

    public User getUserById(int id)
    {
        User user = userDao.selectByPrimaryKey(id);
        return user;
    }


    public Integer addUser(User user)
    {
        try
        {
            int count = userDao.insert(user);
            return count;
        }
        catch (DuplicateKeyException e)
        {
            //e.printStackTrace();
        }
        return null; //重复插入会报错，设置结果为null
    }

    public PageResponce<User> getUsersByPage(Integer pageNum, Integer pageSize)
    {
        //分页第一步，startPage
        PageHelper.startPage(pageNum,pageSize);
        //分页第二步，查询
        List<User> users = userDao.selectAll();
        //第三步，查询结果封装成pageInfo对象
        PageInfo<User> pageInfo = new PageInfo<>(users);
        //第四步，从pageInfo对象里面获取更多数据，比如total
        PageResponce<User> pageResponce = new PageResponce<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPages());

        return pageResponce;
    }

    public User getUserByUsername(String username)
    {
        User user = new User();
        user.setUsername(username);
        User user1 = userDao.selectOne(user);
        return user1;

    }
}
