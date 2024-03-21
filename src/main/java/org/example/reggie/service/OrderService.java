package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.vavr.collection.Tree;
import org.example.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    //用户下单-->去支付
    public void submit(Orders orders);
}
