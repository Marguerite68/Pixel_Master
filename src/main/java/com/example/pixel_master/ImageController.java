package com.example.pixel_master;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class ImageController {
    private ScrollPane imageScrollPane;
    private Label fileInfoLabel;
    private Label pgmInfoLabel;
    private static Set<VBox> selectedImages = new HashSet<>();

    private static Set<VBox> copyImages = new HashSet<>();

    private static File Folder;



    public ImageController(ScrollPane imageScrollPane, Label fileInfoLabel, Label pgmInfoLabel) {
        this.imageScrollPane = imageScrollPane;
        this.fileInfoLabel = fileInfoLabel;
        this.pgmInfoLabel = pgmInfoLabel;
    }


    /*这个 loadImages(File parentFolder) 方法是 ImageController 类的核心方法，主要作用是加载并显示指定文件夹中的图片，并实现图片的选择交互功能。 */
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

        imageScrollPane.setOnMouseClicked(event -> {
            System.out.println(event.getTarget());
            // 检查点击位置
            if (event.getTarget() instanceof ImageView || event.getTarget() instanceof Text || event.getTarget() instanceof Label || event.getTarget() instanceof VBox) {
               return;
            }
            else {
                // 取消所有选中状态
                for (VBox vbox1 : selectedImages) {
                    vbox1.setStyle("-fx-border-color: transparent;");
                }
                selectedImages.clear();
                updateFileInfoLabel();
            }
        });

        imageScrollPane.setContent(tilePane);
    }


    /*这个 updateFileInfoLabel() 方法是 ImageController 类中的一个私有方法，主要用于更新界面上的文件信息标签，显示当前选中图片的数量和总大小。 */
    private void updateFileInfoLabel() {
        double totalSize = 0.0;
        totalSize=selectedImages.stream().mapToDouble(vbox -> {
            File imageFile = (File) vbox.getUserData();
            return imageFile.length();
        }).sum();


        double sizeInMB = totalSize / (1024.0 * 1024.0);
        fileInfoLabel.setText("当前已选中 " + selectedImages.size() + " 张图片，总大小约为 " + String.format("%.2f", sizeInMB) + " MB");

    }


    /*这个 createThumbnail(Image source) 方法用于生成原图的缩略图，确保缩略图在保持宽高比的同时，适应指定的最大尺寸（150×150 像素）。 */
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


    /* 这个 truncateFileName(String fileName, int maxLength) 方法用于截断过长的文件名，并在末尾添加省略号（...），以确保文件名在界面上显示时不会超出指定长度。*/
    private String truncateFileName(String fileName, int maxLength) {
        if (fileName.length() <= maxLength) {
            return fileName;
        }
        return fileName.substring(0, maxLength - 3) + "...";
    }


    /*判断是不是图片文件 */
    public boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                || fileName.endsWith(".bmp") || fileName.endsWith(".gif");
    }

    // 添加方法来处理按钮点击事件

    public void handleCopyButton() {
        if (selectedImages.size() <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("没有选中任何文件");
            alert.showAndWait();
        }
        else {
            copyImages.clear();
            copyImages.addAll(selectedImages);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("图片已复制");
            alert.showAndWait();
            /*copyImages.forEach(vbox -> {
                System.out.println(vbox.getUserData());
            });*/
        }
    }

    public void handlePasteButton() {
        if (copyImages.size() <= 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("没有可粘贴的文件");
            alert.showAndWait();
        }
        else{
            for (VBox vbox : copyImages) {
                File sourceFile = (File) vbox.getUserData();
                if (sourceFile != null) {
                    String fileName = sourceFile.getName().split("\\.")[0];
                    String suffix= sourceFile.getName().split("\\.")[1];
                    String targetName=sourceFile.getName();
                    if(new File(Folder,sourceFile.getName()).exists())
                    {
                        targetName = fileName + " - 副本" + "." + suffix;
                    }

                    File targetFile = new File(Folder, targetName);
                    try {
                        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("已粘贴文件: " + targetFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("错误");
                        alert.setHeaderText(null);
                        alert.setContentText("粘贴文件 " + sourceFile.getName() + " 时出错");
                        alert.showAndWait();
                    }
                }
            }
            // 刷新图片列表
            loadImages(Folder);
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

    public void handleRenameButton() {
        if(selectedImages.size()==1)
        {
            // 获取选中的图片文件
            VBox selectedVBox = selectedImages.iterator().next();
            File imageFile = (File) selectedVBox.getUserData();

            // 创建一个对话框，提示用户输入新的文件名
            TextInputDialog dialog = new TextInputDialog(imageFile.getName().split("\\.")[0]);
            dialog.setTitle("重命名");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入新的文件名:");

            // 显示对话框并等待用户输入
            dialog.showAndWait().ifPresent(newFileName -> {
                if (!newFileName.isEmpty()) {
                    // 获取文件的原始扩展名
                    String suffix=imageFile.getName().split("\\.")[1];

                    // 构建新的文件名
                    String newFileNameWithExtension = newFileName + "." + suffix;

                    // 创建新的文件对象
                    File newFile = new File(imageFile.getParent(), newFileNameWithExtension);

                    // 重命名文件
                    try {
                        Files.move(imageFile.toPath(), newFile.toPath());
                        // 更新VBox的userData为新的文件对象
                        selectedVBox.setUserData(newFile);
                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        System.out.println(e.getMessage());
                        alert.setTitle("错误");
                        alert.setHeaderText(null);
                        alert.setContentText("重命名文件 " + imageFile.getName() + " 失败: " + e.getMessage());
                        alert.showAndWait();
                    }

                }
                selectedImages.clear();
                updateFileInfoLabel();
                loadImages(Folder);
            });
        } else if (selectedImages.size() >= 2) {
            // 创建一个对话框，提示用户输入名称前缀、起名编号和编号位数
            Dialog<RenameParam> dialog = new Dialog<>();
            dialog.setTitle("批量重命名");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入批量重命名的参数:");

            // 设置对话框的输入控件
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField prefixField = new TextField();
            prefixField.setPromptText("名称前缀");
            TextField startNumberField = new TextField();
            startNumberField.setPromptText("起名编号");
            TextField numberDigitsField = new TextField();
            numberDigitsField.setPromptText("编号位数");

            grid.add(new Label("名称前缀:"), 0, 0);
            grid.add(prefixField, 1, 0);
            grid.add(new Label("起名编号:"), 0, 1);
            grid.add(startNumberField, 1, 1);
            grid.add(new Label("编号位数:"), 0, 2);
            grid.add(numberDigitsField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // 设置对话框的按钮
            ButtonType renameButtonType = new ButtonType("重命名", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(renameButtonType, ButtonType.CANCEL);

            // 设置对话框的结果转换器
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == renameButtonType) {
                    String prefix = prefixField.getText();
                    int startNumber = Integer.parseInt(startNumberField.getText());
                    int numberDigits = Integer.parseInt(numberDigitsField.getText());
                    return new RenameParam(prefix, startNumber, numberDigits);
                }
                return null;
            });

            // 显示对话框并等待用户输入
            dialog.showAndWait().ifPresent(params -> {
                int count = 1;
                if((String.valueOf(params.startNumber+selectedImages.size()).length()>params.numberDigits))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText(null);
                    alert.setContentText("重命名文件失败");
                    alert.showAndWait();
                    return ;
                }

                for (VBox selectedVBox : selectedImages) {
                    File imageFile = (File) selectedVBox.getUserData();
                    String suffix = imageFile.getName().split("\\.")[1];
                    String newFileName = String.format("%s%0" + params.numberDigits + "d.%s", params.prefix, params.startNumber + count - 1, suffix);
                    File newFile = new File(imageFile.getParent(), newFileName);
                    // 重命名文件
                    try {
                        Files.move(imageFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        // 更新VBox的userData为新的文件对象
                        selectedVBox.setUserData(newFile);
                    } catch (Exception e) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        System.out.println(e.getMessage());
                        alert.setTitle("错误");
                        alert.setHeaderText(null);
                        alert.setContentText("重命名文件 " + imageFile.getName() + " 失败: " + e.getMessage());
                        alert.showAndWait();
                    }
                    count++;
                }
                selectedImages.clear();
                updateFileInfoLabel();
                // 刷新图片列表
                loadImages(Folder);
            });
        }
    }

    // 提供方法来设置按钮的事件处理程序
    public void setButtonActions(Button deleteButton, Button copyButton, Button pasteButton, Button slideModeButton,Button renameButton) {
        deleteButton.setOnAction(e -> handleDeleteButton());
        copyButton.setOnAction(e -> handleCopyButton());
        pasteButton.setOnAction(e -> handlePasteButton());
        renameButton.setOnAction(e -> handleRenameButton());
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


    // 获取选中的图片,用于幻灯片播放
    public static Set<VBox> getSelectedImages() {
        return selectedImages;
    }
}

