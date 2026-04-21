package com.steincker.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ST000087
 * @Date 2023-12-29 17:08
 * @Description:
 */
@Data
public class PushMessageDetail implements Serializable {

    /**
     * id
     **/
    private Long id;

    /**
     * 父级id【关联标题-内容类型的主表(push_message)id】
     **/
    private Long parentId;

    /**
     * 语种【关联维护的语种 若不涉及国际化 则只有一个语种】
     **/
    private String language;

    /**
     * 标题
     **/
    private String title;

    /**
     * 内容
     **/
    private String message;


    /**
     * 是否删除 0 未删除
     **/
    private Long deleted;
    /**
     * app弹窗标题
     */
    private String appTitle;
    /**
     * app弹窗内容
     */
    private String appMessage;
}
