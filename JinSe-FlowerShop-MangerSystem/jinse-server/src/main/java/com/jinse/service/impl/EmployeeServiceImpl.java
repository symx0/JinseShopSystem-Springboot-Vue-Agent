package com.jinse.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jinse.constant.MessageConstant;
import com.jinse.constant.PasswordConstant;
import com.jinse.constant.StatusConstant;
import com.jinse.dto.EmployeeDTO;
import com.jinse.dto.EmployeeLoginDTO;
import com.jinse.dto.EmployeePageQueryDTO;
import com.jinse.entity.Employee;
import com.jinse.exception.AccountLockedException;
import com.jinse.exception.AccountNotFoundException;
import com.jinse.exception.PasswordErrorException;
import com.jinse.mapper.EmployeeMapper;
import com.jinse.result.PageResult;
import com.jinse.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //用md5对密码进行加密
        password=DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
//             System.out.println("加密后的密码："+password);
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        //将DTO的数据拷贝到实体对象中
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置账号状态，默认为正常状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码，默认密码为123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        /**
        //创建时间
        employee.setCreateTime(LocalDateTime.now());
        //更新时间
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录的创建人和更新人
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        */

        //数据已经封装到employee对象，可以调用mapper层的方法插入到数据库中
        employeeMapper.insert(employee);
    }


    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        /*select * from employee limit 0,10
        类似于mybatis的动态SQL，将limit start,start+pageSize动态地拼接到emplyee后面
        这里的0，10是动态的，0是start，10是基于start和pageSize动态地计算出来的*/
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        /*需要将DTO中的搜索员工姓名传到数据库进行模糊查询，因此要把DTO传入pageQuery方法*/
        Page<Employee> page=employeeMapper.pageQuery(employeePageQueryDTO);

        long total=page.getTotal();
        List<Employee> records=page.getResult();
        return new PageResult(total,records);
    }

    /**
     * 开启禁用员工账号(根据员工id)
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, long id) {
        //将目标员工的id和状态值封装到employee对象中,并传入mapper层
        /*因为Employee类有@Bulider 注解，因此可以使用builder()方法修改对象属性*/
        Employee employee=Employee.builder()
                .status(status)
                .id(id) //因为要根据id来修改对应的用户，因此需要将id传到mapper层
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 批量启用禁用员工账号
     * @param status
     * @param ids
     */
    @Transactional
    public void batchStartOrStop(Integer status, List<Long> ids) {
        for (Long id : ids) {
            Employee employee = Employee.builder().status(status).id(id).build();
            employeeMapper.update(employee);
        }
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        return employee;
    }

    /**
     * 修改员工信息
     */
    public void update(Employee employee) {
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }

    public void deleteBatch(List<Long> ids) {
        employeeMapper.deleteByIds(ids);
    }

}
