package com.steincker.entity;

import lombok.*;

import java.util.Date;

/**
 * [系统配置] 多语言
 * @author ST000050
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConfigLanguage {
    /** id */
    private Long id;

    // -------------------- 识别信息 --------------------

    /** 应用 */
    private String app;

    /** 类型 */
    private String type;

    /** 编码 */
    private String code;

    // -------------------- 语言信息 --------------------

    /** 语言 */
    private String lang;

    /** 名称 */
    private String text;

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
