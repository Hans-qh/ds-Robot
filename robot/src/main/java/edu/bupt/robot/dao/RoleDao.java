package edu.bupt.robot.dao;

import edu.bupt.robot.pojo.Role;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;
@Repository
public interface RoleDao extends Mapper<Role>
{
}
