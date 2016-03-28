package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
/**
 * Created by EdmundTian on 2/27/16.
 */

/* Attempting to get rid of two classes and just use TextBuffer
 */

 public class TextBuffer {

    public class Node {
//            public String character;
        public Text text;
        public Node next;
        public Node previous;

        public Node(String s, Node n, Node p, String font, int fontSize) {
            text = new Text(0, 0, s);
            text.setFont(Font.font(font, fontSize));
            next = n;
            previous = p;
        }
    }

    private Node sentinel;
    private Node first;
    private Node last;
    private Node current;
    private Node tracker;
//    private Node wrapTracker;
    private int currentPos;
    private int size;
    private int trackerIndex;
    private String font;
    private int fontSize;

    /* creates an empty TextBuffer */
    public TextBuffer(String font, int fontSize) {
        sentinel = new Node(null, null, null, font, fontSize);
        sentinel.next = sentinel;
        sentinel.previous = sentinel;
        first = sentinel;
        last = sentinel;
        current = sentinel;
        currentPos = -1;
        size = 0;
        this.font = font;
        this.fontSize = fontSize;
    }
    public Node first() {
        return first;
    }
    public Node tracker() {
        return tracker;
    }
    public void resetTracker() {
        tracker = sentinel.next;
        trackerIndex = 0;
    }
    public void incrementTracker() {
        tracker = tracker.next;
        trackerIndex += 1;
    }
    public void decrementTracker() {
        tracker = tracker.previous;
        trackerIndex -= 1;
    }
    public Text trackerText() {
        return tracker.text;
    }
    public int trackerIndex() {
        return trackerIndex;
    }
    public void setTracker(Node node) {
        tracker = node;
    }
//    public void resetWrapTracker() {
//        wrapTracker = current;
//    }
//    public void decrementWrapTracker() {
//        wrapTracker = wrapTracker.previous;
//    }

    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return this.size;
    }
    public int currentPos() {
        return currentPos;
    }
    public Node current() {
        return current;
    }
    public Text currentText() {
        return current.text;
    }
    public void increaseCurrentPos() {
        currentPos += 1;
        current = current.next;
    }
    public void decreaseCurrentPos() {
        currentPos -= 1;
        current = current.previous;
    }
    public void setCurrent(Node node) {
        current = node;
    }

    public void addChar(String c) {
        Node newChar = new Node(c, current.next, current, font, fontSize);
        current.next = newChar;
        newChar.next.previous = newChar;
        current = current.next;
        currentPos += 1;
        size += 1;
        if (size == 1) {
            first = newChar;
            last = newChar;
        }
    }

    public void deleteChar() {
        current = current.previous;
        current.next = current.next.next;
        current.next.previous = current;
        currentPos -= 1;
        size -= 1;
    }

    public Text get(int index) {
        Node tracker = first;
        int counter = index;
        while (tracker != sentinel && counter > 0) {
            tracker = tracker.next;
            counter -= 1;
        }
        if (counter == 0) {
            return tracker.text;
        } else {
            throw new IndexOutOfBoundsException("Index out of bound.");
        }
    }

    /* For testing purposes */
    public void printDeque() {
        int counter = 0;
        while (counter < size) {
            System.out.print(get(counter).getText() + " ");
            counter += 1;
        }
    }
    public Node sentinel() {
        return sentinel;
    }

    public void changeFontSize(int size) {
        Node placeholder = tracker;
        setTracker(first);
        while (!tracker().equals(sentinel())) {
            tracker().text.setFont(Font.font(font, size));
            incrementTracker();
        }
        tracker = placeholder;
    }
}