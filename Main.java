import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

        // label
        Label testLabel = new Label("LABEL HERE");
        Task labelTask = new Task<Void>() {
            @Override
            protected Void call() {
                while (true) {
                    updateMessage(Main.server.dataProperty().getValue());
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
        testLabel.textProperty().bind(labelTask.messageProperty());
        Thread t2 = new Thread(labelTask);
        t2.start();

        // vbox
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(btn1, btn2, testLabel);
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
