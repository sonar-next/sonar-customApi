package org.sonar.plugin.issues;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.common.utils.MybatisUtils;
import org.sonar.dao.UserDao;
import org.sonar.entity.User;
import org.sonar.service.SonarService;
import org.sonar.service.impl.SonarServiceImpl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * 自定义接口
 */
@Slf4j
public class UserApiService implements WebService {
    public static final String ERROR ="error";
    public static final String PARAM ="param";
    //设置返回值为json
    public static final String JSONTYPE = "application/json";


    private SonarService sonarService = new SonarServiceImpl();

    private final class Handle implements RequestHandler {
        @Override
        public void handle(Request request, Response response)  {
            //1.获取SqlSession对象
            SqlSession sqlSession = MybatisUtils.getSqlSession();
            //2.执行SQL
            UserDao mapper = sqlSession.getMapper(UserDao.class);
            User user = mapper.getCount();
            try{
                OutputStream output = response.stream()
                        .setMediaType(UserApiService.JSONTYPE)
                        .setStatus(200)
                        .output();
                //这样设置才会返回值为json格式
                try(OutputStreamWriter writer = new OutputStreamWriter(output)) {
                    writer.write(JSON.toJSONString(user));
                }
            }catch (Exception e){
                response.newJsonWriter()
                        .beginObject()
                        .prop(ERROR,"api/user/ is error")
                        .endObject()
                        .close();
            }finally {
                response.noContent();
            }
        }
    }

    @Override
    public void define(Context context) {
        NewController controller = context.createController("api/customuser");
        controller.setDescription("customuser  api");
        //1.查询用户问题总数
        // 2.查询项目下问题分类总数
        NewAction issuesAction = controller.createAction("database");
        issuesAction.setDescription("custom database ")
                .setHandler(new Handle());
        //非必填选项设置一个默认值，否则在Handle 方法中获取数据会失败
        controller.done();
    }
}
