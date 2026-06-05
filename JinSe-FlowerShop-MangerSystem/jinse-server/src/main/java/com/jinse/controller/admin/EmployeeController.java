package com.jinse.controller.admin;

import com.jinse.constant.JwtClaimsConstant;
import com.jinse.dto.EmployeeDTO;
import com.jinse.dto.EmployeeLoginDTO;
import com.jinse.dto.EmployeePageQueryDTO;
import com.jinse.entity.Employee;
import com.jinse.properties.JwtProperties;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.EmployeeService;
import com.jinse.utils.JwtUtil;
import com.jinse.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags="员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工登出")
    public Result<String> logout() {
        log.info("员工登出");
        return Result.success("下班愉快");
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping("/save")
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        //返回添加成功（因为是插入操作，不需要返回给前端数据，所以success方法里不需要填参数）
        return Result.success("新增成功");
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询,参数为：{}",employeePageQueryDTO);
        //将查询到的结果封装到pageResult对象内
        PageResult pageResult=employeeService.pageQuery(employeePageQueryDTO);
        //将查询结果PageResult进一步封装到Result.data里，返回给前端
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status,long id){
        log.info("启用禁用员工账号,参数为：{},{}",status,id);
        employeeService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 批量启用禁用员工账号
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/batch/{status}")
    @ApiOperation("批量启用禁用员工账号")
    public Result batchStartOrStop(@PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("批量启用禁用员工账号, status={}, ids={}", status, ids);
        employeeService.batchStartOrStop(status, ids);
        return Result.success("操作成功");
    }


    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee=new Employee();
        employee=employeeService.getById(id);
        employee.setPassword("****");
        return Result.success(employee);
    }

    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息：{}",employeeDTO);
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employeeService.update(employee);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除员工")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除员工：{}", ids);
        employeeService.deleteBatch(ids);
        return Result.success("删除成功");
    }
}
