import javax.swing.*;

import bmg.lib.AffineTransform2D;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class GPSTracks extends JFrame {
    private JButton fileSelectButton;
    private JComboBox<String> colorComboBox;
    private JComboBox<String> widthSelectBox;
    private JPanel drawingPanel;

    private double[] world_extent = { 0, 0, 0, 0 };
    private double[] screen_extent = { 0, 400, 0, 400 };

    private Path2D path = new Path2D.Double();

    private List<Point2D> points = new ArrayList<>();

    public GPSTracks() {
        setTitle("GPSTracks");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Textfeld für die Anzahl der Features
        fileSelectButton = new JButton("Datei auswählen");
        widthSelectBox = new JComboBox<>(new String[] {"dünn", "mittel", "dick"});
        colorComboBox = new JComboBox<>(new String[] { "Rot", "Grün", "Blau" });
        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                Graphics2D g2d = (Graphics2D) g;
                g2d.draw(path);
            }
        };

        // Action Listener für den Button
        fileSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    JOptionPane.showMessageDialog(null, "Du hast die Datei ausgewählt: " + selectedFile.getAbsolutePath());
                    
                    points = loadPointsFromCSV(selectedFile.getAbsolutePath());
                    points = transformPoints(points);
                    path = createPathfromPoints(points);

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

    private List<Point2D> loadPointsFromCSV(String filePath) {
        String line = "";
        String csvSplitBy = ",";
        List<Point2D> points = new ArrayList<>();
        Path2D worldPath = new Path2D.Double();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // erste Zeile
            while ((line = br.readLine()) != null) {
                // Verwende das Trennzeichen, um die Zeile in Felder zu zerlegen
                String[] data = line.split(csvSplitBy);

                // Verarbeite die Daten nach Bedarf
                for (String value : data) {
                    System.out.print(value + " ");
                }
                System.out.println(); // Neue Zeile für jede Zeile der CSV-Datei

                Point2D point = new Point2D.Double(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                points.add(point);

                if (worldPath.getCurrentPoint() == null) {
                    worldPath.moveTo(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                } else {
                    worldPath.lineTo(Double.parseDouble(data[5]), Double.parseDouble(data[6]));
                }
            }
            this.world_extent[0] = worldPath.getBounds2D().getMinX();
            this.world_extent[1] = worldPath.getBounds2D().getMaxX();
            this.world_extent[2] = worldPath.getBounds2D().getMinY();
            this.world_extent[3] = worldPath.getBounds2D().getMaxY();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<Point2D> transformPoints(List<Point2D> points) {
        System.out.println("transformPoints");
        AffineTransform2D T = AffineTransform2D.worldToScreen(world_extent, screen_extent);
        System.out.println(T.toString());
        List<Point2D> transformedPoints = new ArrayList<>();
        for (Point2D p : points) {
            System.out.printf("(%.3f | %.3f)",  p.getX(), p.getY());
            Point2D tp = T.apply(p);
            transformedPoints.add(tp);
            System.out.printf(" --> (%.3f | %.3f)\n",  tp.getX(), tp.getY());
        }
        return transformedPoints;
    }

    private Path2D createPathfromPoints(List<Point2D> points) {
        Path2D path = new Path2D.Double();
        path.setWindingRule(Path2D.WIND_NON_ZERO); // Wicklungsregel festlegen (optional)

        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);

            if (i == 0) {
                path.moveTo(point.getX(), point.getY()); // Erster Punkt als MoveTo setzen
            } else {
                path.lineTo(point.getX(), point.getY()); // Linie zu den folgenden Punkten hinzufügen
            }
        }

        return path;
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
