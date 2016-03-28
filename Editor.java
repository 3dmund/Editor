package editor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

import java.io.*;


public class Editor extends Application {

    /************
      ATTRIBUTES
     *************/

    private static int WINDOW_WIDTH = 500;
    private static int WINDOW_HEIGHT = 500;
    private static double usableScreenWidth = WINDOW_WIDTH - 20;

    private static final int STARTING_FONT_SIZE = 12;
    private static final int STARTING_TEXT_POSITION_X = 5;
    private static final int STARTING_TEXT_POSITION_Y = 0;

    int textCenterX;
    int textCenterY;

    private static int totalTextWidth = STARTING_TEXT_POSITION_X;
    private static int totalTextY = STARTING_TEXT_POSITION_Y;
    private static int totalTextHeight = STARTING_TEXT_POSITION_Y;

    private static int cursorPositionX = STARTING_TEXT_POSITION_X;
    private static int cursorPositionY = STARTING_TEXT_POSITION_Y;

    private static int mousePressedX;
    private static int mousePressedY;

    private static int clickPositionX;
    private static int clickPositionY;

    private static int fontSize = STARTING_FONT_SIZE;
    private static String fontName = "Verdana";

    private static int currentLine = 0;
    private static int trackerLine = 0;
    private static int clickLine = 0;

    private static boolean trackerBeforeCurrent = true;
    private static boolean trackerIsCurrent = false;

    private static String secondArgument;

    private static TextBuffer.Node restriction;

    private static int displayedTextTop = 0;
    private static double hidden;
    private static double scrollValue;



    /************
       OBJECTS
    *************/
    private static Group root = new Group();

    private static Group textRoot = new Group();
//    root.getChildren().add(textRoot);

    private static Cursor cursor = new Cursor(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, STARTING_FONT_SIZE);
//    private static ScrollBarClass scrollBar = new ScrollBarClass(STARTING_TEXT_POSITION_Y, totalTextHeight, WINDOW_HEIGHT);

    /* List containing pointers to all starting nodes */
    private static ArrayList <TextBuffer.Node> newLineNodes = new ArrayList();

    /** The Text to display on the screen. */
    private static Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");

    private static TextBuffer buffer = new TextBuffer(fontName, fontSize);

    private static Text currentText;

    private static int charWidth;
    private static int charHeight = (int) (displayText.getLayoutBounds().getHeight() + 0.5);

    private static String outputFilename;
    private static String inputFilename;

    private static ScrollBar scrollBar;


    /******************
      HELPER FUNCTIONS
     ******************/

    private static void reset() {
        totalTextWidth = STARTING_TEXT_POSITION_X;
        totalTextY = STARTING_TEXT_POSITION_Y;
        totalTextHeight = charHeight;
        cursorPositionX = STARTING_TEXT_POSITION_X;
        cursorPositionY = STARTING_TEXT_POSITION_Y;
    }

    private static void enter() {
        totalTextWidth = STARTING_TEXT_POSITION_X;
        totalTextY += charHeight;
        totalTextHeight += charHeight;
        /* check with boolean trackerBeforeCurrent */
        if (trackerBeforeCurrent || trackerIsCurrent) {
            cursorPositionX = STARTING_TEXT_POSITION_X;
            cursorPositionY += charHeight;
            cursor.updatePos(cursorPositionX, cursorPositionY);
        }
    }

    /* decrement tracker until you reach the space */
    private static void wordStart() {
        while (!buffer.tracker().text.getText().equals(" ")) {
            buffer.decrementTracker();
            if (buffer.tracker().equals(buffer.current())) {
                trackerIsCurrent = true;
            }
            if (buffer.tracker().equals(buffer.current().previous)) {
                trackerIsCurrent = false;
                trackerBeforeCurrent = true;
            }
        }
    }

    private static boolean currentInEarlierWord() {
        TextBuffer.Node placeHolder = buffer.tracker();
        boolean is = trackerIsCurrent;
        boolean before = trackerBeforeCurrent;
        boolean found = false;
        wordStart();
        buffer.decrementTracker();
        while (!buffer.tracker().equals(buffer.sentinel()) & !found) {
            if (buffer.tracker().equals(buffer.current())) {
                found = true;
            }
            buffer.decrementTracker();
        }
        buffer.setTracker(placeHolder);
        trackerIsCurrent = is;
        trackerBeforeCurrent = before;
        return found;
    }

    private static void newLine(boolean currentBeforeWord) {
        totalTextWidth = STARTING_TEXT_POSITION_X;
        totalTextY += charHeight;
        totalTextHeight += charHeight;
        /* check for wordoneline */
        if (!currentBeforeWord) {
            cursorPositionX = STARTING_TEXT_POSITION_X;
            cursorPositionY += charHeight;
            currentLine += 1;
        }
    }

    private static boolean wordOneLine() {
        TextBuffer.Node placeHolder = buffer.tracker();
        buffer.decrementTracker();
        while (!buffer.tracker().equals(newLineNodes.get(trackerLine).previous)) {
            if (buffer.tracker().text.getText().equals(" ")) {
                buffer.setTracker(placeHolder);
                return false;
            }
            buffer.decrementTracker();
        }
        buffer.setTracker(placeHolder);
        return true;
    }

    /* only call this when not in last line */
    /* if at last line, set restriction to sentinel */
    private static boolean whiteSpaceCase(TextBuffer.Node restriction) {
        TextBuffer.Node placeHolder = buffer.tracker();
        while (!buffer.tracker().equals(restriction)) {
            if (!buffer.tracker().text.getText().equals(" ")) {
                buffer.setTracker(placeHolder);
                return false;
            }
            buffer.incrementTracker();
        }
        buffer.setTracker(placeHolder);
        if (!buffer.tracker().previous.text.getText().equals(" ")) {
//            buffer.setTracker(placeHolder);
            return false;
        }
//        buffer.setTracker(placeHolder);
        return true;
    }

    private static void setRestriction(int line) {
        if (line == newLineNodes.size() - 1) {
            restriction = buffer.sentinel();
        }
        else {
            restriction = newLineNodes.get(line + 1).previous;
        }
    }

    /** clicking */
    int findLine() {
        int clickedLine = 0;
        int hiddenText = displayedTextTop;
        System.out.println("Hidden: " + displayedTextTop);
        double relativeMousePosition = mousePressedY + displayedTextTop;
        /* move line to first line shown */
        while (clickedLine < newLineNodes.size() - 1) {
            if (relativeMousePosition < charHeight) {
                return clickedLine;
            }
            relativeMousePosition -= charHeight;
            clickedLine += 1;
            clickPositionY += charHeight;
        }
        return newLineNodes.size() - 1;
    }

    /* doesn't account for white space word wrapping */
    void setClickPosition() {
        buffer.setTracker(newLineNodes.get(clickLine));
        int widthTracker = STARTING_TEXT_POSITION_X;
        int overShoot;
        charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
        setRestriction(clickLine);
        while ((widthTracker + charWidth) < mousePressedX &&
                !buffer.tracker().equals(restriction)) {
            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
//                            widthTracker +=  buffer.tracker().text.getLayoutBounds().getWidth();
            widthTracker += charWidth;
            buffer.incrementTracker();
            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
        }
        if (buffer.tracker().equals(restriction)) {
            buffer.decrementTracker();
            clickPositionX = widthTracker;
        }
        else {
            overShoot = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
            if (Math.abs(widthTracker + overShoot - mousePressedX) > Math.abs(mousePressedX - widthTracker)) {
                clickPositionX = widthTracker;
                /* see if this works */
                buffer.decrementTracker();
            }
            else {
                clickPositionX = widthTracker + overShoot;
//                buffer.incrementTracker();
            }
        }
        buffer.setCurrent(buffer.tracker());
        cursor.updatePos(clickPositionX, clickPositionY);
        cursorPositionX = clickPositionX;
        cursorPositionY = clickPositionY;
        currentLine = clickLine;
    }

    /** scrolling */
    private static void scroll(int displayedTextTop) {
        hidden = Math.max(totalTextHeight - WINDOW_HEIGHT, 0);
//        displayedTextTop = (int) (hidden * (scrollValue / (scrollBar.getMax() - scrollBar.getMin())) + 0.5);
        textRoot.setLayoutY(-displayedTextTop);
        System.out.println("totalTextY: " + totalTextY);
        System.out.println("totalTextHeight: " + totalTextHeight);
        System.out.println("window height: " + WINDOW_HEIGHT);
        System.out.println("hidden: " + hidden);
        System.out.println("dispalyedTextTop: " + displayedTextTop);
    }

    private static void updateScroll() {
        scrollBar.setPrefHeight(WINDOW_HEIGHT);

        // Set the range of the scroll bar.
        scrollBar.setMin(STARTING_TEXT_POSITION_Y);
//        totalTextY = totalTextHeight + charHeight;
        scrollBar.setMax(totalTextHeight);

//        usableScreenWidth = (int) (WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth() + 0.5);
        scrollBar.setLayoutX(usableScreenWidth);
        hidden = Math.max(totalTextHeight - WINDOW_HEIGHT, 0);
        displayedTextTop = (int) (hidden * (scrollValue / totalTextHeight) + 0.5);
    }


    /** Changing line */
    private static void changeLine(int newLine, boolean up) {
        int widthTracker = STARTING_TEXT_POSITION_X;
        int overShoot;
                        /* keep track of position for all nodes in newLineNodes */
        buffer.setTracker(newLineNodes.get(newLine));
                        /* also check that tracker isn't pointing to enter flag
                         * and check for space */
        charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
        while ((widthTracker + charWidth) < cursorPositionX &&
                /* Don't need this */
//                !buffer.tracker().equals(newLineNodes.get(currentLine)) &&
                /* new line + 1 */
//                !buffer.tracker().next.equals(newLineNodes.get(currentLine)) &&
                !buffer.tracker().next.equals(newLineNodes.get(newLine + 1)) &&
                !buffer.tracker().next.equals(buffer.sentinel())) {
            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
//                            widthTracker +=  buffer.tracker().text.getLayoutBounds().getWidth();
            widthTracker += charWidth;
            buffer.incrementTracker();
            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
        }
        if (buffer.tracker().next.equals(newLineNodes.get(newLine + 1)) ||
                buffer.tracker().next.equals(buffer.sentinel())) {
            cursorPositionX = widthTracker;
            buffer.decrementTracker();
        }
        else {
            overShoot = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
            if (Math.abs(widthTracker + overShoot - cursorPositionX) > Math.abs(cursorPositionX - widthTracker)) {
                cursorPositionX = widthTracker;
                /* see if this works */
                buffer.decrementTracker();
            }
            else {
                cursorPositionX = widthTracker + overShoot;
//                buffer.incrementTracker();
            }
        }
        buffer.setCurrent(buffer.tracker());
        if (up) {cursorPositionY -= charHeight;}
        else {cursorPositionY += charHeight;}
        cursor.updatePos(cursorPositionX, cursorPositionY);
        currentLine = newLine;
    }

    /** RENDERING */
    private static void render() {
        reset();
        buffer.resetTracker();
        currentLine = 0;
        trackerLine = 0;
        newLineNodes.clear();
        trackerBeforeCurrent = true;
        trackerIsCurrent = false;

//        currentText = buffer.first();
        /* instead of for statement use while loop to check tracker */
        while (buffer.tracker() != buffer.sentinel()) {
                /* Don't use get(), must be constant time */
            if (buffer.tracker().equals(buffer.current())) {
                trackerBeforeCurrent = false;
                trackerIsCurrent = true;
            }
            if (buffer.tracker().equals(buffer.current().next)) {
                trackerBeforeCurrent = false;
                trackerIsCurrent = false;
            }
            if (buffer.tracker().equals(buffer.sentinel().next)) {
                newLineNodes.add(trackerLine, buffer.tracker());
            }
            currentText = buffer.trackerText();
            if (currentText.getText().hashCode() == 13) {
                enter();
                currentLine += 1;
                trackerLine += 1;
                newLineNodes.add(trackerLine, buffer.tracker().next);
            }

            /* need to adjust cursor */
            else if ((totalTextWidth + currentText.getLayoutBounds().getWidth() + 5) > usableScreenWidth) {
                if (!(buffer.tracker().text.getText().equals(" "))) {
                    if (wordOneLine()) {
                        boolean currentInEarlierWord = currentInEarlierWord();
                        newLine(currentInEarlierWord);
                        trackerLine += 1;
                        newLineNodes.add(trackerLine, buffer.tracker());
                        if (buffer.current().next.equals(newLineNodes.get(trackerLine))) {
                            currentLine += 1;
                            cursorPositionX = STARTING_TEXT_POSITION_X;
                            cursorPositionY += charHeight;
                        }
                        buffer.decrementTracker();
                    }
                    else if (buffer.tracker().previous.text.getText().equals(" ")) {
                        /* Make this conditional */
                        /* Check if tracker is in earlier word instead */
                        boolean currentInEarlierWord = currentInEarlierWord();
                        newLine(currentInEarlierWord);
                        trackerLine += 1;
                        newLineNodes.add(trackerLine, buffer.tracker());
//                        enter();
                        buffer.decrementTracker();
                    }
                    else {
//                        boolean currentInWord = currentInWord();
                        boolean currentInEarlierWord = currentInEarlierWord();
                        wordStart();
                        newLine(currentInEarlierWord);
                        trackerLine += 1;
                        newLineNodes.add(trackerLine, buffer.tracker().next);
                    }
//                newLine(currentInWord);
//                    enter();
                }
            }

            else {
                currentText.setTextOrigin(VPos.TOP);
                currentText.setX(totalTextWidth);
                currentText.setY(totalTextY);
                charWidth = (int) (currentText.getLayoutBounds().getWidth() + 0.5);
                totalTextWidth += charWidth;
                if (trackerBeforeCurrent || trackerIsCurrent) {
                    cursorPositionX += charWidth;
                }
            }
            buffer.incrementTracker();
        }
        cursor.updatePos(cursorPositionX, cursorPositionY);
    }

    /*****************************
             KEY PRESSED
     *****************************/
    private class KeyEventHandler implements EventHandler<KeyEvent> {
        KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;

            // Initialize some empty text and add it to root so that it will be displayed.
            displayText = new Text(textCenterX, textCenterY, "");
            // Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
            // that when the text is assigned a y-position, that position corresponds to the
            // highest position across all letters (for example, the top of a letter like "I", as
            // opposed to the top of a letter like "e"), which makes calculating positions much
            // simpler!
            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));
            charHeight = (int) (displayText.getLayoutBounds().getHeight() + 0.5);

            // All new Nodes need to be added to the root in order to be displayed.
            textRoot.getChildren().add(displayText);
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8 && !keyEvent.isShortcutDown()) {
                    // Ignore control keys, which have non-zero length, as well as the backspace
                    // key, which is represented as a character of value = 8 on Windows.
//                    displayText.setText(characterTyped);
                    buffer.addChar(characterTyped);
                    currentText = buffer.currentText();
                    textRoot.getChildren().add(currentText);
                    render();
                    Actions.newAction("deleteChar", buffer.currentText().getText(), buffer.current());
                    Actions.redos.clear();
                    keyEvent.consume();
                }
                /* create separate condition for snapping */
//                double currentScrollValue = scrollValue;
                updateScroll();
                /* snap to cursor */
                if (cursorPositionY < displayedTextTop) {
                    displayedTextTop = cursorPositionY;
                    scroll(displayedTextTop);
                    scrollBar.setValue(displayedTextTop * totalTextHeight / hidden);
                }
                if (cursorPositionY > (displayedTextTop + WINDOW_HEIGHT)) {
                    displayedTextTop = (cursorPositionY + charHeight) - WINDOW_HEIGHT;
                    scroll(displayedTextTop);
                    scrollBar.setValue(displayedTextTop * totalTextHeight / hidden);
                }
//                scrollValue = currentScrollValue;
//                scrollBar.setValue(scrollValue);
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                /* make left and right much shorter */
                if (code == KeyCode.LEFT) {
                    if (!buffer.current().equals(buffer.sentinel())) {
                        buffer.setTracker(buffer.current());
                        setRestriction(currentLine);
                        if (newLineNodes.contains(buffer.tracker().next)) {
                            buffer.setTracker(newLineNodes.get(currentLine - 1));
                            while (!buffer.tracker().equals(newLineNodes.get(currentLine)) &&
                                    !buffer.tracker().next.equals(newLineNodes.get(currentLine))) {
                                charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
                                cursorPositionX += charWidth;
                                buffer.incrementTracker();
                            }
                            cursorPositionY -= charHeight;
                            currentLine -= 1;
                        }
                        else if (whiteSpaceCase(restriction)) {
//                            buffer.decreaseCurrentPos();
                        }
                        else {
                            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
                            cursorPositionX -= charWidth;
                        }
                        buffer.decreaseCurrentPos();
                        cursor.updatePos(cursorPositionX, cursorPositionY);
                    }
                    keyEvent.consume();
                }

                /** Arrow Keys */
                else if (code == KeyCode.RIGHT) {
                    if (!buffer.current().next.equals(buffer.sentinel())) {
//                        buffer.decreaseCurrentPos();
                        buffer.setTracker(buffer.current().next);
                        if (newLineNodes.contains(buffer.tracker().next)) {
                            buffer.setTracker(newLineNodes.get(currentLine + 1));
                            cursorPositionX = STARTING_TEXT_POSITION_X;
                            cursorPositionY += charHeight;
                            currentLine += 1;
                        }
                        else {
                            charWidth = (int) (buffer.tracker().text.getLayoutBounds().getWidth() + 0.5);
                            cursorPositionX += charWidth;
                        }
                        buffer.increaseCurrentPos();
                        cursor.updatePos(cursorPositionX, cursorPositionY);
                    }
                    keyEvent.consume();
                }
                /* Need to implement start of currentLine nodes first
                 * Use a while loop to check the last element in buffer that is before x-pos
                 * compare it to the first element after x-pos*/
                else if (code == KeyCode.UP) {
                    if (currentLine > 0) {
                        changeLine(currentLine - 1, true);
                    }
                    keyEvent.consume();
                }
                else if (code == KeyCode.DOWN) {
                    if (currentLine < (newLineNodes.size() - 1)) {
                        changeLine(currentLine + 1, false);
                    }
                    keyEvent.consume();
                }
                /** Others */
                else if (code == KeyCode.BACK_SPACE) {
                    if (!buffer.current().equals(buffer.sentinel())) {
                        /* must be constant time */
//                        currentText = buffer.get(buffer.currentPos());
                        String deletedCharacter = buffer.current().text.getText();
                        currentText = buffer.currentText();
                        textRoot.getChildren().remove(currentText);
                        buffer.deleteChar();
                        render();
                        Actions.newAction("addChar", deletedCharacter, buffer.current());
                        Actions.redos.clear();
                        cursor.updatePos(cursorPositionX, cursorPositionY);
                    }
                    keyEvent.consume();
                }
                else if (keyEvent.isShortcutDown()) {
                    if (keyEvent.getCode() == KeyCode.P) {
                        System.out.println(cursorPositionX + ", " + cursorPositionY);
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.S) {
                        save(outputFilename);
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                        fontSize += 4;
                        buffer.changeFontSize(fontSize);
                        charHeight = (int) (buffer.sentinel().next.text.getLayoutBounds().getHeight() + 0.5);
                        cursor.updateHeight(charHeight);
//                        System.out.println(fontSize);
                        render();
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.MINUS) {
                        fontSize -= 4;
                        if (fontSize < 4) {
                            fontSize = 4;
                        }
                        buffer.changeFontSize(fontSize);
                        charHeight = (int) (buffer.sentinel().next.text.getLayoutBounds().getHeight() + 0.5);
                        cursor.updateHeight(charHeight);
//                        System.out.println(fontSize);
                        render();
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.Z) {
                        Object[] action = Actions.undo();
                        if (action != null) {
                            if (action[0] == "addChar") {
                                buffer.setCurrent((TextBuffer.Node) action[2]);
                                buffer.addChar((String) action[1]);
                                displayText = buffer.currentText();
                                textRoot.getChildren().add(displayText);
                                render();

                                System.out.println("Character added.");
                            }
                            else if (action[0] == "deleteChar") {
                                buffer.setCurrent((TextBuffer.Node) action[2]);
                                textRoot.getChildren().remove(buffer.currentText());
                                root.getChildren().remove(buffer.currentText());
//                                Actions.setLocation(action, ((TextBuffer.Node) action[2]).previous );
                                Actions.decrementAllLocationsWithAdd(Actions.redos, buffer.current());
                                buffer.deleteChar();

                                render();

                                System.out.println("Character deleted.");
                            }
                        }

                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.Y) {
                        Object[] action = Actions.redo();
                        if (action != null) {
                            if (action[0] == "addChar") {
                                buffer.setCurrent(((TextBuffer.Node) action[2]));

                                buffer.addChar((String) action[1]);
                                Actions.incrementAllLocationsWithAdd(Actions.redos, buffer.current().previous);
                                displayText = buffer.currentText();
                                textRoot.getChildren().add(displayText);
                                render();
                                System.out.println("Character added.");
                            }
                            else if (action[0] == "deleteChar") {
                                buffer.setCurrent(((TextBuffer.Node) action[2]).next);
                                textRoot.getChildren().remove(buffer.currentText());
                                root.getChildren().remove(buffer.currentText());
                                /* if any actions are pointing to this, decrementLocation */
                                Actions.decrementAllLocationsWithAdd(Actions.redos, buffer.current());
//                                Actions.incrementLocation(action);
                                buffer.deleteChar();
//                                Actions.decrementLocation(action);
                                render();
                                System.out.println("Character deleted.");
                            }
                        }

                    }
                    keyEvent.consume();
                }
            }
        }
    }

    /***********
     MOUSE CLICK
     ***********/
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        Text positionText;

        MouseClickEventHandler(Group root) {
            // For now, since there's no mouse position yet, just create an empty Text object.
            positionText = new Text("");
            // We want the text to show up immediately above the position, so set the origin to be
            // VPos.BOTTOM (so the x-position we assign will be the position of the bottom of the
            // text).
            positionText.setTextOrigin(VPos.BOTTOM);

            // Add the positionText to root, so that it will be displayed on the screen.
//            root.getChildren().add(positionText);
        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            mousePressedX = (int) (mouseEvent.getX() + 0.5);
            mousePressedY = (int) (mouseEvent.getY() + 0.5);
            clickPositionX = 0;
            clickPositionY = 0;

            System.out.println(mousePressedX + ", " + mousePressedY);

            clickLine = findLine();
            setClickPosition();
        }
    }

    /******
      START
     ******/
    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
//        Group root = new Group();
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);
        textRoot.getChildren().add(cursor.cursor);
        root.getChildren().add(textRoot);

        cursor.updateHeight(STARTING_FONT_SIZE);
        cursor.makeRectangleColorChange();
        scene.setOnMouseClicked(new MouseClickEventHandler(root));
//        root.getChildren().add(scrollBar.scrollBar);

//        scrollBar.setLayoutX(usableScreenWidth);
        scrollBar = new ScrollBar();

        scrollBar.setOrientation(Orientation.VERTICAL);
        // Set the height of the scroll bar so that it fills the whole window.
        scrollBar.setPrefHeight(WINDOW_HEIGHT);

        // Set the range of the scroll bar.
        scrollBar.setMin(STARTING_TEXT_POSITION_Y);
        System.out.println(STARTING_TEXT_POSITION_Y);
//        totalTextY = totalTextHeight + charHeight;
        System.out.println(totalTextY);
        scrollBar.setMax(totalTextY);

//        usableScreenWidth = (int) (WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth() + 0.5);
        scrollBar.setLayoutX(usableScreenWidth);
//        usableScreenWidth = WINDOW_WIDTH - scrollBar.getLayoutBounds().getWidth();
        System.out.println(scrollBar.getLayoutBounds().getWidth());

        // Add the scroll bar to the scene graph, so that it appears on the screen.
        root.getChildren().add(scrollBar);

        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                // newValue describes the value of the new position of the scroll bar. The numerical
                // value of the position is based on the position of the scroll bar, and on the min
                // and max we set above. For example, if the scroll bar is exactly in the middle of
                // the scroll area, the position will be:
                //      scroll minimum + (scroll maximum - scroll minimum) / 2
                // Here, we can directly use the value of the scroll bar to set the height of Josh,
                // because of how we set the minimum and maximum above.
                scrollValue = newValue.doubleValue();
                System.out.println("Scroll value: " + scrollValue);
                displayedTextTop = (int) (hidden * (scrollValue / (scrollBar.getMax() - scrollBar.getMin())) + 0.5);
                scroll(displayedTextTop);
            }
        });

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                // Re-compute Allen's width.
                int newWindowWidth = newScreenWidth.intValue();
                WINDOW_WIDTH = newWindowWidth;
                usableScreenWidth = WINDOW_WIDTH - 20;
                System.out.println(usableScreenWidth);
//                scrollBar.setLayoutX(usableScreenWidth);
//                int newAllenWidth = getDimensionInsideMargin(newScreenWidth.intValue());
//                allenView.setFitWidth(newAllenWidth);
                render();
                updateScroll();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                int newWindowHeight = newScreenHeight.intValue();
                WINDOW_HEIGHT = newWindowHeight;
//                scrollBar.setPrefHeight(WINDOW_HEIGHT);
//                int newAllenHeight = getDimensionInsideMargin(newScreenHeight.intValue());
//                allenView.setFitHeight(newAllenHeight);
                render();
//                scrollBar.setMax(totalTextY);
                updateScroll();
            }
        });


        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);

        primaryStage.setTitle("Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void open(String inputFileName) {
        try {
            File inputFile = new File(inputFileName);
            // Check to make sure that the input file exists!
            if (!inputFile.exists()) {
                System.out.println("Unable to open file " + inputFileName + ", doesn't exist");
                return;
            }

            int intRead = -1;
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            while ((intRead = bufferedReader.read()) != -1) {
                // The integer read can be cast to a char, because we're assuming ASCII.
                char charRead = (char) intRead;
                String stringRead = "" + charRead;
                buffer.addChar(stringRead);
                textRoot.getChildren().add(buffer.currentText());
            }
            render();

        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("Unable to open file" + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    public static void save(String outputFilename) {
        try {
            File outputFile = new File(outputFilename);
            // Check to make sure that the input file exists!
            if (!outputFile.exists()) {
                System.out.println("Unable to save" + outputFilename);
                return;
            }
            FileReader reader = new FileReader(outputFile);
            // It's good practice to read files using a buffered reader.  A buffered reader reads
            // big chunks of the file from the disk, and then buffers them in memory.  Otherwise,
            // if you read one character at a time from the file using FileReader, each character
            // read causes a separate read from disk.  You'll learn more about this if you take more
            // CS classes, but for now, take our word for it!
            BufferedReader bufferedReader = new BufferedReader(reader);

            // Create a FileWriter to write to outputFilename. FileWriter will overwrite any data
            // already in outputFilename.
            FileWriter writer = new FileWriter(outputFilename);

            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            buffer.resetTracker();
            while (buffer.trackerText().getText() != "") {
                // The integer read can be cast to a char, because we're assuming ASCII.
                int intRead = (int) buffer.trackerText().getText().charAt(0);
                writer.write(intRead);
                buffer.incrementTracker();
            }
            System.out.println("Successfully saved file");

            // Close the reader and writer.
            bufferedReader.close();
            writer.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.println("File not found! Exception was: " + fileNotFoundException);
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            inputFilename = args[0];
            outputFilename = inputFilename;
            if (args.length > 1) {
                secondArgument = args[1];
            }
            open(inputFilename);
            Print.print(secondArgument);
        }
        else {
            System.out.println("No filename provided");
        }
        launch(args);


    }

}