package com.example.pixel_master;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApplication extends Application {
    private Label pgmInfoLabel = new Label("欢迎来到 Pixel Master，请选择一个文件夹");// 用于显示当前选中的目录路径
    private Label fileInfoLabel = new Label("当前未选中图片");// 用于显示选中文件的信息
    private final Label versionLabel = new Label("Version: 1.0.0");//每次提交之前记得给版本号+1，格式为：正式版本号.功能版本号.开发版本号
    private final ScrollPane imageScrollPane = new ScrollPane();

    private TreeController treeController;
    private ImageController imageController;


    @Override
    /*
      设置主舞台并初始化UI组件。
     */
    public void start(Stage stage) {
        // 设置应用程序图标
        new Image(getClass().getResource("/image/Pixel Master icon.png").toExternalForm());
        // 禁用窗口缩放
        stage.setResizable(false);

        // 创建根面板
        Pane root = new Pane();

        // 初始化imageController
        imageController = new ImageController(imageScrollPane, fileInfoLabel, pgmInfoLabel);

        //传递fileInfoLabel和pgmInfoLabel给treeController以供实时更新
        treeController = new TreeController(fileInfoLabel, pgmInfoLabel, imageScrollPane);
        TreeView<String> directoryTree = treeController.getTreeView();
        directoryTree.setPrefWidth(265);
        directoryTree.setPrefHeight(600);

        // 设置图片滚动面板的首选大小
        imageScrollPane.setPrefSize(930, 600);

        // 初始化图像控制器并创建控制面板
        imageController = new ImageController(imageScrollPane, fileInfoLabel, pgmInfoLabel);
        HBox controlPanel = createControlPanel(imageController);

        // 设置标签的样式
        pgmInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
        fileInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        // 设置组件的布局位置
        directoryTree.setLayoutX(25);//设置目录树的位置
        directoryTree.setLayoutY(50);
        imageScrollPane.setLayoutX(320);//设置图片预览主面板的位置
        imageScrollPane.setLayoutY(50);
        pgmInfoLabel.setLayoutX(320);//设置程序信息文本的位置
        pgmInfoLabel.setLayoutY(20);
        fileInfoLabel.setLayoutX(5);//设置文件信息文本的位置
        fileInfoLabel.setLayoutY(700);
        versionLabel.setLayoutX(1165);//设置版本信息文本的位置
        versionLabel.setLayoutY(703);
        controlPanel.setLayoutX(500);//设置按钮面板的位置
        controlPanel.setLayoutY(664);

        // 将所有组件添加到根面板
        root.getChildren().addAll(
                directoryTree,
                imageScrollPane,
                pgmInfoLabel,
                versionLabel,
                fileInfoLabel,
                controlPanel
        );

        // 创建并设置场景
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setTitle("Pixel Master");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createControlPanel(ImageController imageController) {
        Button deleteButton = new Button("删除");
        Button copyButton = new Button("复制");
        Button pasteButton = new Button("粘贴");
        Button renameButton = new Button("重命名");
        Button slideModeButton = new Button("幻灯片");

        //点击slideModeButton后直接调用SlideMode的静态方法创建实例
        slideModeButton.setOnAction(event -> SlideMode.showSlideIntervalDialog());

        //其他按钮的点击事件在ImageController中处理
        imageController.setButtonActions(deleteButton, copyButton, pasteButton, slideModeButton, renameButton);

        return new HBox(10, deleteButton, copyButton, pasteButton, slideModeButton, renameButton);
    }


    public static void main(String[] args) {
        launch();
    }
}
