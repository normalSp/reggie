package org.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.example.reggie.entity.Orders;
import org.example.reggie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单-->去支付
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单信息：{}", orders);

        orderService.submit(orders);

        return null;
    }

    /**
     * 订单列表
     * @param page
     * @param pageSize
     * @return
     */
    @Transactional
    @GetMapping("/userPage")
    public R<Page<Orders>> list(int page, int pageSize){
        Page<Orders> page_ = new Page<>(page, pageSize);

        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        lambdaQueryWrapper.eq(Orders::getUserId, BaseContext.getCurrentUserId());

        orderService.page(page_, lambdaQueryWrapper);
        return R.success(page_);
    }
}
