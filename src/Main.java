import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private Client client = new Client();
    private TextField serverIp;
    private TextField serverPort;
    private TextField filePathSend;
    private TextField fileNameReceive;
    private TextField fileNameSend;
    private TextField directoryReceive;
    private TextField directorySend;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {

        String url = "127.0.0.1";
        System.out.println(client.connect(url, 8080) ? "Connecté au serveur" : "Pas connecté");

        primaryStage.setTitle("Client HTTP");
        Group root = new Group();
        Scene scene = new Scene(root, 400, 250, Color.WHITE);

        // create the tabs
        TabPane tabPane = new TabPane();

        // send tab
        Tab tabSend = new Tab();
        tabSend.setText("Envoyer un fichier");
        VBox sendBox = new VBox();

        // file name
        sendBox.getChildren().add(new Label("Nom du fichier sur le serveur : "));
        fileNameSend = new TextField();
        sendBox.getChildren().add(fileNameSend);

        // file selection
        sendBox.getChildren().add(new Label("Chemin du fichier à envoyer :"));
        HBox selectSend = new HBox();
        filePathSend = new TextField();
        selectSend.getChildren().add(filePathSend);
        Button btnSelectFileSend = new Button("Selectionner");
        btnSelectFileSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null)
                    filePathSend.setText(file.getAbsolutePath());
            }
        });
        selectSend.getChildren().add(btnSelectFileSend);
        sendBox.getChildren().add(selectSend);

        // send button
        Button btnSend = new Button("Envoyer le fichier");
        btnSend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    int result = client.putFile(filePathSend.getText(), fileNameSend.getText());

                    /*final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    dialogVbox.setAlignment(Pos.CENTER);
                    Scene dialogScene = new Scene(dialogVbox, 250, 50);
                    dialog.setScene(dialogScene);*/

                    switch(result) {
                        case 0:
                            System.out.println("Page correctement téléchargée");
                            break;

                        case 1:
                            System.out.println("Erreur lors de la connexion avec le serveur");
                            break;

                        case 5:
                            System.out.println("Problème lors de la création du fichier");
                            break;

                        default:
                            System.out.println("Erreur du serveur : " + result);
                            break;
                    }
                    //dialog.show();

                } catch (NumberFormatException e) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    dialogVbox.getChildren().add(new Label("Numéro de port invalide"));
                    dialogVbox.setAlignment(Pos.CENTER);
                    Scene dialogScene = new Scene(dialogVbox, 250, 50);
                    dialog.setScene(dialogScene);
                    dialog.show();
                }
            }
        });
        sendBox.getChildren().add(btnSend);
        tabSend.setContent(sendBox);
        tabPane.getTabs().add(tabSend);

        // receive file tab
        Tab tabReceive = new Tab();
        tabReceive.setText("Recevoir un fichier");
        VBox receiveFileBox = new VBox();
        // file name
        receiveFileBox.getChildren().add(new Label("Nom du fichier désiré : "));
        fileNameReceive = new TextField();
        receiveFileBox.getChildren().add(fileNameReceive);
        // directory
        receiveFileBox.getChildren().add(new Label("Répertoire de téléchargement : "));
        HBox selectDirectory2 = new HBox();
        directoryReceive = new TextField();
        selectDirectory2.getChildren().add(directoryReceive);
        Button btnSelectDir2 = new Button("Selectionner");
        btnSelectDir2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                DirectoryChooser chooser = new DirectoryChooser();
                File selectedDirectory2 = chooser.showDialog(primaryStage);
                if (selectedDirectory2 != null)
                    directoryReceive.setText(selectedDirectory2.getAbsolutePath());
            }
        });
        selectDirectory2.getChildren().add(btnSelectDir2);
        receiveFileBox.getChildren().add(selectDirectory2);
        tabReceive.setContent(receiveFileBox);
        // receive
        Button receive = new Button("Recevoir le fichier");
        receive.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    int result = client.getPage(directoryReceive.getText(), fileNameReceive.getText());

                    /*final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    dialogVbox.setAlignment(Pos.CENTER);
                    Scene dialogScene = new Scene(dialogVbox, 350, 50);
                    dialog.setScene(dialogScene);*/

                    switch(result) {
                        case 0:
                            System.out.println("Page correctement téléchargée");
                            break;

                        case 1:
                            System.out.println("Erreur lors de la connexion avec le serveur");
                            break;

                        case 2:
                            System.out.println("Réponse non valide");
                            break;

                        case 3:
                            System.out.println("Aucune donnée");
                            break;

                        case 4:
                            System.out.println("Erreur 404: Fichier inexistant");
                            break;

                        case 5:
                            System.out.println("Problème lors de la création du fichier");
                            break;

                        default:
                            System.out.println("Erreur du serveur : " + result);
                            break;
                    }
                    //dialog.show();

                } catch (NumberFormatException e) {
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(primaryStage);
                    VBox dialogVbox = new VBox(20);
                    dialogVbox.getChildren().add(new Label("Numéro de port invalide"));
                    dialogVbox.setAlignment(Pos.CENTER);
                    Scene dialogScene = new Scene(dialogVbox, 250, 50);
                    dialog.setScene(dialogScene);
                    dialog.show();
                }
            }
        });
        receiveFileBox.getChildren().add(receive);
        tabReceive.setContent(receiveFileBox);
        tabPane.getTabs().add(tabReceive);


        // set tabs to scene and stage
        BorderPane borderPane = new BorderPane();
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        //borderPane.setTop(serverPane);
        borderPane.setCenter(tabPane);
        root.getChildren().add(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
