package com.jinse.service.impl;


import com.jinse.dto.GoodsSalesDTO;
import com.jinse.entity.Orders;
import com.jinse.mapper.OrderMapper;
import com.jinse.mapper.UserMapper;
import com.jinse.service.ReportService;
import com.jinse.vo.OrderReportVO;
import com.jinse.vo.SalesTop10ReportVO;
import com.jinse.vo.TurnoverReportVO;
import com.jinse.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
//import org.apache.maven.surefire.shade.org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内的营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){ //如果begin自增到end，则退出循环
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList=new ArrayList<>();

        List<Integer> validStatusList = new ArrayList<>(Arrays.asList(Orders.RECEIPT_CONFIRMED, Orders.COMPLETED));

        for(LocalDate date:dateList){
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);

            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("statusList", validStatusList);

            Double turnover = orderMapper.sumByMap(map);
            turnover=turnover==null ? 0.0 : turnover;

            turnoverList.add(turnover);
        }

        String dateListStr= StringUtils.join(dateList,","); //将日期列表构造成字符串，日期之间用逗号隔开
        String turnoverListStr=StringUtils.join(turnoverList,","); //将营业额列表构造成字符串，营业额之间用逗号隔开

        //将查询到的营业额数据封装到VO对象并返回
        return TurnoverReportVO
                .builder()
                .dateList(dateListStr)//将日期列表字符串封装到TurnoverReportVO对象中
                .turnoverList(turnoverListStr) //将营业额列表字符串封装到TurnoverReportVO对象中
                .build();
    }

    /**
     * 统计指定时间区间内的用户数量
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //创建存放每日用户量的列表
        List<Integer> newUserList=new ArrayList<>();//select count(*) from user where create_time between begin and end
        List<Integer> totalUserList=new ArrayList<>(); //select count(*) from user where create_time < end

        for(LocalDate date:dateList){
            //设置该日的起始时间与结束时间
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN); //00:00:00
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX); //23:59:59

            Map map=new HashMap();
            map.put("end",endTime); //先传end到map里，用于计算总用户数 (总用户数只需end来形成时间区间)
            //统计总用户数
            Integer totalUser=userMapper.countByMap(map);

            map.put("begin",beginTime); //接着再传begin到map里，用于计算新增用户数 (新增用户数需要begin/end来形成时间区间)
            //统计新增用户数
            Integer newUser=userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        String dateListStr=StringUtils.join(dateList,",");
        String newUserListStr=StringUtils.join(newUserList,",");
        String totalUserListStr=StringUtils.join(totalUserList,",");

        return UserReportVO.builder()
                .dateList(dateListStr)
                .newUserList(newUserListStr)
                .totalUserList(totalUserListStr)
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList=new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();

        List<Integer> validStatusList = new ArrayList<>(Arrays.asList(Orders.RECEIPT_CONFIRMED, Orders.COMPLETED));

        for(LocalDate date:dateList){
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount=getOrderCount(beginTime,endTime,null);
            Integer validOrderCount=getOrderCountByStatusList(beginTime,endTime,validStatusList);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算时间区间内的订单总数
        Integer totalOrderCount=orderCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单总数
        Integer validOrderCount=validOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate=0.0;
        if(totalOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 统计指定时间区间内的销量排名
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end, Integer limit) {
        Map map = new HashMap();
        if (begin != null) {
            map.put("begin", begin);
        }
        if (end != null) {
            map.put("end", end);
        }
        map.put("limit", (limit != null && limit > 0) ? limit : 9999);
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop10(map);
        List<String> nameList=salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameListStr=StringUtils.join(nameList,",");
        List<Integer> numberList=salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberListStr=StringUtils.join(numberList,",");

        return SalesTop10ReportVO.builder()
                .nameList(nameListStr)
                .numberList(numberListStr)
                .build();
    }

    /**
     * 统计指定时间区间内订单总数
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }

    private Integer getOrderCountByStatusList(LocalDateTime begin,LocalDateTime end,List<Integer> statusList){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("statusList",statusList);
        return orderMapper.countByMap(map);
    }

}
