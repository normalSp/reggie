package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.CustomException;
import org.example.reggie.common.R;
import org.example.reggie.dto.DishDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.DishFlavor;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.DishFlavorService;
import org.example.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;

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

    /**
     * 修改套餐界面展示原信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
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

        //如果菜品停售才删除，否则返回R.error
        for(int i = 0; i < ids.length; i++){
            Dish dish = dishService.getById(ids[i]);
            if(0 == dish.getStatus()){
                dishService.removeById(ids[i]);
            }
            else{
                throw new CustomException("删除失败，请先停售");
            }
        }

        return R.success("删除成功");
    }


    /**
     * 单个停售 & 批量停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> discontinued(Long[] ids){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
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

    /**
     * 根据传入id返回dishDto集合
     * @return
     */
    @Transactional
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //菜品起售才显示
        lambdaQueryWrapper.eq(Dish::getStatus, 1);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        //查出dishes
        List<Dish> dishes = dishService.list(lambdaQueryWrapper);
        List<DishDto> dishDtos = new ArrayList<>();

        //将dishes复制进dishDto中
        for (Dish dish1 : dishes) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish1, dishDto);
            dishDtos.add(dishDto);
        }

        for (DishDto dishDto : dishDtos) {
            Category category = categoryService.getById(dishDto.getCategoryId());
            dishDto.setCategoryName(category.getName());

            //查出dishFlavors
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId, dishDto.getId());
            List<DishFlavor>  dishFlavors = dishFlavorService.list(lambdaQueryWrapper1);

            //将dishFlavors复制进dishDto
            dishDto.setFlavors(dishFlavors);

        }


        return R.success(dishDtos);

    }
}
