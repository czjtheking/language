package com.steincker.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 能源模型事件
 * @author Steincker Mybatis Generator
 */
@Getter
@Setter
@ToString
public class EnergyModelEvent {
    /** id */
    private Long id;

    /** 名称 */
    private String name;

    // -------------------- 识别信息，关联信息 --------------------

    /** 标识 【业务识别】【国际化 code】 */
    private String code;

    /** 父事件 id */
    private Long parentId;

    /** 父事件标识 【业务派生】【国际化 code】 */
    private String parentCode;

    /** 类型标识 【类型事件】 */
    private String typeCode;

    /** 产品 id 【产品事件】 */
    private Long productId;

    // -------------------- 功能信息 --------------------

    /** 告警 id */
    private Integer alarmId;

    /** 原因 id */
    private Integer causeId;

    /** 类型（0：未分类，1：系统创建，2：设备上报，3：数值转换，4：数据质量） */
    private Byte type;

    /** 分组（0：未分组，1：通信异常） 【-> 自定义分组表 id？】 */
    private Byte group;

    /** 级别（0：提示，1：次要，2：重要，3：紧急） 【-> 自定义级别表 id？】 */
    private Byte level;

    // -------------------- 描述信息 --------------------

    /** 属性 json 【key: 名称 / 国际化？】 */
    private String attr;

    /** 原因 【国际化 code】 */
    private String cause;

    /** 建议 【国际化 code】 */
    private String suggestion;

    // -------------------- 通用信息 --------------------

    /** 描述和说明信息 */
    private String description;
}
