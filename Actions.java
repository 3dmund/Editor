package editor;
import javax.xml.soap.Text;
import java.util.Stack;

/**
 * Created by EdmundTian on 3/7/16.
 */
public class Actions {
    /* Use Array of size two */
    public static Stack<Object[]> undos = new Stack<>();
    public static Stack<Object[]> redos = new Stack<>();

    public static String opposite(String action) {
        if (action == "addChar") {
            return "deleteChar";
//            actionData[0] = "deleteChar";
        }
        else {
            return "addChar";
//            actionData[0] = "addChar";
        }
//        return actionData;
    }

    public static void setLocation(Object[] action, TextBuffer.Node location) {
        action[2] = location;
    }

    public static void decrementLocation(Object[] action) {
        action[2] = ((TextBuffer.Node) action[2]).previous;
    }

    public static void decrementAllLocationsWithAdd(Stack<Object[]> changes, TextBuffer.Node location) {
        for (int i = 0; i < changes.size(); i += 1) {
            if (((TextBuffer.Node) changes.get(i)[2]).equals(location) &&
                    changes.get(i)[0] == "addChar") {
                decrementLocation(changes.get(i));
            }
        }
    }

    public static void incrementLocation(Object[] action) {
        action[2] = ((TextBuffer.Node) action[2]).next;
    }

    public static void incrementAllLocationsWithAdd(Stack<Object[]> changes, TextBuffer.Node location) {
        for (int i = 0; i < changes.size(); i += 1) {
            if (((TextBuffer.Node) changes.get(i)[2]).equals(location) &&
                    changes.get(i)[0] == "addChar") {
                incrementLocation(changes.get(i));
            }
        }
    }

    public static void newAction(String action, String character, TextBuffer.Node node) {
        Object[] actionData = new Object[3];
        actionData[0] = action;
        actionData[1] = character;
        actionData[2] = node;
        undos.push(actionData);
        if (undos.size() > 100) {
            undos.remove(0);
        }
    }

    public static Object[] undo() {
        if (undos.size() > 0) {
            Object[] lastAction = undos.pop();
            Object[] oppositeAction = new Object[] {opposite((String) lastAction[0]), lastAction[1], lastAction[2]};
            if (oppositeAction[0] == "addChar") {
                decrementLocation(oppositeAction);
            }
            redos.push(oppositeAction);
            return lastAction;
        }
        return null;
    }

    public static Object[] redo() {
        if (redos.size() > 0) {
            Object[] lastAction = redos.pop();
            Object[] oppositeAction = new Object[] {opposite((String) lastAction[0]), lastAction[1], lastAction[2]};
            if (oppositeAction[0] == "addChar") {
                decrementLocation(oppositeAction);
            }
            undos.push(oppositeAction);
            return lastAction;
        }
        return null;
    }

    public static void main(String[] args) {

    }



    /*
    public static void main (String[] args) {
        undos = new Stack<>();
        newAction(0);
        newAction(1);
        newAction(2);
        newAction(3);
//        undos.remove(0);
        System.out.println(undos.get(0));
        System.out.println(undos.get(1));
        System.out.println(undos.get(2));
//        System.out.println(undos.get(3));
    }
    */
}
