package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.reggie.common.R;
import org.example.reggie.entity.Category;
import org.example.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类新增
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("保存分类信息：{}", category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分类信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("分页查询分类信息，page:{}, pageSize:{}, name:{}", page, pageSize, name);

        Page page_ = new Page(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(name != null, Category::getName, name);

        queryWrapper.orderByDesc(Category::getSort);

        categoryService.page(page_, queryWrapper);

        return R.success(page_);
    }

    /**
     * 根据id删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类信息，id:{}", ids);

        categoryService.remove(ids);

        return R.success("删除分类信息成功");
    }

    /**
     * 根据id更新分类信息
     * @param category
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("更新分类信息：{}", category);

        categoryService.updateById(category);

        log.info("更新分类信息成功");

        return R.success("更新分类信息成功");
    }

    /**
     * 根据条件查询分类信息
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }
}
