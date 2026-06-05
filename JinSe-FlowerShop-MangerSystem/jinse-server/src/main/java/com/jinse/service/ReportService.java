package com.jinse.service;


import com.jinse.vo.OrderReportVO;
import com.jinse.vo.SalesTop10ReportVO;
import com.jinse.vo.TurnoverReportVO;
import com.jinse.vo.UserReportVO;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public interface ReportService {

    /**
     * 统计指定时间区间内的营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户量统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end, Integer limit);
}
