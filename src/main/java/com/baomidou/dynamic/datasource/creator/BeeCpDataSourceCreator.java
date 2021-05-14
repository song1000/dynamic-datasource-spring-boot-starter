/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.creator;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.beecp.BeeCpConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static com.baomidou.dynamic.datasource.support.DdConstants.BEECP_DATASOURCE;
import static com.baomidou.dynamic.datasource.support.DdConstants.HIKARI_DATASOURCE;

/**
 * BeeCp数据源创建器
 *
 * @author TaoYu
 * @since 2020/5/14
 */
@Slf4j
@Data
@AllArgsConstructor
public class BeeCpDataSourceCreator implements DataSourceCreator {

    private static Boolean beeCpExists = false;
    private static Method configCopyMethod = null;

    static {
        try {
            Class.forName(HIKARI_DATASOURCE);
            beeCpExists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private BeeCpConfig beeCpConfig;

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        BeeDataSourceConfig config = dataSourceProperty.getBee().toBeeCpConfig(beeCpConfig);
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        config.setPoolName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }
        if (!dataSourceProperty.getLazy()) {
            return new BeeDataSource(config);
        } else {
            log.warn("beecp current not support lazy init");
            return new BeeDataSource(config);
        }
//        BeeDataSource beeDataSource = new BeeDataSource();
//        try {
//            Method copyToMethod = BeeDataSourceConfig.class.getDeclaredMethod("copyTo", BeeDataSourceConfig.class);
//            copyToMethod.setAccessible(true);
//            copyToMethod.invoke(beeDataSource, config);
//        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return beeDataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && beeCpExists) || (type != null && BEECP_DATASOURCE.equals(type.getName()));
    }
}
