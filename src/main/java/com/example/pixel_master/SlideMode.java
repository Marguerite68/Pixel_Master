package com.example.pixel_master;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;
import java.io.File;
import javafx.application.Platform;  // 用于在JavaFX应用程序中创建和管理窗口,处理事件和线程（自动播放时的moveright()）

public class SlideMode {
    private int interval; // 播放间隔（秒）
    private VBox selectedVBox = new VBox(); // 用于存放图片的容器
    private Set<VBox> selectedImages = ImageController.getSelectedImages();
    private BorderPane borderPane = new BorderPane(); // 用于显示图片的根布局 分上下左右中五个区域
    private ScrollPane scrollPane = new ScrollPane(); // 用于显示图片的滚动面板
    private ImageView imageView = new ImageView();  // 用于显示图片的视图
    private Stage slidestate = new Stage(); // 用于显示图片的舞台
    private HBox hBox = new HBox(); // 用于功能的水平布局
    private int cur; // 用于记录当前显示的图片索引
    private volatile boolean isAutoPlay = false; // 使用 volatile 关键字确保可见性
    private Thread autoPlayThread; // 保存自动播放线程


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
        selectedImages = ImageController.getSelectedImages();
        selectedVBox = selectedImages.iterator().next();
        cur = 1; // 初始化索引
        File imagefile = (File) selectedVBox.getUserData();

        slidestate.setTitle(imagefile.getName()); // 设置窗口标题

        Image image = new Image(imagefile.getAbsolutePath());

        imageView.setImage(image);
        imageView.setPreserveRatio(true); // 保持宽高比
        imageView.setSmooth(true); // 平滑缩放

        scrollPane.setContent(imageView);
        scrollPane.setFitToHeight(true); // 设置滚动面板高度自适应
        scrollPane.setFitToWidth(true); // 设置滚动面板宽度自适应
        scrollPane.setStyle("-fx-alignment: center;"); // 设置滚动面板居中显示
        borderPane.setCenter(scrollPane); // 将滚动面板放入边框面板中
        addMenu();

        Scene scene = new Scene(borderPane, 1000, 800); // 设置场景大小

        adjustImageViewSize(); // 调整图片视图大小

        // 新增：设置窗口大小变化时，图片视图也随之变化
        //监听事件  窗口大小有变化就适配
        slidestate.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImageViewSize();
        });
        slidestate.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImageViewSize();
        });

        // 加入按钮


        slidestate.setScene(scene);
        slidestate.show();
    }

    private void adjustImageViewSize() {
        double width = slidestate.getWidth();
        double height = slidestate.getHeight() - hBox.getHeight(); // 减去 HBox 的高度

        borderPane.setPrefSize(width, height + hBox.getHeight());
        scrollPane.setPrefSize(width, height);

        // 获取图片的宽高比
        double imageRatio = imageView.getImage().getWidth() / imageView.getImage().getHeight();

        // 根据宽高比和可用空间调整 ImageView 的大小
        if (width / height > imageRatio) {
            imageView.setFitHeight(height);
            imageView.setFitWidth(height * imageRatio);
        } else {
            imageView.setFitWidth(width);
            imageView.setFitHeight(width / imageRatio);
        }

        double toppadding = (height - imageView.getFitHeight()) / 2;
        double leftpadding = (width - imageView.getFitWidth()) / 2;

        // 新增：确保内边距不小于 0
        toppadding = Math.max(0, toppadding);
        leftpadding = Math.max(0, leftpadding);

        // 设置滚动面板的内边距
        scrollPane.setPadding(new Insets(toppadding, leftpadding, toppadding, leftpadding));

        // 重新设置滚动条位置
        if (imageView.getFitWidth() > scrollPane.getWidth() || imageView.getFitHeight() > scrollPane.getHeight()) {
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
        }
    }

    public void addMenu() {
        hBox.setPrefSize(slidestate.getWidth() - 100, 80); //设置水平布局的大小为窗口宽度-100，高度为100
        hBox.setSpacing(20); //设置水平布局的间距为20
        hBox.setAlignment(Pos.CENTER);  //设置水平布局的居中对齐
        hBox.getChildren().clear();

        Button enlargeButton = new Button("放大");
        Button narrowButton = new Button("缩小");
        Button moveLeftButton = new Button("<--");
        Button moveRightButton = new Button("-->");
        Button auto = new Button("自动播放");
        Button exit = new Button("退出");

        auto.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");
        enlargeButton.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");
        narrowButton.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");
        moveLeftButton.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");
        moveRightButton.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");
        exit.setStyle("-fx-min-height: 30; -fx-min-width: 80;-fx-background-color: rgb(210,210,210);");

        auto.setPrefSize(45, 45);
        enlargeButton.setPrefSize(45, 45);
        narrowButton.setPrefSize(45, 45);
        moveLeftButton.setPrefSize(45, 45);
        moveRightButton.setPrefSize(45, 45);
        exit.setPrefSize(45, 45);

        hBox.getChildren().addAll(moveLeftButton, enlargeButton, narrowButton, moveRightButton, auto, exit); //将按钮添加到水平布局中
        borderPane.setBottom(hBox); //将水平布局放入边框面板中

        //设置按钮的点击事件
        enlargeButton.setOnAction(event -> {
            breakAutoPlay(auto);
            enlarge();
        });

        narrowButton.setOnAction(event -> {
            breakAutoPlay(auto);
            narrow();
        });

        moveLeftButton.setOnAction(event -> {
            breakAutoPlay(auto);
            moveleft();
        });

        moveRightButton.setOnAction(event -> {
            breakAutoPlay(auto);
            moveright();
        });

        exit.setOnAction(event -> {
            
            slidestate.close(); // 关闭幻灯片窗口
        });

        auto.setOnAction(event->{
            autoPlay(auto); // 自动播放
        });
    }

    public void enlarge(){
        double x = 1.2; // 放大比例
        imageView.setFitWidth(imageView.getFitWidth() * x);
        imageView.setFitHeight(imageView.getFitHeight() * x);
        adjustImage_button(x); // 调整图片视图大小
    }

    public void narrow(){
        double x = 0.8; // 缩小比例
        imageView.setFitWidth(imageView.getFitWidth() * x);
        imageView.setFitHeight(imageView.getFitHeight() * x);
        adjustImage_button(x); // 调整图片视图大小
    }

    public void adjustImage_button(double x) {
        double width = slidestate.getWidth();
        double height = slidestate.getHeight() - hBox.getHeight(); // 减去 HBox 的高度

        borderPane.setPrefSize(width, height + hBox.getHeight());
        scrollPane.setPrefSize(width, height);

        double toppadding = (height - imageView.getFitHeight()) / 2;
        double leftpadding = (width - imageView.getFitWidth()) / 2;

        // 新增：确保内边距不小于 0
        toppadding = Math.max(0, toppadding);
        leftpadding = Math.max(0, leftpadding);

        // 设置滚动面板的内边距
        scrollPane.setPadding(new Insets(toppadding, leftpadding, toppadding, leftpadding));

        // 重新设置滚动条位置
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);
    }

    public void moveleft(){
        VBox new_selectedVboxs = new VBox();
        Iterator<VBox> iterator = selectedImages.iterator();
        int index = 0;
        if(selectedImages.size() == 1){
            return;
        }

        if(cur == 1){
            while(iterator.hasNext()){
                index++;
                new_selectedVboxs = iterator.next();
                if(index == selectedImages.size()){
                    cur = index;
                    //new_selectedVboxs = iterator.next();  不知道为什么放在后面就不行
                    File imagefile = (File) new_selectedVboxs.getUserData();
                    slidestate.setTitle(imagefile.getName()); // 设置窗口标题
    
                    Image image = new Image(imagefile.getAbsolutePath());
    
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true); // 保持宽高比
                    imageView.setSmooth(true); // 平滑缩放
                    adjustImageViewSize();
                    break;
                }
            }
        }else{
            while(iterator.hasNext()){
                index++;
                new_selectedVboxs = iterator.next();
                if(index == cur-1){
                    cur = index;
                    //new_selectedVboxs = iterator.next();
                    File imagefile = (File) new_selectedVboxs.getUserData();
                    slidestate.setTitle(imagefile.getName()); // 设置窗口标题
    
                    Image image = new Image(imagefile.getAbsolutePath());
    
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true); // 保持宽高比
                    imageView.setSmooth(true); // 平滑缩放
                    adjustImageViewSize();
                    break;
                }
            }   
        }
    }

    public void moveright(){
        VBox new_selectedVboxs = new VBox();
        Iterator<VBox> iterator = selectedImages.iterator();
        int index = 0;
        if(selectedImages.size() == 1){
            return;
        }
        if(cur == selectedImages.size()){
            while(iterator.hasNext()){
                index++;
                new_selectedVboxs = iterator.next();
                if(index == 1){
                    cur = index;
                    final VBox finalVbox = new_selectedVboxs; // 创建一个 final 变量,以便在 lambda 表达式中使用
                    Platform.runLater(() -> {
                        // 例如更新窗口标题
                        File imagefile = (File)finalVbox.getUserData();
                        slidestate.setTitle(imagefile.getName());
                
                        // 其他可能的 UI 更新操作，如更新 ImageView 等
                        Image image = new Image(imagefile.getAbsolutePath());
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        adjustImageViewSize();
                    });
                    break;
                }
            }
        }else{
            while(iterator.hasNext()){
                index++;
                new_selectedVboxs = iterator.next();
                if(index == cur+1){
                    cur = index;
                    final VBox finalVbox = new_selectedVboxs; // 创建一个 final 变量,以便在 lambda 表达式中使用
                    Platform.runLater(() -> {
                        // 例如更新窗口标题
                        File imagefile = (File) finalVbox.getUserData();
                        slidestate.setTitle(imagefile.getName());
                
                        // 其他可能的 UI 更新操作，如更新 ImageView 等
                        Image image = new Image(imagefile.getAbsolutePath());
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        adjustImageViewSize();
                    });
                
                    break;
                }
            }
        }
    }

    public void autoPlay(Button auto) {
        if (isAutoPlay) {
            isAutoPlay = false;
            auto.setText("自动播放");
            if (autoPlayThread != null && autoPlayThread.isAlive()) {
                autoPlayThread.interrupt(); // 中断线程
            }
        } else {
            isAutoPlay = true;
            auto.setText("停止播放");
            autoPlayThread = new Thread(() -> {
                while (isAutoPlay) {
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        // 线程被中断时退出循环
                        return;
                    }
                    moveright();
                }
            });
            autoPlayThread.start();
        }
    }

    public void breakAutoPlay(Button auto) {
        isAutoPlay = false;
        auto.setText("自动播放");
        if (autoPlayThread != null && autoPlayThread.isAlive()) {
            autoPlayThread.interrupt(); // 中断线程
        }
    }

}