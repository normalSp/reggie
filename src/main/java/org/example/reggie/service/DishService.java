package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，信息要插入到dish表和dishFlavor表
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品，并且查询出菜品的味道
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品，信息要更新到dish表和dishFlavor表
    public void updateWithFlavor(DishDto dishDto);
}
