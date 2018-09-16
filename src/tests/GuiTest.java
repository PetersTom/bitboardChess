package tests;

import GUI.Gui;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class GuiTest {

    @Test
    void getSquare() {
        Point p = new Point(200, 700);
        assertEquals(3, Gui.getSquare(p));
        p = new Point( 443, 236);
        assertEquals(45, Gui.getSquare(p));
    }

    @Test
    void getGraphicalPoint() {
        int x = 4;
        assertEquals(new Point(300, 700), Gui.getGraphicalPosition(x));
        x = 17;
        assertEquals(new Point(0, 500), Gui.getGraphicalPosition(x));
    }

}