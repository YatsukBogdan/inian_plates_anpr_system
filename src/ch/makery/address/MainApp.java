package ch.makery.address;

import javafx.event.ActionEvent;

import java.io.BufferedReader;
import java.io.File;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MainApp extends Application {

    public static Connection conn;
    public static Statement stmt;

    private Stage primaryStage;
    private AnchorPane rootLayout;

    @FXML
    private Image car_image;
    private File car_file;

    @FXML
    private ProgressIndicator indicator;
    @FXML
    private ProgressIndicator indicator_db;

    @FXML
    private Button proceed_button;

    @FXML
    private Button write_to_db;

    @FXML
    private TextField number_text;

    @FXML
    private ImageView car_image_view;

    @FXML
    private TextFlow plate_not_found_text;
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Number recognition");
        initRootLayout();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/recognizing_interface.fxml"));
            rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openFileChooser(ActionEvent event){
        File tmp_car_file = car_file;

        FileChooser fileChooser = new FileChooser();
        try {
            car_file = fileChooser.showOpenDialog(primaryStage);
            car_image = new Image(car_file.toURI().toString());
        }
        catch (NullPointerException e){
            car_file = tmp_car_file;
            car_image = new Image(car_file.toURI().toString());
        }
        car_image_view.setImage(car_image);
        indicator.visibleProperty().setValue(false);
        indicator_db.visibleProperty().setValue(false);
        proceed_button.setDisable(false);
        number_text.setText("");
    }

    @FXML
    protected void proceedImage(ActionEvent event){
        try {
            indicator.visibleProperty().setValue(true);
            indicator.setProgress(-1.0f);

            Process process = Runtime.getRuntime().exec("alpr -c in " + car_file.getAbsolutePath());
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            int i = 0;
            while((s = stdInput.readLine()) != null){
                if (i == 1) {
                    String [] splited = s.split("\\s+");
                    number_text.setAlignment(Pos.BASELINE_CENTER);
                    if (splited[2].compareTo("") == 0){}
                    number_text.setText(splited[2]);
                    break;
                }
                i++;
            }
            indicator.setProgress(1.0f);
            proceed_button.setDisable(true);
            write_to_db.setDisable(false);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    protected void writeToDatabase(ActionEvent event){
        try {
            indicator_db.visibleProperty().setValue(true);
            indicator_db.setProgress(-1.0f);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/plates?user=root&password=1111");

            String plate_number_rec = number_text.getText();

            stmt = conn.createStatement();
            stmt.executeUpdate("insert into plates.numbers (plate_number) value ('" + plate_number_rec +"')");

            stmt.close();
            conn.close();
            indicator_db.setProgress(1.0f);
            // Do something with the Connection
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        catch (InstantiationException e){
            e.printStackTrace();
        }
        catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}