package org.sonar.service;


import org.sonar.vo.CompentsVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface SonarService {


    /**
     * 统计单个文件用户重复行和覆盖行
     * @param key 查询文件地址
     * @return
     */
    Map<String,Map<String,Integer>> userQuality(String key);


    /**
     * 获取项目下对应所有代码文件
     * @return 项目组件
     */
    List<CompentsVo> listFiLProject(String component);


}
