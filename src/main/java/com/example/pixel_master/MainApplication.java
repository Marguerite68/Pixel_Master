package com.example.pixel_master;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainApplication extends Application {
    private Label infoLabel = new Label("请选择一个文件夹");

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image("file:src/main/resources/image/Pixel Master icon.png"));//设置图标
        stage.setResizable(false);//禁用窗口缩放


        BorderPane root = new BorderPane();

        // 目录树
        TreeView<String> directoryTree = createDirectoryTree();
        directoryTree.setPrefWidth(250);  // 目录树宽度

        // 图片预览
        FlowPane imagePreviewPane = createImagePreviewPane();
        ScrollPane scrollPane = new ScrollPane(imagePreviewPane);
        scrollPane.setFitToWidth(true);

        // 操作按钮
        HBox controlPanel = createControlPanel();
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setSpacing(15);

        // 信息提示栏
        VBox bottomPanel = new VBox(infoLabel, controlPanel);
        bottomPanel.setSpacing(10);
        bottomPanel.setAlignment(Pos.CENTER);

        // 设置布局
        root.setLeft(directoryTree);
        root.setCenter(scrollPane);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add("file:src/main/resources/style/style.css");
        stage.setTitle("Pixel Master");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 创建左侧目录树
     */
    private TreeView<String> createDirectoryTree() {
        TreeItem<String> rootItem = new TreeItem<>("我的电脑");
        rootItem.setExpanded(true);

        // 示例文件夹
        TreeItem<String> picturesFolder = new TreeItem<>("图片");
        TreeItem<String> downloadsFolder = new TreeItem<>("下载");
        rootItem.getChildren().addAll(picturesFolder, downloadsFolder);

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setPrefWidth(200);

        // 监听目录点击事件
        treeView.setOnMouseClicked(event -> {
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                infoLabel.setText("当前选中目录：" + selectedItem.getValue());
                // TODO: 这里调用 B 模块的方法，加载该目录下的图片
            }
        });
        return treeView;
    }

    /**
     * 创建图片预览区域（右侧）
     */
    private FlowPane createImagePreviewPane() {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);

        // 示例图片
        List<String> sampleImages = List.of("example1.jpg", "example2.jpg", "example3.jpg");
        for (String img : sampleImages) {
            ImageView imageView = new ImageView(new Image("file:" + img));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setId(img);

            // 绑定右键菜单
            ContextMenu contextMenu = createContextMenu(imageView);
            imageView.setOnContextMenuRequested(event ->
                    contextMenu.show(imageView, event.getScreenX(), event.getScreenY())
            );

            // 监听点击图片，更新信息提示栏
            imageView.setOnMouseClicked(event ->
                    infoLabel.setText("选中的图片：" + imageView.getId())
            );

            flowPane.getChildren().add(imageView);
        }
        return flowPane;
    }

    /**
     * 创建右键菜单（适用于图片）
     */
    private ContextMenu createContextMenu(ImageView imageView) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("删除");
        MenuItem renameItem = new MenuItem("重命名");

        deleteItem.setOnAction(e -> System.out.println("删除图片：" + imageView.getId()));
        renameItem.setOnAction(e -> System.out.println("重命名图片：" + imageView.getId()));

        contextMenu.getItems().addAll(deleteItem, renameItem);
        return contextMenu;
    }

    /**
     * 创建底部操作按钮
     */
    private HBox createControlPanel() {
        Button deleteButton = new Button("删除");
        Button copyButton = new Button("复制");
        Button pasteButton = new Button("粘贴");
        Button slideModeButton = new Button("幻灯片模式");

        // TODO: 绑定实际功能
        deleteButton.setOnAction(e -> System.out.println("点击了删除按钮"));
        copyButton.setOnAction(e -> System.out.println("点击了复制按钮"));
        pasteButton.setOnAction(e -> System.out.println("点击了粘贴按钮"));

        return new HBox(10, deleteButton, copyButton, pasteButton);
    }

    public static void main(String[] args) {
        launch();
    }
}
