package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.CustomException;
import org.example.reggie.entity.*;
import org.example.reggie.mapper.OrderMapper;
import org.example.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单-->去支付
     * @param orders
     * @return
     */
    @Transactional
    @Override
    public void submit(Orders orders) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentUserId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper  = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);

        if(shoppingCarts.isEmpty()){
            throw new CustomException("购物车为空");
        }

        User user = userService.getById(userId);
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        if(null == addressBook){
            throw new CustomException("收货地址不存在");
        }

        long orderId = IdWorker.getId();

        //总金额 AtomicInteger为进程安全的整形
        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart item : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            orderDetails.add(orderDetail);
        }

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向订单表插入（一条）数据
        this.save(orders);

        //向订单明细表插入（数条）数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(lambdaQueryWrapper);
    }
}
