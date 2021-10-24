package org.sonar.vo;

import lombok.Data;

import java.util.List;

@Data
public class CompentsVo {
    private String key;
    private String qualifier;
    private List<ParamVo> measures;
}
