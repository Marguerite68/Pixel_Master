package com.example.pixel_master;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApplication extends Application {
    private Label pgmInfoLabel = new Label("欢迎来到 Pixel Master，请选择一个文件夹");
    private Label fileInfoLabel = new Label("当前未选中图片");
    private final Label versionLabel = new Label("Version: 0.1.6 beta");
    private final ScrollPane imageScrollPane = new ScrollPane();

    private TreeController treeController;
    private ImageController imageController;

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image("file:src/main/resources/image/Pixel Master icon.png"));
        stage.setResizable(true);

        Pane root = new Pane();

        treeController = new TreeController(fileInfoLabel, pgmInfoLabel, imageScrollPane);
        TreeView<String> directoryTree = treeController.getTreeView();
        directoryTree.setPrefWidth(265);
        directoryTree.setPrefHeight(600);

        imageScrollPane.setPrefSize(930, 600);

        imageController = new ImageController(imageScrollPane, fileInfoLabel, pgmInfoLabel);
        HBox controlPanel = createControlPanel(imageController);

        pgmInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
        fileInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        directoryTree.setLayoutX(25);
        directoryTree.setLayoutY(50);

        imageScrollPane.setLayoutX(320);
        imageScrollPane.setLayoutY(50);

        pgmInfoLabel.setLayoutX(320);
        pgmInfoLabel.setLayoutY(20);
        fileInfoLabel.setLayoutX(5);
        fileInfoLabel.setLayoutY(700);
        versionLabel.setLayoutX(1165);
        versionLabel.setLayoutY(703);

        controlPanel.setLayoutX(500);
        controlPanel.setLayoutY(664);

        root.getChildren().addAll(
                directoryTree,
                imageScrollPane,
                pgmInfoLabel,
                versionLabel,
                fileInfoLabel,
                controlPanel);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add("file:src/main/resources/style/style.css");
        stage.setTitle("Pixel Master");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createControlPanel(ImageController imageController) {
        Button deleteButton = new Button("删除");
        Button copyButton = new Button("复制");
        Button pasteButton = new Button("粘贴");
        Button slideModeButton = new Button("幻灯片");

        // 使用 ImageController 来设置按钮的事件处理程序
        imageController.setButtonActions(deleteButton, copyButton, pasteButton, slideModeButton);

        return new HBox(10, deleteButton, copyButton, pasteButton, slideModeButton);
    }

    public static void main(String[] args) {
        launch();
    }
}
