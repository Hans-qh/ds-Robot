package edu.bupt.robot.advice;

import edu.bupt.robot.pojo.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ResponseBody
@ControllerAdvice
public class MyExceptionHandler
{
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult> handleException(Exception e)
    {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setSuccess(false);

        if(e instanceof MissingServletRequestParameterException) //参数没有填对
        {
            responseResult.setMessage("请求参数缺失："+e.getMessage());
            return ResponseEntity.badRequest().body(responseResult);
        }
        responseResult.setMessage("服务器内部错误："+e.getMessage());
        return ResponseEntity.status(500).body(responseResult); //服务器内部问题
    }

}
