//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

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
    private static boolean _logDebug = true;
    private static boolean _logErrors = true;
    private static boolean _logWarnings = true;

    public static void initLogFile() throws RuntimeException {
        _logFile = new File("logs/log.txt");
        _writeToFile = true;
        try
        {
            if(!_logFile.getParentFile().exists())
                _logFile.getParentFile().mkdirs();

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
            e.printStackTrace();
            throw new RuntimeException("Log file not found, or can't be accessed!");
        }
    }

    public static void setWriteToFile(boolean _writeToFile) {
        Log._writeToFile = _writeToFile;
    }

    public static void setEnableErrors(boolean logErrors) {
        Log._logErrors = logErrors;
    }

    public static void setEnableDebug(boolean _enableDebug) {
        Log._logDebug = _enableDebug;
    }

    public static void setEnableWarnings(boolean _logWarnings){
        Log._logWarnings = _logWarnings;
    }

    public static void d(String tag, String content)
    {
        if(!_logDebug)
            return;
        String debug = "[Debug " + tag + "]: " + content;
        System.out.println(debug);
        writeToFile(debug);
    }

    public static void e(String tag, String content)
    {
        if(!_logErrors)
            return;
        String err = "[Error " + tag + "]: " + content;
        System.err.println(err);
        writeToFile(err);
    }

    public static void w(String tag, String content){
        if(!_logWarnings)
            return;
        String warning = "[Warning " + tag + "]: " + content;
        System.out.println(warning);
        writeToFile(warning);
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
            System.err.println("Logwriter closed with an exception");
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