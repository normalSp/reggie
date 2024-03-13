package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.entity.Employee;
import org.example.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.apache.commons.codec.digest.DigestUtils;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5Hex(password);

        //2.根据页面提交的username查询数据库
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee employee1 = employeeService.getOne(lambdaQueryWrapper);

        //3.查询是否存在此用户名
        if(employee1 == null) {
            return R.error("无此用户名，登录失败");
        }

        //4.比对密码
        if(!employee1.getPassword().equals(password)){
            return R.error("密码不正确，登录失败");
        }

        //5.查看员工状态，若禁用则返回员工禁用结果
        if(employee1.getStatus() == 0){
            return R.error("员工已禁用，登录失败");
        }

        //6.登录成功，将员工信息返回给前端
        request.getSession().setAttribute("employee",employee1.getId());
        return R.success(employee1);

    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}", employee.toString());

        //初始信息设置
        //初始密码设置123456，md5加密
        employee.setPassword(DigestUtils.md5Hex("123456"));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户id
        Long employeeId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(employeeId);
        employee.setUpdateUser(employeeId);

        employeeService.save(employee);
        log.info("新增员工成功");
        return R.success("新增员工成功");
    }
}
