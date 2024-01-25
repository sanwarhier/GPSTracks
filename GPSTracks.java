import javax.swing.*;

import java.awt.event.MouseEvent;

import bmg.lib.AffineTransform2D;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class GPSTracks extends JFrame implements MouseMotionListener{
    private JButton fileSelectButton;
    private JComboBox<String> colorComboBox;
    private JComboBox<String> widthSelectBox;
    private JPanel drawingPanel;

    private double[] world_extent = { 0, 0, 0, 0 };
    private double[] screen_extent = { 0, 400, 0, 400 };
    private double[] world_longlat = {0, 0, 0, 0};

    private int selectedWidth = 1;

    private List<Point2D> pointsWorld = new ArrayList<>();
    private List<Point2D> pointsScreen = new ArrayList<>();
    private List<Double> speeds = new ArrayList<>();
    private List<OffsetDateTime> timestamps = new ArrayList<>();

    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    public GPSTracks() {
        setTitle("GPSTracks");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fileSelectButton = new JButton("Datei auswählen");
        widthSelectBox = new JComboBox<>(new String[] {"dünn", "mittel", "dick"});
        colorComboBox = new JComboBox<>(new String[] { "Rot", "Grün", "Blau" });
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Setze Zoom- und Pan-Transformation
                g2d.scale(scale, scale);
                g2d.translate(offsetX, offsetY);

                // male Hintergrundkarte
                ImageIcon imageIcon = new ImageIcon();
                try {
                    String bbox = String.format(
                        "%s,%s,%s,%s", 
                        world_longlat[0], 
                        world_longlat[2], 
                        world_longlat[1], 
                        world_longlat[3]);
                    String requestUrl = String.format(
                        "https://ows.mundialis.de/services/service?service=WMS&version=1.1.1&request=GetMap&layers=OSM-WMS&styles=default&width=800&height=600&srs=EPSG:4326&bbox=%s&format=image/png",
                        bbox);
                    URL imageUrl = new URL(requestUrl);

                    // Bild herunterladen
                    imageIcon = new ImageIcon(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                g2d.drawImage(imageIcon.getImage(), 0, 0, getWidth(), getHeight(), this);

                // male Liniensegmente
                for (int i = 0; i < speeds.size(); i++) {
                    g.setColor(Color.red);
                    if (speeds.get(i) > 2.2) {
                        g.setColor(Color.orange);
                    }
                    if (speeds.get(i) > 2.6) {
                        g.setColor(Color.yellow);
                    }
                    if (speeds.get(i) > 2.8) {
                        g.setColor(Color.green);
                    }
                    g2d.setStroke(new BasicStroke(selectedWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Line2D line = new Line2D.Double(pointsScreen.get(i), pointsScreen.get(i+1));
                    g2d.draw(line);
                }
            }
        };

        drawingPanel.addMouseMotionListener(this);
        addMouseMotionListener(this);

        addMouseWheelListener(e -> {
            // Zoom
            double zoomFactor = (e.getWheelRotation() < 0) ? 1.1 : 0.9; // Zoom in or out
            zoom(e.getX(), e.getY(), zoomFactor);
            repaint();
        });

        // Action Listener für den Button
        fileSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    JOptionPane.showMessageDialog(null, "Du hast die Datei ausgewählt: " + selectedFile.getAbsolutePath());
                    
                    pointsWorld = loadPointsFromCSV(selectedFile.getAbsolutePath());
                    pointsScreen = transformPoints(pointsWorld);
                    speeds = calculateSpeeds();

                    drawingPanel.repaint();
                }
            }
        });

        widthSelectBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedWidthStr = (String) widthSelectBox.getSelectedItem();
                    switch (selectedWidthStr) {
                        case "dünn":
                            selectedWidth = 2;
                            break;
                        case "mittel":
                            selectedWidth = 4;
                            break;
                        case "dick":
                            selectedWidth = 6;
                            break;
                    
                        default:
                            selectedWidth = 1;
                            break;
                    }
                    drawingPanel.repaint();
                }
            }
        });

        // Layout-Konfiguration
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Farbe:"));
        controlPanel.add(colorComboBox);
        controlPanel.add(new JLabel("Linienbreite:"));
        controlPanel.add(widthSelectBox);
        controlPanel.add(fileSelectButton);

        add(controlPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private Point2D startPoint;

    public void mouseMoved(MouseEvent e) {
       startPoint = new Point2D.Double(e.getX(), e.getY());
    }

    public void mouseDragged(MouseEvent e) {
       // Pan
        double deltaX = e.getX() - startPoint.getX();
        double deltaY = e.getY() - startPoint.getY();

        offsetX += deltaX;
        offsetY += deltaY;

        startPoint = e.getPoint();
        repaint();
    }

    private void zoom(int zoomX, int zoomY, double zoomFactor) {
        // Adjust offset to keep the zoom center at the same location
        offsetX -= zoomX / scale - zoomX / (scale * zoomFactor);
        offsetY -= zoomY / scale - zoomY / (scale * zoomFactor);
        scale *= zoomFactor;
    }

    private List<Point2D> loadPointsFromCSV(String filePath) {
        String line = "";
        String csvSplitBy = ",";
        List<Point2D> points = new ArrayList<>();
        Path2D worldPath = new Path2D.Double();
        Path2D longLatPath = new Path2D.Double();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // erste Zeile
            while ((line = br.readLine()) != null) {
                // Verwende das Trennzeichen, um die Zeile in Felder zu zerlegen
                String[] data = line.split(csvSplitBy);

                // Punkte speichern
                Point2D point = new Point2D.Double(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                points.add(point);

                //Timestamp speichern
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ssX");
                String datestr = data[1].substring(1, data[1].length() - 1);
                OffsetDateTime offsetDateTime = OffsetDateTime.now();
                try {
                    offsetDateTime = OffsetDateTime.parse(datestr, formatter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timestamps.add(offsetDateTime);

                // Path kreieren
                if (worldPath.getCurrentPoint() == null) {
                    worldPath.moveTo(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                    longLatPath.moveTo(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
                } else {
                    worldPath.lineTo(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                    longLatPath.lineTo(Double.parseDouble(data[2]), Double.parseDouble(data[3]));
                }
            }
            this.world_extent[0] = worldPath.getBounds2D().getMinX();
            this.world_extent[1] = worldPath.getBounds2D().getMaxX();
            this.world_extent[2] = worldPath.getBounds2D().getMinY();
            this.world_extent[3] = worldPath.getBounds2D().getMaxY();

            this.world_longlat[0] = longLatPath.getBounds2D().getMinX();
            this.world_longlat[1] = longLatPath.getBounds2D().getMaxX();
            this.world_longlat[2] = longLatPath.getBounds2D().getMinY();
            this.world_longlat[3] = longLatPath.getBounds2D().getMaxY();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<Double> calculateSpeeds() {
        List<Double> speeds = new ArrayList<>();
        for (int i = 0; i < pointsWorld.size()-1; i++) {
            double distance = calculateDistance(pointsWorld.get(i).getX(), pointsWorld.get(i).getY(), pointsWorld.get(i+1).getX(), pointsWorld.get(i+1).getY());
            Duration timeDifference = Duration.between(timestamps.get(i), timestamps.get(i+1));
            speeds.add(distance/timeDifference.toSeconds());
        }
        return speeds;
    }

    private static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private List<Point2D> transformPoints(List<Point2D> points) {
        AffineTransform2D T = AffineTransform2D.worldToScreen(world_extent, screen_extent);
        List<Point2D> transformedPoints = new ArrayList<>();
        for (Point2D p : points) {
            //System.out.printf("(%.3f | %.3f)",  p.getX(), p.getY());
            Point2D tp = T.apply(p);
            transformedPoints.add(tp);
            //System.out.printf(" --> (%.3f | %.3f)\n",  tp.getX(), tp.getY());
        }
        return transformedPoints;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GPSTracks();
            }
        });
    }
}
