package org.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.BaseContext;
import org.example.reggie.common.R;
import org.example.reggie.entity.ShoppingCart;
import org.example.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加到购物车的信息：{}",shoppingCart);

        //1. 设置用户id，指定是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentUserId());

        //2. 查询当前菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        //2.1 判断加入购物车的是菜品还是套餐
        if(null != shoppingCart.getDishId()) {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());

        }else if(null != shoppingCart.getSetmealId()){
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart shoppingCartOne = shoppingCartService.getOne(lambdaQueryWrapper);

        if(null != shoppingCartOne) {
            //3. 如果在，则数据+1
            Integer number = shoppingCartOne.getNumber();
            shoppingCartOne.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCartOne);
        }else {
            //4. 如果不在，则新增一条数据，数量置为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCartOne =  shoppingCart;
        }

        return R.success(shoppingCartOne);
    }

    /**
     * 购物车商品减1
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentUserId());

        if(null != shoppingCart.getDishId()){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }
        else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }


        ShoppingCart shoppingCartOne = shoppingCartService.getOne(lambdaQueryWrapper);

        if(null != shoppingCartOne) {
            Integer number = shoppingCartOne.getNumber();
            if(number > 1) {
                shoppingCartOne.setNumber(number - 1);
                shoppingCartService.updateById(shoppingCartOne);
                return R.success("减少成功");
            }else {
                shoppingCartService.removeById(shoppingCartOne.getId());
                return R.success("删除成功");
            }
        }

        return R.error("更新失败");
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart>  lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentUserId());
        lambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lambdaQueryWrapper);

        return R.success(shoppingCarts);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentUserId());

        shoppingCartService.remove(lambdaQueryWrapper);

        return R.success("清空购物车成功");
    }
}
