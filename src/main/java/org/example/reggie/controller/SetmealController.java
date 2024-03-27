package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.CustomException;
import org.example.reggie.common.R;
import org.example.reggie.dto.SetmealDto;
import org.example.reggie.entity.Category;
import org.example.reggie.entity.Dish;
import org.example.reggie.entity.Setmeal;
import org.example.reggie.entity.SetmealDish;
import org.example.reggie.service.CategoryService;
import org.example.reggie.service.SetmealDishService;
import org.example.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * 套餐管理
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜单
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("新增套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("分页查询套餐信息，page:{},pageSize:{},name:{}",page,pageSize,name);

        Page<Setmeal> page_ = new Page<>(page,pageSize);
        Page<SetmealDto>  setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        lambdaQueryWrapper.like(name!=null,Setmeal::getName,name);

        setmealService.page(page_,lambdaQueryWrapper);

        BeanUtils.copyProperties(page_, setmealDtoPage, "records");

        List<Setmeal> records = page_.getRecords();
        List<SetmealDto> setmealDtos = new ArrayList<>();

        for(Setmeal setmeal:records){
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);

            Category category = categoryService.getById(setmeal.getCategoryId());
            setmealDto.setCategoryName(category.getName());

            setmealDtos.add(setmealDto);
        }

        setmealDtoPage.setRecords(setmealDtos);
        return R.success(setmealDtoPage);
    }

    /**
     * 单品删除 & 批量删除
     * @param ids
     * @return
     */
    @Transactional
    @DeleteMapping
    public R<String> deletes(Long[] ids){
        log.info("调用删除套餐方法，传入id：{}",ids);

        for (Long id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            if (0 == setmeal.getStatus()) {
                setmealService.removeById(id);
                LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
                setmealDishService.remove(lambdaQueryWrapper);
            } else {
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
    public R<String> disables(Long[] ids){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> setmeals = setmealService.list(lambdaQueryWrapper);

        for(Setmeal setmeal:setmeals){
            setmeal.setStatus(0);
            setmealService.updateById(setmeal);
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
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> setmeals = setmealService.list(lambdaQueryWrapper);

        for(Setmeal setmeal:setmeals){
            setmeal.setStatus(1);
            setmealService.updateById(setmeal);
        }

        return R.success("起售成功");
    }


    /**
     * 修改套餐界面展示原信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改套餐信息：{}",setmealDto);

        setmealService.updateWithDish(setmealDto);

        return R.success("修改套餐成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(@RequestParam Map<String,String> map){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(map.get("categoryId") != null, Setmeal::getCategoryId, map.get("categoryId"));
        lambdaQueryWrapper.eq(map.get("status") != null, Setmeal::getStatus, map.get("status"));
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(lambdaQueryWrapper);
        return R.success(setmealList);
    }


}
