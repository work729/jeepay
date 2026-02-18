package com.jeequan.jeepay.service;

import com.baomidou.mybatisplus.extension.ddl.IDdl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
public class MysqlDdl implements IDdl {

    @Autowired
    private DataSource dataSource;

    @Override
    public List<String> getSqlFiles() {
        return Arrays.asList(
                "db/ddl/t_pay_product.sql"
        );
    }

    @Override
    public void runScript(Consumer<DataSource> consumer) {
        consumer.accept(this.dataSource);
    }
}
