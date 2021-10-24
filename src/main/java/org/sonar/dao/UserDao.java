package org.sonar.dao;

import org.apache.ibatis.annotations.Mapper;
import org.sonar.entity.User;

@Mapper
public interface UserDao {

    /**
     * 查询数据库中文件条数
     * @return
     */
    User getCount();

}
