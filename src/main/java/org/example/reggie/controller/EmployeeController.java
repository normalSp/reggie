package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.entity.Employee;
import org.example.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import org.apache.commons.codec.digest.DigestUtils;
import javax.servlet.http.HttpServletRequest;

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

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户id
        //Long employeeId = (Long) request.getSession().getAttribute("employee");

        //employee.setCreateUser(employeeId);
        //employee.setUpdateUser(employeeId);

        employeeService.save(employee);
        log.info("新增员工成功");
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * Page是mp封装的类
     * 前端用到的是Page这个类
     * 而不是employee
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("分页查询员工，当前页：{}，每页条数：{}，员工姓名：{}", page, pageSize, name);

        //构造分页构造器
        Page page_ = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //只在name不为空的情况下查询
        queryWrapper.like(StringUtils.isNotBlank(name), Employee::getName, name);

        //排序条件 根据UpdateTime降序排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(page_, queryWrapper);

        return R.success(page_);
    }

    /**
     * 根据员工id进行更新
     * 前端传回的employee对象已经将status设置
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info("更新员工，员工信息：{}", employee.toString());
        log.info("EmployeeController-update 线程id为：{}", Thread.currentThread().getId());

        //Long  employeeId = (Long) request.getSession().getAttribute("employee");

        //employee.setUpdateUser(employeeId);
        //employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);

        return R.success("员工修改更新成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable() Long id){
        log.info("根据id查询员工，id：{}", id);
        Employee employee = employeeService.getById(id);
        if(null != employee) {
            return R.success(employee);
        }
        else return R.error("没有查询到对应员工信息");
    }
}
