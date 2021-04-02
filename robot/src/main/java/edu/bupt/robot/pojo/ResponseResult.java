package edu.bupt.robot.pojo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
//响应结果，包括：是否成功以及 提示信息
//所有响应结果应该继承这个类
public class ResponseResult
{
    protected boolean success;
    protected String message;
}
