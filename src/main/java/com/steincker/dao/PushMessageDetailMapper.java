package com.steincker.dao;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.steincker.entity.PushMessageDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author ST000087
 * @Date 2023-12-29 17:21
 * @Description: 消息分类的数据交互接口
 */
@DS("push")
public interface PushMessageDetailMapper {

    /**
     * @param
     * @return void
     * @author ST000087
     * @date 2024-04-18 19:39
     * @description 新增message
     */
    void addDetailMessage(List<PushMessageDetail> list);


    /**
     * @param parentIdList
     * @return void
     * @author ST000087
     * @date 2024-04-18 20:59
     * @description 查询通过上级id
     */
    List<PushMessageDetail> selectByParentIdList(List<Long> parentIdList);

    /**
     * @param list
     * @return void
     * @author ST000087
     * @date 2024-04-19 13:33
     * @description 批量更新
     */
    void updateBatch(List<PushMessageDetail> list);

    /**
     * @param parentId
     * @return void
     * @author ST000087
     * @date 2024-04-19 14:38
     * @description 父级id
     */
    void deleteByParentId(Long parentId);

    /**
     * @param list
     * @return void
     * @author ST000087
     * @date 2024-04-19 14:49
     * @description 根据父级id 批量更新
     */
    void deleteByBatchParentId(List<Integer> list);



    /**
     * @param list 父级ID
     * @param language 语言
     * @return java.util.List<com.steincker.cloud.push.bean.PushMessageDetail>
     * @author ST000087
     * @date 2024-04-19 17:24
     * @description
     */
    List<PushMessageDetail> selectByParentAndLanguage(@Param("list") List<Long> list,@Param("language") String language);


    int setGroupConcatMaxLen();


    /** 获取所有多语言 【默认不包括逻辑删除】 */
    List<PushMessageDetail> findAllGroupBy();



}
