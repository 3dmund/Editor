package editor;

/**
 * Created by EdmundTian on 3/6/16.
 */
public class Print {
    public static void print(String StringToPrint) {
        if (StringToPrint.equals("debug")) {
            System.out.println("Debug");
        }
    }

    public Print(String StringToPrint) {
        print(StringToPrint);
    }
}
