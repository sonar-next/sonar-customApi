package org.sonar.common.scheduled;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TimerProject {

     public void insertDB() {
         //自定义定时器
         Timer timer=new Timer("Timer-0");
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                log.info("执行定时任务开始，线程名称:{}",Thread.currentThread().getName());
             }},1000,1000);
     }

}
