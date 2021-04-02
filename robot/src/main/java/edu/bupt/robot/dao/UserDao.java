package edu.bupt.robot.dao;
import edu.bupt.robot.pojo.User;
import tk.mybatis.mapper.common.Mapper;
import org.springframework.stereotype.Repository;
@Repository
public interface UserDao extends Mapper<User>
{
}
