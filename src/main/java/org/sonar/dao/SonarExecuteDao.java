package org.sonar.dao;

import lombok.extern.slf4j.Slf4j;
import org.sonar.common.utils.HttpUtils;

import java.io.IOException;
import java.util.Map;


@Slf4j
public class SonarExecuteDao {


    /**
     * 获取远程请求基本json信息
     * @param url 查询URL地址
     * @param headers   请求头地址
     * @param queryParams   查询参数
     * @return
     */
    public String getUrlJson(String url, Map<String, String> headers, Map<String, Object> queryParams) {
        String s = null;
        try {
            s = HttpUtils.doGet(url,headers,queryParams);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return s;
    }

    /**
     *  获取用户cookie ,设置到请求头中
     * @param url url地址
     * @param path 用户登录信息
     * @return
     */
    public String getUserCookie(String url,String path){
        String connectCookie = null;
        try {
            connectCookie = HttpUtils.getConnectCookie(url, path);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return connectCookie;
    }

}
