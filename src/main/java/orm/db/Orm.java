package orm.db;

public class Orm {

    public static void load() {

        try {// gets caller class full path
            StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
            Class<?> callerClass = Class.forName(ste.getClassName());
            String s = callerClass.getName();
            int i = s.lastIndexOf(".");
            if(i > -1) s = s.substring(i + 1);
            s = s + ".class";
            Object testPath =  callerClass.getResource(s);

            Inspector inspector = Inspector.getInspector();
            inspector.inspect(testPath.toString());
        } catch (Exception e) {e.printStackTrace();}
    }


}
