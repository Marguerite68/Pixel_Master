package com.example.pixel_master;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ImageController {
    private ScrollPane imageScrollPane;
    private Label fileInfoLabel;
    private Label pgmInfoLabel;
    private static Set<VBox> selectedImages = new HashSet<>();

    private static File Folder;



    public ImageController(ScrollPane imageScrollPane, Label fileInfoLabel, Label pgmInfoLabel) {
        this.imageScrollPane = imageScrollPane;
        this.fileInfoLabel = fileInfoLabel;
        this.pgmInfoLabel = pgmInfoLabel;
    }

    public void loadImages(File parentFolder) {
        this.Folder = parentFolder;
        File[] images = parentFolder.listFiles(file -> file.isFile() && isImageFile(file));

        imageScrollPane.setContent(null);

        if (images == null || images.length == 0) {
            Label noImagesLabel = new Label("当前路径下没有可供预览的图片");
            noImagesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            noImagesLabel.setAlignment(Pos.CENTER);

            StackPane stackPane = new StackPane(noImagesLabel);
            stackPane.setPrefSize(900, 600);
            imageScrollPane.setContent(stackPane);
            return;
        }

        TilePane tilePane = new TilePane();
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPrefColumns(5);
        tilePane.setAlignment(Pos.TOP_LEFT);

        for (File image : images) {
            try {
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

                String fullFileName = image.getName();
                Label nameLabel = new Label(truncateFileName(fullFileName, 20));
                nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: black;");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(150);
                nameLabel.setAlignment(Pos.CENTER);
                nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
                nameLabel.setPrefHeight(35);

                VBox vbox = new VBox(imageView, nameLabel);
                vbox.setAlignment(Pos.CENTER);
                vbox.setSpacing(5);
                vbox.setPadding(new Insets(5));
                vbox.setStyle("-fx-border-color: transparent; -fx-border-width: 2px;");

                // 设置vbox的UserData为image，用于后续获取图片文件
                vbox.setUserData(image);

                vbox.setOnMouseClicked(event -> {
                    if (event.isControlDown()) {
                        toggleSelection(vbox);

                    } else {
                        singleSelect(vbox);

                    }
                    updateFileInfoLabel();
                });

                vbox.setOnMouseEntered(event -> vbox.setStyle("-fx-border-color: rgb(97,145,175); -fx-border-width: 2px;"));
                vbox.setOnMouseExited(event -> {
                    if (!selectedImages.contains(vbox)) {
                        vbox.setStyle("-fx-border-color: transparent;");
                    }
                });

                tilePane.getChildren().add(vbox);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        imageScrollPane.setContent(tilePane);
    }

    private void updateFileInfoLabel() {
        double totalSize = 0.0;
        totalSize=selectedImages.stream().mapToDouble(vbox -> {
            File imageFile = (File) vbox.getUserData();
            return imageFile.length();
        }).sum();


        double sizeInMB = totalSize / (1024.0 * 1024.0);
        fileInfoLabel.setText("当前已选中 " + selectedImages.size() + " 张图片，总大小约为 " + String.format("%.2f", sizeInMB) + " MB");

    }


    private Image createThumbnail(Image source) {
        double originalWidth = source.getWidth();
        double originalHeight = source.getHeight();

        double scale = Math.min(150 / originalWidth, 150 / originalHeight);
        double targetWidth = originalWidth * scale;
        double targetHeight = originalHeight * scale;

        ImageView imageView = new ImageView(source);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        imageView.setPreserveRatio(true);

        return imageView.snapshot(null, null);
    }

    private String truncateFileName(String fileName, int maxLength) {
        if (fileName.length() <= maxLength) {
            return fileName;
        }
        return fileName.substring(0, maxLength - 3) + "...";
    }

    public boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                || fileName.endsWith(".bmp") || fileName.endsWith(".gif");
    }

    // 添加方法来处理按钮点击事件

    public void handleCopyButton() {
        System.out.println("点击了复制按钮");
    }

    public void handlePasteButton() {
        System.out.println("点击了粘贴按钮");
    }

    public void handleSlideModeButton() {
        System.out.println("点击了幻灯片按钮");
    }

    // 提供方法来设置按钮的事件处理程序
    public void setButtonActions(Button deleteButton, Button copyButton, Button pasteButton, Button slideModeButton) {
        deleteButton.setOnAction(e -> handleDeleteButton());
        copyButton.setOnAction(e -> handleCopyButton());
        pasteButton.setOnAction(e -> handlePasteButton());
        slideModeButton.setOnAction(e -> handleSlideModeButton());
    }

    // 处理单选
    private void singleSelect(VBox vbox) {

        for (VBox selectedVBox : selectedImages) {
            selectedVBox.setStyle("-fx-border-color: transparent;");
        }
        if (selectedImages.contains(vbox)) {
            selectedImages.remove(vbox);
            vbox.setStyle("-fx-border-color: transparent;");

        } else {
            selectedImages.clear();
            selectedImages.add(vbox);
            vbox.setStyle("-fx-border-color: rgb(97,145,175); -fx-border-width: 2px;");

        }
    }

    private void toggleSelection(VBox vbox) {

        if (selectedImages.contains(vbox)) {
            selectedImages.remove(vbox);
            vbox.setStyle("-fx-border-color: transparent;");

        } else {
            selectedImages.add(vbox);
            vbox.setStyle("-fx-border-color: rgb(97,145,175); -fx-border-width: 2px;");

        }
    }

    public void handleDeleteButton() {

        if (selectedImages.size() <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("信息");
            alert.setHeaderText(null);
            alert.setContentText("没有选中任何文件");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("确定要删除选中的图片吗？");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.setOnCloseRequest(event -> {
                if (alert.getResult() == ButtonType.YES) {
                    // 处理删除操作
                    selectedImages.forEach(vbox -> {
                        File imageFile = (File) vbox.getUserData();
                        if (imageFile != null && imageFile.delete()) {
                            System.out.println("已删除文件: " + imageFile.getName());
                        } else {
                            System.out.println("删除文件失败: " + imageFile.getName());
                        }
                    });
                    selectedImages.clear();
                    updateFileInfoLabel();
                    loadImages(this.Folder);

                }
            });
            alert.showAndWait();
        }
    }

    // 获取选中的图片
    public static Set<VBox> getSelectedImages() {
        return selectedImages;
    }
}
