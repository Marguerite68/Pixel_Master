package com.example.pixel_master;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.geometry.Pos;

import java.io.File;

public class TreeController {
    private TreeView<String> treeView;
    private ImageController imageController;

    private final Image diskIcon = new Image("file:src/main/resources/image/disk.png");
    private final Image folderIcon = new Image("file:src/main/resources/image/folder.png");
    private final Image PCIcon = new Image("file:src/main/resources/image/PC.png");

    public TreeController(Label fileInfoLabel, Label pgmInfoLabel, ScrollPane imageScrollPane) {
        this.imageController = new ImageController(imageScrollPane, fileInfoLabel, pgmInfoLabel);
        this.treeView = createDirectoryTree();
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }

    private TreeView<String> createDirectoryTree() {
        TreeItem<String> rootItem = createTreeItem("我的电脑", PCIcon, null);
        rootItem.setExpanded(true);

        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                TreeItem<String> diskItem = createTreeItem(root.toString(), diskIcon, root);
                rootItem.getChildren().add(diskItem);
                addSubdirectories(diskItem, root);
            }
        }

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setPrefWidth(250);

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                File file = (File) newItem.getGraphic().getUserData();
                if (file != null && file.isDirectory()) {
                    imageController.loadImages(file);
                }
            }
        });

        return treeView;
    }

    private void addSubdirectories(TreeItem<String> parentItem, File folder) {
        File[] files = folder.listFiles(file -> file.isDirectory() && !file.getName().equalsIgnoreCase("$RECYCLE.BIN"));
        if (files != null) {
            for (File file : files) {
                TreeItem<String> dirItem = createTreeItem(file.getName(), folderIcon, file);
                parentItem.getChildren().add(dirItem);
                if (hasSubdirectories(file)) {
                    dirItem.getChildren().add(new TreeItem<>("加载中..."));
                }

                dirItem.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                    if (isNowExpanded) {
                        loadSubdirectoriesAsync(dirItem);
                    }
                });
            }
        }
    }

    private TreeItem<String> createTreeItem(String name, Image icon, File file) {
        ImageView imageView = (icon != null) ? new ImageView(icon) : null;
        if (imageView != null) {
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setUserData(file);
        }

        TreeItem<String> item = new TreeItem<>(name, imageView);

        if (file != null) {
            Tooltip tooltip = new Tooltip(file.getAbsolutePath());
            Tooltip.install(imageView, tooltip);
        }

        return item;
    }

    private boolean hasSubdirectories(File folder) {
        File[] files = folder.listFiles(file -> file.isDirectory() || imageController.isImageFile(file));
        return files != null && files.length > 0;
    }

    private void loadSubdirectoriesAsync(TreeItem<String> parentItem) {
        if (parentItem.getChildren().isEmpty()) return;

        File parentFolder = (File) parentItem.getGraphic().getUserData();
        if (parentFolder == null || !parentFolder.exists() || !parentFolder.isDirectory()) return;

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
                            parentItem.getChildren().clear();
                            for (File file : subDirs) {
                                TreeItem<String> dirItem = createTreeItem(file.getName(), folderIcon, file);
                                parentItem.getChildren().add(dirItem);
                                if (hasSubdirectories(file)) {
                                    dirItem.getChildren().add(new TreeItem<>("加载中..."));
                                }

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
}
