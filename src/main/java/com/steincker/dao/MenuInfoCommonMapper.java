package com.steincker.dao;

import com.steincker.entity.MenuInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author : [dengbo]
 * @className : MenuInfoCommonMapper
 * @description : [菜单]
 * @createTime : [2023/11/30 19:27]
 */
public interface MenuInfoCommonMapper {
    /**
     * 通过id 查询菜单
     * @param list
     * @return
     */
    List<MenuInfo> findByIds(@Param("list") Collection<Integer> list);

    /**
     * 查询所有菜单
     * @return
     */
    List<MenuInfo> findAll();

    /**
     * 搜索所有菜单
     * @return
     */
    List<MenuInfo> searchAll(@Param("name") String name,@Param("type") Integer type,@Param("visible") Boolean visible);

    /**
     * 通过id 和 类型 查询菜单
     * @param list
     * @param types
     * @return
     */
    List<MenuInfo> findByIdsAndTypes(@Param("list") Collection<Integer> list,@Param("types") List<Integer> types);
    /**
     * 通过id 和 类型 查询菜单
     * @param parentId
     * @param types
     * @return
     */
    List<MenuInfo> findByParentId(@Param("parentId") Integer parentId,@Param("types") List<Integer> types);

    /**
     * 通过菜单ID 删除
     * @param menuIds
     * @return
     */
    int deleteMenuByIds(@Param("menuIds") Collection<Integer> menuIds);

    /**
     * 查询下级孩子
     * @param menuId
     * @return
     */
    List<MenuInfo> findSubChildren(@Param("menuId") Integer menuId);

    /**
     * 查询下级孩子数量
     * @param menuId
     * @return
     */
    int checkSubChildrenExist(@Param("menuId") Integer menuId);
}
