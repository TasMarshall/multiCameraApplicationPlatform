package platform;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraManager.core.DirectStreamView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class GUI_Controller {

    @FXML
    private StackPane map_view;

    @FXML
    private Button btn_start;

    @FXML
    private GridPane grid_pane;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    static int mapWidth;
    static int mapHeight;

    static int cameraWidth;
    static int cameraHeight;

    static MapView mapView;
    static MCP_Application mcp_application;

    private Map<String,Pane> cameraImageMap = new HashMap<>();

    private static Image notWorkingImage;
    private static ImageIcon notWorkingImageIcon;

    public static void init(MapView mapView2, MCP_Application mcp_application2){
        mcp_application = mcp_application2;
        mapView = mapView2;

        initMap(800,800);
        initCameras(200,200);
        initSymbols();
    }

    private static void initSymbols() {

        BufferedImage bufferedImage = null;

        try {
            bufferedImage = ImageIO.read(new File("src\\main\\resources\\gui\\notworking.jpg"));
        } catch (IOException e) {

        }

        notWorkingImage = SwingFXUtils.toFXImage(bufferedImage,null);
        notWorkingImageIcon = new ImageIcon("src\\main\\resources\\gui\\notworking.jpg");

    }

    public static void initMap(int width, int height) {

        mapWidth = width;
        mapHeight = height;

    }

    public static void initCameras(int width, int height){

        cameraWidth = width;
        cameraHeight = height;

    };

    public void pressMapStartButton (ActionEvent event){

        map_view.getChildren().addAll(mapView.getMapView());
        btn_start.cancelButtonProperty();
        btn_start.setVisible(false);

        List<? extends Camera> cameras = mcp_application.getAllCameras();

        float gridHeight = cameras.size();
        float gridWidth = 1;

        /*for (Camera camera: cameras){
            int temp = camera.getAnalysisManager().getAnalysisAlgorithmsSet().size();
            if (temp > gridWidth-1){
                gridWidth = temp+1;
            }
        }

        for (int i = 0; i < gridHeight; i++){
            for( int j = 0; j < gridWidth; j++){

                Pane pane = new Pane();
                final SwingNode swingNode = new SwingNode();

                if(j==0){

                    createAndSetSourceVideo(swingNode, cameras.get(i));
                    pane.getChildren().add(swingNode);

                    cameraImageMap.put(cameras.get(i).getIdAsString() + "stream",pane);

                }
                else{

                    if (!(cameras.get(i) instanceof SimulatedCamera)) {
                        Object[] analysisAlgorithms = cameras.get(i).getAnalysisManager().getSortedAlgorithmSet().toArray();
                        if ( analysisAlgorithms.length >= j){

                            Image image;

                            if (cameras.get(i).isWorking()) {

                                Mat mat = ((AnalysisAlgorithm) analysisAlgorithms[j - 1]).getProcessedImage();

                                BufferedImage bImage = null;

                                try {
                                    bImage = DirectStreamView.Mat2BufferedImage(mat);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                image = SwingFXUtils.toFXImage(bImage, null);

                            }
                            else {
                                image = notWorkingImage;
                            }

                            ImageView imageView = new ImageView(image);
                            imageView.setImage(image);

                            pane.getChildren().add(imageView);

                            cameraImageMap.put(cameras.get(i).getIdAsString() + "analysis" + j,pane);

                            final int ii = i;
                            final int jj = j;

                            // grab a frame every 33 ms (30 frames/sec)
                            Runnable frameGrabber = new Runnable() {

                                @Override
                                public void run()
                                {
                                    int  iii = ii;
                                    Image image;

                                    if (cameras.get(iii).isWorking()) {

                                        Mat mat = ((AnalysisAlgorithm) analysisAlgorithms[jj - 1]).getProcessedImage().clone();
                                        BufferedImage bImage = null;
                                        try {
                                            bImage = DirectStreamView.Mat2BufferedImage(mat);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        image = SwingFXUtils.toFXImage(bImage, null);
                                    }
                                    else{
                                        image = notWorkingImage;
                                    }

                                    updateImageView(imageView, image);

                                }
                            };

                            this.timer = Executors.newSingleThreadScheduledExecutor();
                            this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                        }
                    }
                    else{
                        createAndSetProcessedVideos(swingNode, cameras.get(i), cameras.get(i).getAnalysisManager().getSortedAlgorithmSet().toArray(), j);
                        pane.getChildren().add(swingNode);
                    }

                }

                GridPane.setConstraints(pane,i,j,(int)gridWidth,(int)gridHeight);
                grid_pane.getChildren().add(pane);

            }
        }
*/
    }

    private void updateImageView(ImageView view, Image image)
    {
        onFXThread(view.imageProperty(), image);
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property
     *            a {@link ObjectProperty}
     * @param value
     *            the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value)
    {
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    private void createAndSetSourceVideo(final SwingNode swingNode, Camera camera) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                /*JPanel panel = new JPanel();*/
                if (!(camera instanceof SimulatedCamera)) {
                    JPanel jPanel;
                    if (camera.isWorking()) {
                        DirectStreamView cameraStream = camera.getCameraStreamManager().getDirectStreamView();
                        jPanel = cameraStream.getVideoSurface();

                    }
                    else {
                        ImagePanel panel = new ImagePanel(notWorkingImageIcon.getImage());
                        jPanel = panel;
                    }
                    swingNode.setContent(jPanel);
                    //jPanel.
                }
                else{
                    JTextArea jTextArea = new JTextArea("Sim Camera");
                    swingNode.setContent(jTextArea);
                }
            }
        });
    }

    private void createAndSetProcessedVideos(final SwingNode swingNode, Camera camera, Object[] analysisAlgorithms, int j) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                /*JPanel panel = new JPanel();*/
                if (!(camera instanceof SimulatedCamera)) {
                    if (analysisAlgorithms.length >= j){


                    }
                    else {
                        JTextArea jTextArea = new JTextArea("Sim Camera");
                        swingNode.setContent(jTextArea);
                    }
                }
                else{
                    JTextArea jTextArea = new JTextArea("Sim Camera");
                    swingNode.setContent(jTextArea);
                }
            }
        });
    }

    class ImagePanel extends JPanel {

        private java.awt.Image img;

        public ImagePanel(java.awt.Image img) {
            this.img = img;
            Dimension size = new Dimension((int)img.getWidth(null), (int)img.getHeight(null));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setLayout(null);
        }

        public void paintComponent(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }

    }

    public Map<String, Pane> getCameraImageMap() {
        return cameraImageMap;
    }

    public void setCameraImageMap(Map<String, Pane> cameraImageMap) {
        this.cameraImageMap = cameraImageMap;
    }
}
