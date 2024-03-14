package org.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 根据id删除分类信息
     * 删除前进行判断
     * 是否与其他表进行关联（Dish表、Setmeal表）
     * @param id
     */
    public void remove(Long id);
}
