package edu.bupt.robot.controller;

import edu.bupt.robot.dao.UserDao;
import edu.bupt.robot.pojo.*;
import edu.bupt.robot.service.RoleService;
import edu.bupt.robot.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@Api(tags="用户管理")
@RequestMapping("/user")
public class UserController
{
    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    //功能1：登录验证
    @GetMapping("/login")
    @ApiOperation("登录验证")
    @ApiImplicitParams({@ApiImplicitParam(name="username",value="用户名"),
                        @ApiImplicitParam(name="password",value="密码")})
    public ResponseEntity<User>  getUserByUsernameAndPassword(HttpServletRequest request,
                                                              @RequestParam String username,
                                                              @RequestParam String password )
    {
        /* //加了controllerAdvice之后，如果参数缺少，直接被捕获，走不到这里来
        if(StringUtil.isEmpty(username) || StringUtil.isEmpty(password))
        {
            return ResponseEntity.badRequest().build();
        }*/
        User user = userService.getUserByUsernameAndPassword(username, password);
        if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        HttpSession session = request.getSession();
        session.setAttribute("user",user); //利用session把user存起来，达到登录的目的
        return ResponseEntity.ok(user);
    }

    //功能2：退出登录
    @GetMapping("/logout")
    @ApiOperation("退出登录")
    public ResponseEntity<User>  userLogout(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");//利用session把user存起来，达到登录的目的
        if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //没有登录怎么退出
        User user1 = (User)user;
        session.invalidate(); //设置session失效
        return ResponseEntity.ok(user1);
    }

    //功能3：（管理员）添加add用户
    @PostMapping("/add")
    @ApiOperation("管理员添加用户")
    @ApiImplicitParams({@ApiImplicitParam(name="username",value="用户名"),
                        @ApiImplicitParam(name="password",value="密码"),
                        @ApiImplicitParam(name="tel",value="电话"),
                        @ApiImplicitParam(name="mail",value="邮箱"),
                        @ApiImplicitParam(name="rolename",value="角色名称"),
    })
    public ResponseEntity<UserResponce>  addUser(HttpServletRequest request,
                                                 @RequestParam String username,
                                                 @RequestParam String password,
                                                 @RequestParam String tel,
                                                 @RequestParam String mail,
                                                 @RequestParam String rolename)
    {
        UserResponce userResponce = new UserResponce();
        //1，不是管理员的登录用户，不让添加
        User loginUser = (User)request.getSession().getAttribute("user");
        if(loginUser==null) //未登录
        {
            userResponce.setSuccess(false);
            userResponce.setMessage("未登录！");
            return ResponseEntity.ok(userResponce);
        }
        if(!isUserAdministrator(loginUser)) //非管理员，不允许添加
        {
            userResponce.setSuccess(false);
            userResponce.setMessage("非管理员，不允许添加！");
            return ResponseEntity.ok(userResponce);
        }


        //2，根据rolename查询roleId,因为User需要的是roleId
        Role role = roleService.getRoleByName(rolename);
        int roleId = role.getId();

        //3，添加用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setTel(tel);
        user.setMail(mail);
        user.setRoleid(roleId);
        Integer count = userService.addUser(user);
        if(count==null) //用户名或者电话或者邮箱重复！
        {
            userResponce.setSuccess(false);
            userResponce.setMessage("用户名或者电话或者邮箱重复！");
            return ResponseEntity.ok(userResponce);
        }
        if(count>0)
        {
            userResponce.setUser(user);
            userResponce.setSuccess(true);
            userResponce.setMessage("添加成功！");
        }
        return ResponseEntity.ok(userResponce);
    }

    //功能4：（管理员,调试人员）查询所有用户
    @GetMapping("/get")
    @ApiOperation("（管理员,调试人员）查询所有用户")
    @ApiImplicitParams({@ApiImplicitParam(name="pageNum",value="第几页"),
                        @ApiImplicitParam(name="pageSize",value="每页显示几条")})
    public ResponseEntity<PageResponce<User>>  getAllUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum, //第几页
            @RequestParam(defaultValue = "2") Integer pageSize) //每页显示几条
    {
        PageResponce<User> pageResponce = new PageResponce<User>();
        //1，不是管理员和调试人员的登录用户，不让查询
        User loginUser = (User)request.getSession().getAttribute("user");
        if(loginUser==null) //未登录
        {
            pageResponce.setSuccess(false);
            pageResponce.setMessage("未登录！");
            return ResponseEntity.ok(pageResponce);
        }
        Role role = roleService.getRoleByUser(loginUser);
        if(role==null ||
                (!role.getRolename().equalsIgnoreCase("管理员")
                & !role.getRolename().equalsIgnoreCase("调试人员"))
                )//非管理员或者调试人员，不允许查询所有用户 (双| 和 单&)
        {
            pageResponce.setSuccess(false);
            pageResponce.setMessage("非管理员或者调试人员，不允许查询所有用户");
            return ResponseEntity.ok(pageResponce);
        }

        //2，查询所有用户
        PageResponce<User> pageResponce1 = userService.getUsersByPage(pageNum, pageSize);
        pageResponce1.setSuccess(true);
        pageResponce1.setMessage("查询成功！");
        return ResponseEntity.ok(pageResponce1);
    }

    /*
    这两个功能有点类似，其实可以直接getRoleByUser就行
     */
    //判断用户是否是管理员身份
    private boolean isUserAdministrator(User user)
    {
        Role role = roleService.getRoleByUser(user);
        if(role==null) return false; //该user 没有设置身份角色
        return role.getRolename().equalsIgnoreCase("管理员");
    }
    //判断用户是否是调试人员身份
    private boolean isUserDebugger(User user)
    {
        Role role = roleService.getRoleByUser(user);
        if(role==null) return false;
        return role.getRolename().equalsIgnoreCase("调试人员");
    }

}
