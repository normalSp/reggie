package org.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.DishFlavor;
import org.example.reggie.mapper.DishMapper;
import org.example.reggie.service.DishFlavorService;
import org.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlaverService;

    /**
     * 新增菜品，同时保存对应口味信息
     * 由于需要同时操作两张表
     * @Transactional: 开启事务管理
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        log.info("往dish表中插入信息...");
        super.save(dishDto);

        //由于flavors集合中的dishId没有赋值，所以需要手动赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }

        //保存菜品的口味信息到菜品口味表dish_flavor
        log.info("往dish_flavor表中插入信息...");
        dishFlaverService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品，并且查询出菜品的味道
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = super.getById(id);
        DishDto dishDto = new DishDto();

        //查询出菜品的味道
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlaverService.list(wrapper);

        BeanUtils.copyProperties(dish, dishDto);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品，信息要更新到dish表和dishFlavor表
     * @param dishDto
     */
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品的基本信息到菜品表dish
        this.updateById(dishDto);

        //先查询出菜品的所有口味id
        List<Long> flavorIds = new ArrayList<>();
        List<DishFlavor> flavorsFromDD = dishDto.getFlavors();

        for (DishFlavor flavor : flavorsFromDD) {
            flavorIds.add(flavor.getId());
        }

        List<DishFlavor> flavors = dishFlaverService.listByIds(flavorIds);


        //先删除菜品口味表dish_flavor中的信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlaverService.remove(lambdaQueryWrapper);

        //再新增菜品的口味信息到菜品口味表dish_flavor
        //由于flavors集合中的dishId没有赋值，所以需要手动赋值
        for (DishFlavor flavor : flavorsFromDD) {
            Long flavorId = flavor.getId();
            //从flavors中取出一个id与flavorId相同的
            DishFlavor flavor1 = flavors.stream().filter(f -> f.getId().equals(flavorId)).findFirst().orElse(null);

            log.info("flavor1:{}", flavor1.toString());

            flavor.setCreateTime(flavor1.getCreateTime());
            flavor.setDishId(dishDto.getId());
        }
        dishFlaverService.saveBatch(flavorsFromDD);
    }
}
