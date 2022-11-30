package org.j3d;

public class Log {

    public static int level = 4;
    
    private static Log instance = null;

    static {
        new Log();
    }

    public Log() {
        instance = this;
    }

    protected void put(int level, Object message) {
        if(level <= Log.level) {
            System.out.println(message);
        }
    }


    public static void log(int level, Object message) {
        instance.put(level, message);
    }
}
