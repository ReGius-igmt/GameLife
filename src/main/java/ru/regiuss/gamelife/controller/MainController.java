package ru.regiuss.gamelife.controller;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import ru.regiuss.gamelife.App;
import ru.regiuss.gamelife.task.GameTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static ru.regiuss.gamelife.util.Environment.*;

public class MainController implements Initializable {

    @FXML
    private Text ageField;

    @FXML
    private Canvas canvas;

    @FXML
    private Slider speedSlider;

    @FXML
    private Button startBtn;

    @FXML
    private Slider zoomSlider;

    private GraphicsContext gc;
    private GameTask gt;
    private Future<?> ft;
    private int[][] matrix;
    private ExecutorService es;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        es = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setName("GameThread");
            return t;
        });
        speedSlider.valueProperty().addListener((observableValue, number, t1) -> {
            if(gt != null)gt.setDelay(100 - t1.longValue());
        });
        zoomSlider.valueProperty().addListener((observableValue, number, t1) -> {
            CELL = t1.intValue();
            canvas.setWidth(CELL * SIZE);
            canvas.setHeight(CELL * SIZE);
            gc.clearRect(0, 0, CELL * SIZE, CELL * SIZE);
        });
        startBtn.setOnAction(event -> {
            if(gt == null)start();
            else stop();
        });

        matrix = new int[SIZE][SIZE];
        gc = canvas.getGraphicsContext2D();
        EventHandler<MouseEvent> drawEvent = event -> {
            if(event.getX() >= SIZE * (CELL + 2) || event.getY() >= SIZE * (CELL + 2) || event.getX() < 0 || event.getY() < 0)return;
            int i = (int) (event.getX() / (CELL + 2));
            int j = (int) (event.getY() / (CELL + 2));
            synchronized (matrix){
                if(matrix[i][j] == (3 - event.getButton().ordinal()) / 2)return;
                matrix[i][j] = (3 - event.getButton().ordinal()) / 2;
            }
        };
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, drawEvent);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, drawEvent);
        matrix = new int[SIZE][SIZE];

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                synchronized (matrix){
                    if(gt != null)ageField.setText(Integer.toString(gt.getAge()));
                    for (int i = 0; i < SIZE; i++) {
                        for (int j = 0; j < SIZE; j++) {
                            gc.setFill(matrix[i][j] == 1 ? PRIMARY : BASIC);
                            gc.fillRect(i*(CELL + 2), j*(CELL + 2), CELL, CELL);
                        }
                    }
                }
            }
        }.start();
    }

    private void start(){
        startBtn.setText("Pause");
        gt = new GameTask(matrix);
        gt.setDelay((long) (100 - speedSlider.getValue()));
        ft = es.submit(gt);
    }

    private void stop(){
        startBtn.setText("Start");
        if(gt != null){
            gt.setAlive(false);
            gt = null;
            ft.cancel(true);
        }
    }


    @FXML
    void onClear(ActionEvent event) {
        stop();
        synchronized (matrix){
            for(int[] a : matrix){
                for (int i = 0; i < SIZE; i++) {
                    a[i] = 0;
                }
            }
        }
    }

    @FXML
    void onLoad(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File initial = new File("./saves");
        initial.mkdirs();
        chooser.setInitialDirectory(initial);
        File f = chooser.showOpenDialog(App.ps);
        if(f == null)return;
        stop();
        synchronized (matrix){
            try(FileInputStream os = new FileInputStream(f)){
                SIZE = os.read();
                matrix = new int[SIZE][SIZE];
                for(int[] arr : matrix){
                    for (int i = 0; i < SIZE; i++) {
                        arr[i] = os.read();
                    }
                }
                new Alert(Alert.AlertType.INFORMATION, "Success Load").show();
            } catch (Exception e){
                new Alert(Alert.AlertType.ERROR, "Error Load").show();
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onSave(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File initial = new File("./saves");
        initial.mkdirs();
        chooser.setInitialDirectory(initial);
        File f = chooser.showSaveDialog(App.ps);
        if(f == null)return;
        stop();
        synchronized (matrix){
            try(FileOutputStream os = new FileOutputStream(f)){
                os.write(SIZE);
                for(int[] arr : matrix){
                    for(int a : arr){
                        os.write(a);
                    }
                }
                new Alert(Alert.AlertType.INFORMATION, "Success Save").show();
            } catch (Exception e){
                new Alert(Alert.AlertType.ERROR, "Error Save").show();
                e.printStackTrace();
            }
        }
    }
}
