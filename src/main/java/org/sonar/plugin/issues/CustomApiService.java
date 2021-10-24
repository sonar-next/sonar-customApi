package org.sonar.plugin.issues;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.service.SonarService;
import org.sonar.service.impl.SonarServiceImpl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * 自定义接口
 */
@Slf4j
public class CustomApiService implements WebService {
    public static final String ERROR ="error";
    public static final String PARAM ="param";
    //设置返回值为json
    public static final String JSONTYPE = "application/json";


    private SonarService sonarService = new SonarServiceImpl();

    private final class Handle implements RequestHandler {
        @Override
        public void handle(Request request, Response response)  {
            //获取请求接口参数值
            String facetsP = request.mandatoryParam(PARAM);
            log.info("----返回传递参数值:{}",facetsP);
            //TODO:修改这里查询sonar项目文件地址
            String key = "saas-cloud-infromation:src/main/java/com/infromation/resource/enterprise/model/test1.java";
            Map<String, Map<String, Integer>> value = sonarService.userQuality(key);
            try{
                OutputStream output = response.stream()
                        .setMediaType(CustomApiService.JSONTYPE)
                        .setStatus(200)
                        .output();
                //这样设置才会返回值为json格式
                try(OutputStreamWriter writer = new OutputStreamWriter(output)) {
                    writer.write(JSON.toJSONString(value));
                }
            }catch (Exception e){
                response.newJsonWriter()
                        .beginObject()
                        .prop(ERROR,"api/customapi/issues is error")
                        .endObject()
                        .close();
            }finally {
                response.noContent();
            }
        }
    }

    @Override
    public void define(Context context) {
        NewController controller = context.createController("api/customapi");
        controller.setDescription("customapi issues api");
        //1.查询用户问题总数
        // 2.查询项目下问题分类总数
        NewAction issuesAction = controller.createAction("issues");
        issuesAction.setDescription("custom Read issues ")
                .setHandler(new Handle());
        //非必填选项设置一个默认值，否则在Handle 方法中获取数据会失败
        issuesAction.createParam(PARAM).setDescription("facets param").setRequired(true);
        controller.done();
    }
}
