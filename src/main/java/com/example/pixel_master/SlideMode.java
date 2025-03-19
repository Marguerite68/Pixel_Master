package com.example.pixel_master;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SlideMode {
    private int interval; // 播放间隔（秒）

    private SlideMode(int interval) {
        this.interval = interval;
    }

    //需要让外部调用这个方法来创建该类的实例
    public static void showSlideIntervalDialog() {
        // 如果未选中任何图片，弹出警告
        if (ImageController.getSelectedImages().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText("未选中任何图片！");
            alert.setContentText("请先选中至少一张图片再进行幻灯片播放！");

            //设置对话框的图标
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image("file:src/main/resources/image/warning.png"));

            alert.showAndWait();
        }
        // 否则弹出设置幻灯片播放间隔时间的对话框
        else {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("设置幻灯片播放间隔时间");
            dialogStage.setResizable(false);
            dialogStage.setWidth(350);
            dialogStage.setHeight(200);
            dialogStage.getIcons().add(new javafx.scene.image.Image("file:src/main/resources/image/Pixel Master icon.png"));

            // 创建文本标签
            Label label = new Label("请选择播放间隔时间（秒）：");
            label.setId("dialog-label");

            // **创建下拉菜单**
            ComboBox<Integer> intervalComboBox = new ComboBox<>();
            intervalComboBox.setId("dialog-combo");
            intervalComboBox.getItems().addAll(1, 2, 3, 5, 10);  // 预设间隔时间
            intervalComboBox.setValue(3);  // 默认选择3秒

            // 创建错误提示
            Label errorLabel = new Label();
            errorLabel.setId("dialog-error");

            // 创建按钮
            Button confirmButton = new Button("确定");
            confirmButton.setId("dialog-button");

            Button cancelButton = new Button("取消");
            cancelButton.setId("dialog-cancel");

            //监听确定按钮点击事件
            confirmButton.setOnAction(event -> {
                Integer selectedInterval = intervalComboBox.getValue();
                if (selectedInterval == null || selectedInterval <= 0) {
                    errorLabel.setText("请选择有效的时间！");
                    return;
                }

                //选择间隔后，创建 SlideMode 并启动
                SlideMode slideMode = new SlideMode(selectedInterval);
                slideMode.startSlideshow();

                dialogStage.close();
            });

            cancelButton.setOnAction(event -> dialogStage.close());

            // 创建布局
            HBox buttonBox = new HBox(15, confirmButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER);

            VBox layout = new VBox(12, label, intervalComboBox, errorLabel, buttonBox);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));

            Scene scene = new Scene(layout);
            scene.getStylesheets().add("file:src/main/resources/style/style.css");

            dialogStage.setScene(scene);
            dialogStage.show();
        }
    }


    public int getInterval() {
        return interval;
    }

    //启动幻灯片播放
    public void startSlideshow() {
        // TODO: 这里实现幻灯片播放逻辑,需要调用ImageController的getSelectedImages()方法获取选中的图片
    }
}
