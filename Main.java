import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
        Button btn = new Button();
        btn.setText("Send \"hello client!\" to client");
        btn.setOnAction((ActionEvent e) -> {
            server.sendMessage();
            server.sendSecondMessage();
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 400, 400);

        primaryStage.setTitle("HypED Base Station");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
