package com.steincker.entity;

import java.util.Date;

/**
 * @tableName CustomerConfigLanguage
 * @comment 客户服务配置 多语言，主要包括例如质保等信息的多语言信息
 * @author Steincker Mybatis Generator
 */
public class CustomerConfigLanguage {
    /**
     * id
     * id
     */
    private Long id;

    /**
     * 应用
     * app
     */
    private String app;

    /**
     * 类型
     * type
     */
    private String type;

    /**
     * 编码
     * code
     */
    private String code;

    /**
     * 语言（如 zh-CN, en-US）
     * lang
     */
    private String lang;

    /**
     * 文本
     * text
     */
    private String text;

    /**
     * 描述和说明信息
     * description
     */
    private String description;

    /**
     * 删除标识（0：未删除，同 id：删除）
     * deleted
     */
    private Long deleted;

    /**
     * 创建人员 id
     * create_by
     */
    private Long createBy;

    /**
     * 创建时间
     * create_time
     */
    private Date createTime;

    /**
     * 更新人员 id
     * update_by
     */
    private Long updateBy;

    /**
     * 更新时间
     * update_time
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app == null ? null : app.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang == null ? null : lang.trim();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? null : text.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}