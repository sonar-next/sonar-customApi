package org.sonar.plugin;

import org.sonar.api.Plugin;
import org.sonar.common.scheduled.TimerProject;
import org.sonar.plugin.issues.CustomApiService;
import org.sonar.plugin.issues.UserApiService;

/**
 * 插件初始化集成
 */
public class webServicePlugin implements Plugin {

    @Override
    public void define(Context context) {
        //自定义接口——sonar中获取数据
        context.addExtension(CustomApiService.class);
        //自定义接口——数据库获取数据
        context.addExtension(UserApiService.class);

        //执行定时器任务
        TimerProject timerProject = new TimerProject();
        timerProject.insertDB();
    }
}
