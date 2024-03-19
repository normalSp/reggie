package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.entity.SetmealDish;
import org.example.reggie.mapper.SetmealMapper;
import org.example.reggie.service.DishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import org.example.reggie.service.SetmealDishService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐，并保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //1. 保存套餐基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

        //2. 保存套餐和菜品的关联关系，操作setmeal表，执行isnert操作
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        for(SetmealDish setmealDish : setmealDishList){
            setmealDish.setSetmealId(setmealDto.getId());
        }

        log.info("调用saveWithDish方法往setmeal中插入信息...");
        setmealDishService.saveBatch(setmealDishList);
    }

    /**
     * 根据id查询套餐，并查询套餐中的菜品
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> dishes = setmealDishService.list(lambdaQueryWrapper);

        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }

}
