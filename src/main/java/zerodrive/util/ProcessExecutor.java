package zerodrive.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProcessExecutor {
    //======================================================================
    // Fields
    private final String charsetName;
    private Receiver stdout;
    private Receiver stderr;


    //======================================================================
    // Constructors
    public ProcessExecutor() {
        this("UTF-8");
    }

    public ProcessExecutor(String _charsetName) {
        this.charsetName = _charsetName;
    }


    //======================================================================
    // Methods
    public int execute(String... command) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);

        final Process process = builder.start();

        this.stdout = new Receiver(process.getInputStream(), this.charsetName);
        this.stderr = new Receiver(process.getErrorStream(), this.charsetName);
        this.stdout.start();
        this.stderr.start();

        return process.waitFor();
    }

    public String readStdout() {
        return this.stdout.queue.poll();
    }

    public String readStderr() {
        return this.stderr.queue.poll();
    }


    //======================================================================
    // Inner Classes
    private static class Receiver extends Thread {
        //======================================================================
        // Fields
        private final Queue<String> queue = new ConcurrentLinkedQueue<>();
        private final BufferedReader reader;


        //======================================================================
        // Constructors
        private Receiver(InputStream in) {
            this(in, "UTF-8");
        }

        private Receiver(InputStream in, String charsetName) {
            try {
                this.reader = new BufferedReader(new InputStreamReader(in, charsetName));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Charset '" + charsetName + "' not supported.");
            }
        }


        //======================================================================
        // Methods
        @Override
        public void run() {
            try {
                String line;
                while (!Thread.interrupted() && null != (line = this.reader.readLine())) {
                    this.queue.offer(line);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                if (null != this.reader) {
                    try {
                        this.reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
