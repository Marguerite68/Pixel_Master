package com.example.pixel_master;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;

public class MainApplication extends Application {
    private Label pgmInfoLabel = new Label("欢迎来到 Pixel Master，请选择一个文件夹");
    private Label fileInfoLabel = new Label("当前未选中图片");
    private final Label versionLabel = new Label("Version: 0.1.6 beta");
    private final Image diskIcon = new Image("file:src/main/resources/image/disk.png"); // 磁盘图标
    private final Image folderIcon = new Image("file:src/main/resources/image/folder.png"); // 文件夹图标
    private final Image imageIcon = new Image("file:src/main/resources/image/pic.png"); // 图片图标
    private final Image PCIcon = new Image("file:src/main/resources/image/PC.png"); // 电脑图标

    private final ScrollPane imageScrollPane = new ScrollPane(); // 图片预览区

    /**
     *  pgmInfoLabel:   显示当前选中目录的路径,程序运行状态等信息
     *  fileInfoLabel:  显示当前选中图片的数量,大小等信息 TODO:随着选中图片实时更新该标签内容
     *  versionLabel:   显示程序版本号
     */

    //----------主窗口----------
    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image("file:src/main/resources/image/Pixel Master icon.png")); // 设置窗口图标
        stage.setResizable(true); // 禁用窗口缩放

        Pane root = new Pane();

        //----------创建组件----------
        TreeView<String> directoryTree = createDirectoryTree(); // 目录树
        directoryTree.setPrefWidth(265);
        directoryTree.setPrefHeight(600);


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
                fileInfoLabel.setText("当前未选中图片");
                pgmInfoLabel.setText("当前选中目录：" + (file != null ? file.getAbsolutePath() : newItem.getValue()));
                loadImages(newItem);
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

                       /* Platform.runLater(() -> {
                            loadImages(parentItem);
                        });*/

                        return null;
                    }
                };
            }
        };

        service.start();
    }



    private void loadImages(TreeItem<String> parentItem) {
        File parentFolder = (File) parentItem.getGraphic().getUserData();
        File[] images = parentFolder.listFiles(file -> file.isFile() && isImageFile(file));

        // 清空 imageScrollPane 原有的内容
        imageScrollPane.setContent(null);

        // 路径下没有图片的提示信息
        if (images == null || images.length == 0) {
            Label noImagesLabel = new Label("当前路径下没有可供预览的图片");
            noImagesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            noImagesLabel.setAlignment(Pos.CENTER);

            StackPane stackPane = new StackPane(noImagesLabel);
            stackPane.setPrefSize(900, 600);
            imageScrollPane.setContent(stackPane);
            return;
        }

        // 创建 TilePane 并整齐排列图片
        TilePane tilePane = new TilePane();
        tilePane.setHgap(15);  // 图片间距
        tilePane.setVgap(15);
        tilePane.setPrefColumns(5); // 每行最多 5 个图片
        tilePane.setAlignment(Pos.TOP_LEFT);

        for (File image : images) {
            try {
                // 创建 ImageView（150*150px 但保持原始比例）
                Image fullImage = new Image(image.toURI().toString(), true);
                ImageView imageView = new ImageView();
                imageView.setFitWidth(150);
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);

                fullImage.progressProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.doubleValue() == 1.0) {
                        imageView.setImage(createThumbnail(fullImage));
                    }
                });

                // 处理文件名
                String fullFileName = image.getName();
                Label nameLabel = new Label(truncateFileName(fullFileName, 20));
                nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(150);
                nameLabel.setAlignment(Pos.CENTER);
                nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                nameLabel.setPrefHeight(35); // 限制最大高度，最多两行

                // 组合图片和名称
                VBox vbox = new VBox(imageView, nameLabel);
                vbox.setAlignment(Pos.CENTER);
                vbox.setSpacing(5);
                vbox.setPadding(new Insets(5));
                vbox.setStyle("-fx-border-color: transparent; -fx-border-width: 2px;");

                // 鼠标点击效果
                vbox.setOnMouseClicked(event -> {
                    pgmInfoLabel.setText("选中的图片：" + fullFileName);
                    float size = (float) image.length() / (1024 * 1024);
                    fileInfoLabel.setText(String.format("当前选中的图片大小：%.2f MB", size) + " (" + (image.length() / 1024) + " KB)");
                });

                vbox.setOnMouseEntered(event -> vbox.setStyle("-fx-border-color: rgb(97,145,175); -fx-border-width: 2px;"));
                vbox.setOnMouseExited(event -> vbox.setStyle("-fx-border-color: transparent;"));

                tilePane.getChildren().add(vbox);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        imageScrollPane.setContent(tilePane);
    }




    /**
     * 生成缩略图，保持原始比例，避免拉伸
     */
    private Image createThumbnail(Image source) {
        double originalWidth = source.getWidth();
        double originalHeight = source.getHeight();

        // 计算等比例缩放
        double scale = Math.min(150 / originalWidth, 150 / originalHeight);
        double targetWidth = originalWidth * scale;
        double targetHeight = originalHeight * scale;

        ImageView imageView = new ImageView(source);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        imageView.setPreserveRatio(true);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        return imageView.snapshot(parameters, null);
    }

    /**
     * 处理超长文件名，最多显示两行，超出部分用 "..." 省略
     */
    private String truncateFileName(String fileName, int maxLength) {
        if (fileName.length() <= maxLength) {
            return fileName;
        }
        return fileName.substring(0, maxLength - 3) + "...";
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
