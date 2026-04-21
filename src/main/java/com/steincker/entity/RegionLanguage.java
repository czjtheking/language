package com.steincker.entity;

import lombok.*;

import java.util.Date;

/**
 * [国家地区] 多语言
 * @author ST000050
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RegionLanguage {
    /** id */
    private Long id;

    // -------------------- 识别信息 --------------------

    /** 区域类型（1：洲，2：国家地区，3：行政地区） */
    private Byte type;

    /** 数字代码 */
    private String code;

    /** 数字代码路径 */
    private String codePath;

    // -------------------- 语言信息 --------------------

    /** 语言 */
    private String lang;

    /** 名称 */
    private String name;

    /** 简称 */
    private String abbr;

    /** 首字母 */
    private String initial;

    // -------------------- 通用信息 --------------------

    /** 描述和说明信息 */
    private String description;

    /** 删除标识（0：未删除，同 id：删除） */
    private Long deleted;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 修改人 */
    private String updateBy;

    /** 修改时间 */
    private Date updateTime;
}
