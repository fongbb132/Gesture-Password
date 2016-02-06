package sample;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Main extends Application {
    static double height = 800;
    static double width = 1200;
    static int radius = 100;
    long timeDraw = 0;
    public static boolean isDraw = false;
    static Canvas canvas;
    GraphicsContext gc;
    static int[] input = new int[4];
    @Override
    public void start(Stage primaryStage) throws Exception{
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        primaryStage.setTitle("Hello World");

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        LeapThread leapThread = new LeapThread(gc);
        leapThread.start();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        width = bounds.getWidth();
        primaryStage.setWidth(bounds.getWidth());
        height = bounds.getHeight();
        primaryStage.setHeight(bounds.getHeight());
        VBox hbox = new VBox();

        hbox.getChildren().add(canvas);
        Runnable a = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (isDraw) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                gc.setStroke(Color.BLUE);
                                gc.stroke();
                                gc.strokeOval(Main.width / 2 - 100, Main.height / 2 - 100, 200, 200);
                                isDraw = false;
                            }
                        });
                    }
                }
            }
        };
        Thread drawingThread = new Thread(a);
        drawingThread.start();

        primaryStage.setScene(new Scene(hbox, 1200, 800));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

}

class LeapThread extends Thread{
    GraphicsContext gc;
    public LeapThread(GraphicsContext gc) {
        this.gc = gc;
    }

    @Override
    public void run() {
        super.run();
        SampleListener listener = new SampleListener(gc);
        Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}

class SampleListener extends Listener {
    GraphicsContext gc;
    int numFrames = 0;
    boolean tap = false;
    ArrayList<Finger> preRightFingers;

    public SampleListener(GraphicsContext gc) {
        this.gc = gc;
    }

    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
        controller.config().setFloat("Gesture.KeyTap.MinDownVelocity", 40.0f);
        controller.config().setFloat("Gesture.KeyTap.HistorySeconds", .2f);
        controller.config().setFloat("Gesture.KeyTap.MinDistance", 5.0f);
        controller.config().save();
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
        numFrames ++;
        if(numFrames%3==1) {
            Frame frame = controller.frame();
            GestureList gestures = frame.gestures();
            for (int i = 0; i < gestures.count(); i++) {
                Gesture gesture = gestures.get(i);
                int num = 0;
                switch (gesture.type()) {
                    case TYPE_CIRCLE:
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                gc.setStroke(Color.BLUE);
                                gc.stroke();
                                gc.strokeOval(Main.width / 2 - 100, Main.height / 2 - 100, 200, 200);
                            }
                        });
                        try {
                            Thread.sleep((long)500.0);
                        }catch (InterruptedException e){

                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                gc.clearRect(0,0,Main.width, Main.height);
                            }
                        });

                        break;

                    case TYPE_KEY_TAP:
                        num++;
                        KeyTapGesture keyTap = new KeyTapGesture(gesture);
                        System.out.println("  Key Tap id: " + keyTap.id()
                                + ", " + keyTap.state()
                                + ", position: " + keyTap.position()
                                + ", direction: " + keyTap.direction());

                        for (Hand hand : frame.hands()) {
                            System.out.println(num);
                            if (hand.isLeft()) {
                                Pointable tappingPointable = keyTap.pointable();
                                if (tappingPointable.isFinger()) {
                                    Finger tappingFinger = new Finger(tappingPointable);
                                    System.out.println("Left: " + tappingFinger.type());
                                }
                            } else {
                                Pointable tappingPointable = keyTap.pointable();
                                if (tappingPointable.isFinger()) {
                                    Finger tappingFinger = new Finger(tappingPointable);
                                    System.out.println("Right: " + tappingFinger.type());

                                    switch(tappingFinger.type()){
                                        case TYPE_THUMB:
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    javafx.scene.image.Image img = new Image("Thumb.png");
                                                    gc.drawImage(img, 0, 0);
                                                }
                                            });

                                            try {
                                                Thread.sleep((long)500.0);
                                            }catch (InterruptedException e){

                                            }
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gc.clearRect(0,0,Main.width, Main.height);
                                                }
                                            });

                                            break;
                                        case TYPE_INDEX:
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    javafx.scene.image.Image img= new Image("Index.png");
                                                    gc.drawImage(img,0,0);
                                                }
                                            });
                                            try {
                                                Thread.sleep((long) 500.0);
                                            } catch (InterruptedException e) {

                                            }
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gc.clearRect(0,0,Main.width, Main.height);
                                                }
                                            });

                                            break;

                                        case TYPE_MIDDLE:
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    javafx.scene.image.Image img= new Image("Middle.png");
                                                    gc.drawImage(img, 0, 0);
                                                }
                                            });
                                            try {
                                                Thread.sleep((long) 500.0);
                                            } catch (InterruptedException e) {

                                            }
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gc.clearRect(0,0,Main.width, Main.height);
                                                }
                                            });

                                            break;
                                        case TYPE_RING:
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    javafx.scene.image.Image img= new Image("Ring.png");
                                                    gc.drawImage(img, 0, 0);
                                                }
                                            });
                                            try {
                                                Thread.sleep((long) 500.0);
                                            } catch (InterruptedException e) {

                                            }
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    gc.clearRect(0,0,Main.width, Main.height);
                                                }
                                            });

                                            break;
                                    }
                                }

                            }
                        }
                        break;
                    default:
                        System.out.println("Unknown gesture type.");
                        break;
                }
            }


            if (!frame.hands().isEmpty() || !gestures.isEmpty()) {
                //System.out.println();
            }
        }
    }
}
