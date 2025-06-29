package com.github.scoliossis.utils;

import com.github.scoliossis.Main;
import com.github.scoliossis.events.Bus;
import com.github.scoliossis.events.impl.FileDroppedEvent;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;

// todo bugs:
//  there is 2 windows when your fullscreened, lwk kinda need because u cant drag into the fullscreen.
//  when unfullscreening the screen has little issues until rescaled.
//  when launched with optifine it has a ugly border.
//  probably has plenty more compatability issues
public class FrameUtil {
    public static JFrame frame;
    public static boolean initialized = false;

    // stupid   G89HoergguiogghoHUG,
    // this probably has 99 problems and they are NOT all bitches.
    // but i want to drag and drop files.
    public static void init() {
        if (Main.optifineInstalled) {
            System.out.println("Not creating JFrame because optifine is evil.");
            return;
        }

        System.out.println("Creating JFrame for Minecraft...");

        try {
            Canvas canvas = new Canvas();

            frame = new JFrame(Main.MOD_NAME + " | " + Main.MOD_VERSION);
            frame.setSize(Display.getWidth(), Display.getHeight());
            frame.setLocation(Display.getX(), Display.getY());
            frame.setResizable(false);

            canvas.setBackground(Color.BLACK);
            canvas.setSize(frame.getWidth(), frame.getHeight());
            canvas.setBounds(0, 0, frame.getWidth(), frame.getHeight());
            canvas.setIgnoreRepaint(true);

            frame.add(canvas);

            addFileDropListener();

            Image image = ImageIO.read(Main.class.getResourceAsStream("/icon.png"));
            frame.setIconImage(image);

            frame.setVisible(true);

            Display.setParent(canvas);

            System.out.println(Main.MOD_NAME + " has successfully created a JFrame.");
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addFileDropListener() {
        frame.setDropTarget(new DropTarget() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    Bus.post(new FileDroppedEvent(droppedFiles));
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    System.err.println("Failed to handle file drop: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
