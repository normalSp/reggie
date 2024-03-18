package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("传入的菜品信息 --> dishDto:{}", dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 分页查询菜品信息
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("传入的分页信息 --> page:{}, pageSize:{}, name:{}", page, pageSize, name);

        Page<Dish> page_ = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page();

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.like(StringUtils.isNotBlank(name), Dish::getName, name);

        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(page_, lambdaQueryWrapper);

        //将page_信息复制到dishDtoPage中，不复制recordes，即只复制page, pageSize, name
        BeanUtils.copyProperties(page_, dishDtoPage,"records");

        //先从dish的page_中获取categoryId，再在category中查询categoryName置入dishDto的records
        List<Dish> records = page_.getRecords();
        List<DishDto> list = new ArrayList<>();

        for (Dish dish : records) {
            DishDto dishDto = new DishDto();
            //将dish中的record，即菜品名称、售价之类信息拷贝进dishDto
            BeanUtils.copyProperties(dish, dishDto);
            Category category = categoryService.getById(dish.getCategoryId());
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            list.add(dishDto);
        }

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);


    }

    @GetMapping("/{id}")
    public R<DishDto>  getById(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 更新菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("调用更新菜品方法，传入的菜品信息 --> dishDto:{}", dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("更新菜品成功");
    }

    /**
     * 单个删除 & 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long[] ids){
        log.info("调用删除菜品方法，传入的菜品id --> ids:{}", ids);

        dishService.removeByIds(Arrays.asList(ids));

        return R.success("删除菜品成功");
    }


    /**
     * 单个停售 & 批量停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> discontinued(Long[] ids){
        LambdaQueryWrapper<Dish>  lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids);

        List<Dish> dishes = dishService.list(lambdaQueryWrapper);

        for (Dish dish : dishes) {
            dish.setStatus(0);
            dishService.updateById(dish);
        }

        return R.success("停售成功");
    }

    /**
     * 单个起售 & 批量起售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> activation(Long[] ids){
        LambdaQueryWrapper<Dish>  lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids);

        List<Dish> dishes = dishService.list(lambdaQueryWrapper);

        for (Dish dish : dishes) {
            dish.setStatus(1);
            dishService.updateById(dish);
        }

        return R.success("启售成功");
    }
}
