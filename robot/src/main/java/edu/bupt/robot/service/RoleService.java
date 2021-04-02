package edu.bupt.robot.service;

import edu.bupt.robot.dao.RoleDao;
import edu.bupt.robot.pojo.Role;
import edu.bupt.robot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService
{
    @Autowired
    RoleDao roleDao;

    public Role getRoleByName(String roleName)
    {
        Role role  = new Role();
        role.setRolename(roleName);
        Role role1 = roleDao.selectOne(role);
        return role1;
    }

    public Role getRoleById(int id)
    {
        Role role = roleDao.selectByPrimaryKey(id);
        return role;
    }

    public Role getRoleByUser(User user)
    {//如果user的roleId没有设置，则数据库默认为NULL，
        //为了与实体对应起来，实体的属性应该使用Integer！
        if(user.getRoleid()==null) return null;
        return this.getRoleById(user.getRoleid());
    }
}
