package com.steincker.entity;

import java.util.Date;

/**
 * @tableName menu_info
 * @comment 菜单表
 * @author Steincker Mybatis Generator
 */
public class MenuInfo extends MenuInfoKey {
    /**
     * 上级id
     * parent_id
     */
    private Integer parentId;

    /**
     * 名称
     * name
     */
    private String name;

    /**
     * type  类型 1：目录(D)、2：菜单(M)、3：页面(P)、4：按钮(B)、5：URL(U)、6：标签(L)
     * type
     */
    private Integer type;

    /**
     * 页面url/路由
     * page
     */
    private String page;

    /**
     * 权限标识
     * perms
     */
    private String perms;

    /**
     * 是否为外链（0否 1是)
     * is_frame
     */
    private Boolean isFrame;

    /**
     * 图标
     * icon
     */
    private String icon;

    /**
     * 可以存储上下级的关系
     * path
     */
    private String path;

    /**
     * 层级
     * level
     */
    private Integer level;

    /**
     * 排序
     * seq
     */
    private Integer seq;

    /**
     * 是否可见
     * visible
     */
    private Boolean visible;

    /**
     * 位置类型 0 顶部 1左边
     * position_type
     */
    private Integer positionType;

    /**
     * 0正常1停用
     * status
     */
    private Integer status;

    /**
     * 创建人
     * create_by
     */
    private String createBy;

    /**
     * 创建日期
     * create_time
     */
    private Date createTime;

    /**
     * 修改人
     * update_by
     */
    private String updateBy;

    /**
     * 修改日期
     * update_time
     */
    private Date updateTime;

    /**
     * 备注
     * remark
     */
    private String remark;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page == null ? null : page.trim();
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms == null ? null : perms.trim();
    }

    public Boolean getIsFrame() {
        return isFrame;
    }

    public void setIsFrame(Boolean isFrame) {
        this.isFrame = isFrame;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Integer getPositionType() {
        return positionType;
    }

    public void setPositionType(Integer positionType) {
        this.positionType = positionType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy == null ? null : createBy.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy == null ? null : updateBy.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}