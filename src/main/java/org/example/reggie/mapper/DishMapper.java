package org.example.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.reggie.entity.Dish;

import javax.annotation.ManagedBean;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
