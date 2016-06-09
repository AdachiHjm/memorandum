package zerodrive.util;

import org.junit.Assert;
import org.junit.Test;

import zerodrive.util.ProcessExecutor.RowHandler;


public class ProcessExecutorTest {

    @Test
    public void test() throws Exception {
        ProcessExecutor executor = new ProcessExecutor("Windows-31J");
        executor.addStderrHandler(new RowHandler() {
            @Override
            public void handle(String line) {
                System.err.println(line);
            }
        });
        int returnCode = executor.execute("java", "-version");
        Assert.assertSame(0, returnCode);
    }
}
