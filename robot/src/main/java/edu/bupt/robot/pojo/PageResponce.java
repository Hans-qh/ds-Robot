package edu.bupt.robot.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

//分页查询的响应信息（查询的可能是用户，或者设备，用泛型表示）
@Setter
@Getter
@ToString
public class PageResponce<T> extends ResponseResult
{
    private List<T> items;
    private Long total; //总条数
    private Integer totalPage; //总页数

    public PageResponce()
    {
        super();
    }
    public PageResponce(List<T> items)
    {
        this.items = items ;
    }

    public PageResponce(List<T> items, Long total)
    {
        this.items = items;
        this.total = total;
    }

    public PageResponce(List<T> items, Long total,int totalPage)
    {
        this.items = items;
        this.total = total;
        this.totalPage = totalPage;
    }
}
