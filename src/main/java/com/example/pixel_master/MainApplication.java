package com.example.pixel_master;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;

public class MainApplication extends Application {
    private Label pgmInfoLabel = new Label("欢迎来到 Pixel Master，请选择一个文件夹");
    private Label fileInfoLabel = new Label("当前未选中图片");
    private final Label versionLabel = new Label("Version: 0.1.3 beta");
    private final Image diskIcon = new Image("file:src/main/resources/image/disk.png"); // 磁盘图标
    private final Image folderIcon = new Image("file:src/main/resources/image/folder.png"); // 文件夹图标
    private final Image imageIcon = new Image("file:src/main/resources/image/image.png"); // 图片图标
    private final Image PCIcon = new Image("file:src/main/resources/image/PC.png"); // 电脑图标

    /**
     *  pgmInfoLabel:   显示当前选中目录的路径,程序运行状态等信息
     *  fileInfoLabel:  显示当前选中图片的数量,大小等信息 TODO:随着选中图片实时更新该标签内容
     *  versionLabel:   显示程序版本号
     */

    //----------主窗口----------
    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image("file:src/main/resources/image/Pixel Master icon.png")); // 设置窗口图标
        stage.setResizable(false); // 禁用窗口缩放

        Pane root = new Pane();

        //----------创建组件----------
        TreeView<String> directoryTree = createDirectoryTree(); // 目录树
        directoryTree.setPrefWidth(265);
        directoryTree.setPrefHeight(600);

        ScrollPane imageScrollPane = new ScrollPane(createImagePreviewPane()); // 图片预览区
        imageScrollPane.setPrefSize(930, 600);

        HBox controlPanel = createControlPanel(); // 操作按钮区

        //----------文本显示样式----------
        pgmInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
        fileInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        //----------绝对定位每个组件----------
        //目录树定位
        directoryTree.setLayoutX(25);
        directoryTree.setLayoutY(50);

        //图片预览区定位
        imageScrollPane.setLayoutX(320);
        imageScrollPane.setLayoutY(50);

        //文本框定位
        pgmInfoLabel.setLayoutX(320);
        pgmInfoLabel.setLayoutY(20);
        fileInfoLabel.setLayoutX(5);
        fileInfoLabel.setLayoutY(700);
        versionLabel.setLayoutX(1165);
        versionLabel.setLayoutY(703);

        //操作块按钮定位
        controlPanel.setLayoutX(500);
        controlPanel.setLayoutY(664);

        //----------添加组件到 Pane----------
        root.getChildren().addAll(
                directoryTree,
                imageScrollPane,
                pgmInfoLabel,
                versionLabel,
                fileInfoLabel,
                controlPanel);

        //----------创建窗口----------
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add("file:src/main/resources/style/style.css");
        stage.setTitle("Pixel Master");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 创建左侧目录树，并为磁盘和文件夹添加图标
     */
    private TreeView<String> createDirectoryTree() {
        TreeItem<String> rootItem = createTreeItem("我的电脑", PCIcon, null);
        rootItem.setExpanded(true);

        // 获取所有磁盘
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                TreeItem<String> diskItem = createTreeItem(root.toString(), diskIcon, root);
                rootItem.getChildren().add(diskItem);
                addSubdirectories(diskItem, root); // 只加载一级子目录
            }
        }

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setPrefWidth(250);

        // 监听目录点击事件，更新信息
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                File file = (File) newItem.getGraphic().getUserData();
                pgmInfoLabel.setText("当前选中目录：" + (file != null ? file.getAbsolutePath() : newItem.getValue()));
            }
        });

        return treeView;
    }

    /**
     * 只加载当前文件夹下的一级子文件夹，并排除 $RECYCLE.BIN
     */
    private void addSubdirectories(TreeItem<String> parentItem, File folder) {
        File[] files = folder.listFiles(file -> file.isDirectory() && !file.getName().equalsIgnoreCase("$RECYCLE.BIN"));
        if (files != null) {
            for (File file : files) {
                TreeItem<String> dirItem = createTreeItem(file.getName(), folderIcon, file);
                parentItem.getChildren().add(dirItem);
                if (hasSubdirectories(file)) {
                    dirItem.getChildren().add(new TreeItem<>("加载中..."));
                }

                // ⭐ 监听展开事件，点击展开按钮时自动加载子目录
                dirItem.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                    if (isNowExpanded) {
                        loadSubdirectoriesAsync(dirItem);
                    }
                });
            }
        }
    }

    /**
     * 创建一个带有图标的 TreeItem，并确保图标在名称前面
     */
    private TreeItem<String> createTreeItem(String name, Image icon, File file) {
        ImageView imageView = (icon != null) ? new ImageView(icon) : null;
        if (imageView != null) {
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setUserData(file); // 存储文件对象
        }

        TreeItem<String> item = new TreeItem<>(name, imageView);

        // 添加 Tooltip，显示完整路径
        if (file != null) {
            Tooltip tooltip = new Tooltip(file.getAbsolutePath());
            Tooltip.install(imageView, tooltip);
        }

        return item;
    }

    /**
     * 检查文件夹是否有子目录或图片
     */
    private boolean hasSubdirectories(File folder) {
        File[] files = folder.listFiles(file -> file.isDirectory() || isImageFile(file));
        return files != null && files.length > 0;
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                || fileName.endsWith(".bmp") || fileName.endsWith(".gif");
    }

    /**
     * 异步加载子目录，避免 UI 卡顿
     */
    private void loadSubdirectoriesAsync(TreeItem<String> parentItem) {
        if (parentItem.getChildren().isEmpty()) return;

        // 获取文件路径
        File parentFolder = (File) parentItem.getGraphic().getUserData();
        if (parentFolder == null || !parentFolder.exists() || !parentFolder.isDirectory()) return;

        // 避免重复加载
        if (!"加载中...".equals(parentItem.getChildren().get(0).getValue())) {
            return;
        }

        Service<Void> service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        File[] subDirs = parentFolder.listFiles(file -> file.isDirectory() && !file.getName().equalsIgnoreCase("$RECYCLE.BIN"));
                        if (subDirs == null) return null;

                        Platform.runLater(() -> {
                            parentItem.getChildren().clear(); // 清除 "加载中..."
                            for (File file : subDirs) {
                                TreeItem<String> dirItem = createTreeItem(file.getName(), folderIcon, file);
                                parentItem.getChildren().add(dirItem);
                                if (hasSubdirectories(file)) {
                                    dirItem.getChildren().add(new TreeItem<>("加载中..."));
                                }

                                // ⭐ 监听展开事件
                                dirItem.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                                    if (isNowExpanded) {
                                        loadSubdirectoriesAsync(dirItem);
                                    }
                                });
                            }
                        });
                        return null;
                    }
                };
            }
        };

        service.start();
    }






    //----------创建图片预览区域----------
    private Pane createImagePreviewPane() {
        Pane flowPane = new Pane();
        //TODO从当前选中目录提取图片文件，显示在右侧区域
        return flowPane;
    }


    //----------创建右键菜单(Untested)----------
    private ContextMenu createContextMenu(ImageView imageView) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("删除");
        MenuItem renameItem = new MenuItem("重命名");

        deleteItem.setOnAction(e -> System.out.println("删除图片：" + imageView.getId()));
        renameItem.setOnAction(e -> System.out.println("重命名图片：" + imageView.getId()));

        contextMenu.getItems().addAll(deleteItem, renameItem);
        return contextMenu;
    }


    //----------创建底部操作按钮----------
    private HBox createControlPanel() {
        Button deleteButton = new Button("删除");
        Button copyButton = new Button("复制");
        Button pasteButton = new Button("粘贴");
        Button slideModeButton = new Button("幻灯片");
        //TODO：以下文字提示仅供测试，实际开发时ActionEvent需绑定其他功能
        deleteButton.setOnAction(e -> System.out.println("点击了删除按钮"));
        copyButton.setOnAction(e -> System.out.println("点击了复制按钮"));
        pasteButton.setOnAction(e -> System.out.println("点击了粘贴按钮"));
        slideModeButton.setOnAction(e -> System.out.println("点击了幻灯片按钮"));

        return new HBox(10, deleteButton, copyButton, pasteButton, slideModeButton);
    }

    //----------启动程序----------
    public static void main(String[] args) {
        launch();
    }
}
