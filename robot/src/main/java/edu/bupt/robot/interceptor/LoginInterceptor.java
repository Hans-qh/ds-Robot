package edu.bupt.robot.interceptor;

import edu.bupt.robot.pojo.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
登录验证拦截器(放弃，在每个controller里面验证算了，为了返回数据的格式)

拦截器的使用方法：
    1：写一个拦截器，实现HandlerInterceptor接口的三个方法，或者复写HandlerInterceptorAdapter需要用到的某个方法。
    2：@Component将拦截器放到容器中。
    3：写一个配置类，实现WebMvcConfigurer接口，复写addInterceptors方法：registry.addInterceptor（new MyInterceptor）
    4: @Configuration 标明这是一个配置类。
 */
//@Component
public class LoginInterceptor extends HandlerInterceptorAdapter
{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        System.out.println("执行了LoginInterceptor的preHandle方法");
        try
        {
            //统一拦截（查询当前session是否存在user）(这里user会在每次登陆成功后，写入session)
            User loginUser = (User) request.getSession().getAttribute("user");
            if (loginUser != null)
            {
                return true; //放行
            }
            //重定向到登录页 (登录页是前端写的，应该定向到前端的登录界面)
            //response.sendRedirect(request.getContextPath()+"/user/login");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;//如果设置为false时，被请求时，拦截器执行到此处将不会继续操作
        //如果设置为true时，请求将会继续执行后面的操作
    }
}

