package zerodrive.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

public class ProcessExecutor {
    //======================================================================
    // Fields
    private final String charsetName;
    private final List<RowHandler> stdoutHandlerList = new LinkedList<>();
    private final List<RowHandler> stderrHandlerList = new LinkedList<>();
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
    public void addStdoutHandler(RowHandler handler) {
        if (null == handler) {
            throw new NullPointerException("Argument 'handler' must not be null.");
        }
        this.stdoutHandlerList.add(handler);
    }

    public void addStderrHandler(RowHandler handler) {
        if (null == handler) {
            throw new NullPointerException("Argument 'handler' must not be null.");
        }
        this.stderrHandlerList.add(handler);
    }

    public int execute(String... command) throws Exception {
        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(false);

        final Process process = builder.start();

        this.stdout = new Receiver(process.getInputStream(), this.charsetName);
        for (RowHandler handler : this.stdoutHandlerList) {
            this.stdout.addHandler(handler);
        }

        this.stderr = new Receiver(process.getErrorStream(), this.charsetName);
        for (RowHandler handler : this.stderrHandlerList) {
            this.stderr.addHandler(handler);
        }

        this.stdout.start();
        this.stderr.start();

        return process.waitFor();
    }


    //======================================================================
    // Inner Classes
    public interface RowHandler {
        void handle(String line);
    }

    private static class Receiver extends Thread {
        //======================================================================
        // Fields
        private final BufferedReader reader;
        private final List<RowHandler> handlerList = new LinkedList<>();


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
        private void addHandler(RowHandler handler) {
            if (null == handler) {
                throw new NullPointerException("Argument 'handler' must not be null.");
            }
            this.handlerList.add(handler);
        }

        @Override
        public void run() {
            try {
                String line;
                while (!Thread.interrupted() && null != (line = this.reader.readLine())) {
                    for (RowHandler handler : this.handlerList) {
                        handler.handle(line);
                    }
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
