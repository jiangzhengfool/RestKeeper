import com.restkeeper.tenant.TenantContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BaseTest {

    @Before
    public void init() {
        //RpcContext.getContext().setAttachment("shopId","test");

        Map<String, Object> map = new HashMap<>();
        map.put("shopId", "test");
        map.put("loginUserId", "1205130634617622530");
        TenantContext.addAttachments(map);
    }
}
