package com.vipshop.microscope.test.integrate;

import com.vipshop.microscope.trace.Tracer;
import com.vipshop.microscope.trace.span.Category;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TopReportTest {

    @Test
    public void testTopReport() {
        while (true) {
            Tracer.cleanContext();
            Tracer.clientSend("http://www.huohu.com", Category.URL);
            try {
                TimeUnit.MILLISECONDS.sleep(new Random(1000).nextInt(1000));
                Tracer.clientSend("getNew@newService", Category.Service);
                TimeUnit.MILLISECONDS.sleep(new Random(1000).nextInt(1000));
                Tracer.clientSend("get@DB", Category.DB);
                TimeUnit.MILLISECONDS.sleep(new Random(1000).nextInt(1000));
                Tracer.clientReceive();
                Tracer.clientReceive();

                Tracer.clientSend("buyNew@buyService", Category.Service);
                TimeUnit.MILLISECONDS.sleep(new Random(1000).nextInt(1000));
                Tracer.clientSend("buy@Cache", Category.Cache);
                TimeUnit.MILLISECONDS.sleep(new Random(1000).nextInt(1000));
                Tracer.clientReceive();
                Tracer.clientReceive();
            } catch (Exception e) {
                Tracer.setResultCode(e);
            } finally {
                Tracer.clientReceive();
            }
        }
    }


}
