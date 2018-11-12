import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
        Button btn1 = new Button();
        btn1.setText("Send \"message 1\"");
        btn1.setOnAction((ActionEvent e) -> {
            server.sendMessage("message 1");
        });

        Button btn2 = new Button();
        btn2.setText("Send \"message 2\"");
        btn2.setOnAction((ActionEvent e) -> {
            server.sendMessage("message 2");
        });

        VBox vbox = new VBox(10, btn1, btn2);
        vbox.setAlignment(Pos.CENTER);

        StackPane root = new StackPane();
        root.getChildren().add(vbox);

        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("HypED Base Station");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
