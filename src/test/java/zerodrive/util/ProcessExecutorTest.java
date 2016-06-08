package zerodrive.util;

import org.junit.Assert;
import org.junit.Test;


public class ProcessExecutorTest {

    @Test
    public void test() throws Exception {
        ProcessExecutor executor = new ProcessExecutor("Windows-31J");
        int returnCode = executor.execute("java", "-version");
        Assert.assertSame(0, returnCode);

        String line;
        while (null != (line = executor.readStdout())) {
            System.out.println(line);
        }
        while (null != (line = executor.readStderr())) {
            System.err.println(line);
        }
    }
}
