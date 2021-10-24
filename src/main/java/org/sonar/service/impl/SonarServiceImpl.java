package org.sonar.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sonar.dao.SonarExecuteDao;
import org.sonar.entity.*;
import org.sonar.service.SonarService;
import org.sonar.vo.CompentsVo;
import org.sonar.vo.ParamVo;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


@Slf4j
public class SonarServiceImpl implements SonarService {

    //重复行
    private static final String DUPLICATED = "coverHits";
    //单元测试覆盖行
    private static final String UTLINEHITS = "utLineHits";
    //重复
    private static final String COVERAGE = "coverage";
    //覆盖
    private static final String DUPLICATED_LINES_DENSITY = "duplicated_lines_density";
    //获取接口组件
    private static final String COMPONENTS = "components";

    //查询项目指标
    public static final String PROJECTMETRICKEYS = "ncloc,bugs,vulnerabilities,code_smells,coverage,duplicated_lines_density";
    //查询项目中所有api接口
    //TODO 修改sonar服务器地址
    public static final String SEARCHURL = "修改你sonar的服务地址";
    public static final String PROJECTLINES = "/api/sources/lines";
    public static final String SEARCHLOGIN = "/api/authentication/login";
    public static final String SEARCHPROJECTFIL = "/api/measures/component_tree";
    //在这里修改登录用的名和密码  获取单文件的提交信息需要用户的权限
    //TODO 修改登录sonar账号密码
    public static final String USERLOGIN = "login=loginName&password=userpassword";


    private SonarExecuteDao sonarExecute =  new SonarExecuteDao();

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private List<CompentsVo> overAllList = new ArrayList<>();



    @Override
    public Map<String, Map<String, Integer>> userQuality(String key) {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, String> headers = getCookieHeaders();
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("key",key);
        String urlJson = sonarExecute.getUrlJson(SEARCHURL + PROJECTLINES, headers, queryParams);
        JSONObject mapTypes = JSON.parseObject(urlJson);
        String sources = mapTypes.get("sources").toString();
        List<SourcesLines> listSources = JSON.parseObject(sources, new TypeReference<List<SourcesLines>>() {});
        if (!listSources.isEmpty()) {
            //统计单个文件中，用户重复行和覆盖行
            for (SourcesLines sour : listSources) {
                if (sour.isDuplicated() || (sour.getUtLineHits() != null && sour.getUtLineHits()== 1) ) {
                    String scmAuthor = sour.getScmAuthor();
                    if (StringUtils.isNotBlank(scmAuthor)) {
                        Map<String, Integer> userMap = map.get(scmAuthor);
                        //第一次读取时未读取到文件内容
                        if (userMap == null) {
                            HashMap<String, Integer> userDupCount = new HashMap<>();
                            userDupCount.put(DUPLICATED,sour.isDuplicated() ? 1:0);
                            userDupCount.put(UTLINEHITS,(sour.getUtLineHits() != null && sour.getUtLineHits()== 1) ? 1:0);
                            map.put(scmAuthor,userDupCount);
                        }else {
                            userMap.put(DUPLICATED, sour.isDuplicated() ? userMap.get(DUPLICATED) +1 :userMap.get(DUPLICATED));
                            userMap.put(UTLINEHITS,(sour.getUtLineHits() != null && sour.getUtLineHits()== 1) ? userMap.get(UTLINEHITS) +1 :userMap.get(UTLINEHITS));
                        }
                    }

                }
            }
        }
        return map;
    }


    @Override
    public List<CompentsVo> listFiLProject(String component) {
        List<CompentsVo> listSources = null;
        //全局变量设置为空
        overAllList = new ArrayList<>();
        //获取cookie值
        Map<String, String> headers = getCookieHeaders();
        //获取单文件内容
        listSources = getProjectCompents(component, headers);
        //第一层获取指定数据
        return listSources;
    }

    /**
     * 查询项目中除了文件夹外的所有文件
     * @param component  组件
     * @param headers   请求头参数
     * @return
     */
    private List<CompentsVo> getProjectCompents(String component, Map<String, String> headers) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("component", component);
        queryParams.put("strategy","children");
        queryParams.put("metricKeys",PROJECTMETRICKEYS);
        String urlJson = sonarExecute.getUrlJson(SEARCHURL + SEARCHPROJECTFIL, headers, queryParams);
        JSONObject mapTypes = JSON.parseObject(urlJson);
        String components = mapTypes.get(COMPONENTS).toString();
        List<CompentsVo> listSources = JSON.parseObject(components, new TypeReference<List<CompentsVo>>() {});
        if (!listSources.isEmpty()){
            for (CompentsVo pro : listSources) {
                Boolean isDucOrUtl = false;
                Map<String, String> collect = pro.getMeasures().stream().collect(Collectors.toMap(ParamVo::getMetric, ParamVo::getValue));
                String cover = collect.get(COVERAGE);
                String dupli = collect.get(DUPLICATED_LINES_DENSITY);
                if ((cover != null && !cover.equals("0.0")) || (dupli != null && !dupli.equals("0.0"))) {
                    isDucOrUtl = true;
                }
                if (Boolean.TRUE.equals(isDucOrUtl)){
                    if (pro.getQualifier().equals("DIR")) {
                        getProjectCompents(pro.getKey(),headers);
                    }else {
                        overAllList.add(pro);
                    }
                }
            }
        }
        return overAllList;
    }

    /**
     * 公共方法获取cookie Map信息
     * @return
     */
    private Map<String, String> getCookieHeaders() {
        String userCookie = sonarExecute.getUserCookie(SEARCHURL + SEARCHLOGIN, USERLOGIN);
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", userCookie);
        return headers;
    }


}
