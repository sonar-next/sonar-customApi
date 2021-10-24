package org.sonar.entity;

import lombok.Data;

@Data
public class SourcesLines {

    private Integer line;
    //内容
    private String code;
    //版本
    private String scmRevision;
    //作者
    private String scmAuthor;
    //时间
    private String scmDate;
    //是否重复行
    private boolean duplicated;
    //是否新增行
    private boolean isNew;
    //单元测试是否覆盖
    private Integer utLineHits;
}
