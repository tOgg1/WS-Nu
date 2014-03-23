package org.ntnunotif.wsnu.base.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Log {
    private static File _logFile;
    private static BufferedWriter _logWriter;
    private static boolean _writeToFile = false;

    public static void initLogFile() throws RuntimeException {
        _logFile = new File("config/log.txt");
        try
        {
            if(!_logFile.isFile())
                _logFile.createNewFile();
            try
            {
                _logWriter = new BufferedWriter(new FileWriter(_logFile, true));
            }
            catch(IOException e)
            {
                Log.e("Log", "Error setting log file");
                return;
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException("Log file not found, or can't be accessed!");
        }
    }

    public static void d(String tag, String content)
    {
        String debug = "[Debug " + tag + "]: " + content;
        System.out.println(debug);
        writeToFile(debug);
    }

    public static void e(String tag, String content)
    {
        String err = "[Error " + tag + "]: " + content;
        System.err.println(err);
        writeToFile(err);
    }

    private static void writeToFile(String s)
    {
        if(_writeToFile)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa");
            Date today = Calendar.getInstance().getTime();
            String timestamp = sdf.format(today);
            try
            {
                _logWriter.newLine();
                _logWriter.write(timestamp + " " + s);
                _logWriter.flush();

            }
            catch(IOException e)
            {
                System.err.println("[Error Log]: Error writing to file");
                return;
            }
        }
    }

    public static void close()
    {
        try
        {
            _logWriter.close();
        }
        catch(Exception e)
        {
            // Fuck off we're done already
        }
    }

    public static boolean clearLogFile()
    {
        try
        {
            _logWriter.close();
            String name = _logFile.getName();
            _logFile.delete();
            _logFile = new File(name);
            _logWriter = new BufferedWriter(new FileWriter(_logFile,  true));
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}