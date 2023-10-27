package com.restkeeper.operator;

import com.alibaba.dubbo.config.annotation.Reference;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EnterpriseCountTest {

    @Reference(version = "1.0.0", check = false)
    private IEnterpriseAccountService enterpriseAccountService;

    @Test
    @Rollback(false)
    public void deleteTest() {
        enterpriseAccountService.removeById("1715613370213748738");
    }

    @Test
    @Rollback(false)
    public void add() {

        System.out.println(Md5Crypt.md5Crypt("123456".getBytes()));
    }


}