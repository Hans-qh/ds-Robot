package edu.bupt.robot.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.*;
import java.awt.datatransfer.StringSelection;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name="device")
public class Device
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //自增长id
    private Long num; //设备编号 ,唯一
    private String name; //设备名称
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date date;  //开启时间
    private String location; //地理位置
    private Integer broken; //是否故障，1代表是
    private Integer maintain; //是否需要保养、是否已经发送提醒：0：不需要保养，没有发送过；1：已经发送过
    //属于哪个用户，是一个外键
    private Integer userid; //不能写int!!!
    @Transient //不保存到数据库中，仅仅前端显示；
    private String username;

}
