import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Main extends Application {
    public static Server server = null;

    public static void main(String[] args) {
        server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();

        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // button 1
        Button btn1 = new Button();
        btn1.setText("Send \"message 1\"");
        btn1.setOnAction((ActionEvent e) -> {
            server.sendMessage("message 1");
        });

        // button 2
        Button btn2 = new Button();
        btn2.setText("Send \"message 2\"");
        btn2.setOnAction((ActionEvent e) -> {
            server.sendMessage("message 2");
        });

        // velocity label
        Label velocityLabel = new Label("VELOCITY");
        Task velocityTask = new Task<Void>() {
            @Override
            protected Void call() {
                while (true) {
                    updateMessage(Main.server.getVelocityProperty().getValue());
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        velocityLabel.textProperty().bind(velocityTask.messageProperty());
        new Thread(velocityTask).start();

        // acceleration label
        Label accelerationLabel = new Label("ACCELERATION");
        Task accelerationTask = new Task<Void>() {
            @Override
            protected Void call() {
                while (true) {
                    updateMessage(Main.server.getAccelerationProperty().getValue());
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        accelerationLabel.textProperty().bind(accelerationTask.messageProperty());
        new Thread(accelerationTask).start();

        // brakeTemp label
        Label brakeTempLabel = new Label("BRAKETEMP");
        Task brakeTempTask = new Task<Void>() {
            @Override
            protected Void call() {
                while (true) {
                    updateMessage(Main.server.getBrakeTempProperty().getValue());
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
                return null;
            }
        };
        brakeTempLabel.textProperty().bind(brakeTempTask.messageProperty());
        new Thread(brakeTempTask).start();

        // hbox with above labels
        HBox dataHBox = new HBox(20, velocityLabel, accelerationLabel, brakeTempLabel);
        dataHBox.setAlignment(Pos.CENTER);

        Label velocityText = new Label("Velocity");
        Label accelerationText = new Label("Acceleration");
        Label brakeTempText = new Label("Brake Temp");
        HBox textHBox = new HBox(10, velocityText, accelerationText, brakeTempText);
        textHBox.setAlignment(Pos.CENTER);

        // vbox
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(btn1, btn2, textHBox, dataHBox);
        vbox.setAlignment(Pos.CENTER);

        // stackpane
        StackPane root = new StackPane();
        root.getChildren().add(vbox);

        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("HypED Base Station");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
