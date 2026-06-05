package com.jinse.service;

import com.jinse.dto.EmployeeDTO;
import com.jinse.dto.EmployeeLoginDTO;
import com.jinse.dto.EmployeePageQueryDTO;
import com.jinse.entity.Employee;
import com.jinse.result.PageResult;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);


    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用禁用员工账号(根据员工id)
     * @param status
     * @param id
     */
    void startOrStop(Integer status, long id);

    /**
     * 批量启用禁用员工账号
     * @param status
     * @param ids
     */
    void batchStartOrStop(Integer status, List<Long> ids);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    Employee getById(Long id);

    /**
     * 修改员工信息
     */
    void update(Employee employee);

    /**
     * 批量删除员工
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
