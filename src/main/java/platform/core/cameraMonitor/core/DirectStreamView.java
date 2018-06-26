package platform.core.cameraMonitor.core;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import platform.core.camera.core.Camera;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.*;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DirectStreamView {

    Camera camera;

    private static final int width = 200;
    private static final int height = 130;

    private final JPanel videoSurface;

    private final BufferedImage image;

    private final DirectMediaPlayerComponent mediaPlayerComponent;
    private boolean streamIsPlaying = false;

    public DirectStreamView(Camera camera) {
        this.camera = camera;
        new NativeDiscovery().discover();

        videoSurface = new VideoSurfacePanel();

        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(width, height);

        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                return new RV32BufferFormat(width, height);
            }
        };

        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new TutorialRenderCallbackAdapter();
            }
        };

        playFromURIandUserPW(camera);

    }

    public void playFromURIandUserPW (Camera camera) {

        String securedAccessURI = camera.getStreamURI().replace("rtsp://","rtsp://" + camera.getUsername() + ":" + camera.getPassword() + "@");
        System.out.println(securedAccessURI);
        streamIsPlaying = mediaPlayerComponent.getMediaPlayer().playMedia(securedAccessURI);

    }

    public void updateStreamState(){

        if (!camera.isWorking()){
            streamIsPlaying = false;
        }

    }

    private class VideoSurfacePanel extends JPanel {

        private VideoSurfacePanel() {
            setBackground(Color.black);
            setOpaque(true);
            setPreferredSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;

            if (streamIsPlaying == false){
                g2.setColor(Color.black);
                g2.fillRect(videoSurface.getWidth()/2 - 50,videoSurface.getHeight()/2-20,100,40);
                g2.setColor(Color.red);
                g2.drawString("Camera not working.", videoSurface.getWidth()/2 - 50, videoSurface.getHeight()/2-10) ;
            }
            else if (streamIsPlaying == true) {
                g2.drawImage(image, null, 0, 0);
            }
        }
    }

    private class TutorialRenderCallbackAdapter extends RenderCallbackAdapter {

        private TutorialRenderCallbackAdapter() {
            super(new int[width * height]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            // Simply copy buffer to the image and repaint
            image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
            videoSurface.repaint();
        }
    }

    public boolean isStreamIsPlaying() {
        return streamIsPlaying;
    }

    public JPanel getVideoSurface() {
        return videoSurface;
    }

    public Mat getImageMat() throws IOException {
        return bufferedImage2Mat(image);
    }

    public Mat bufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }

    public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
    }
}