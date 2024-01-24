package bmg.lib;


import Jama.*;
import java.awt.geom.Point2D;


public class AffineTransform2D {
  public AffineTransform2D() {
    m = Matrix.identity(3,3);
  }

  public AffineTransform2D(Matrix A) {
    m = A;
  }

  public void translate(double dx, double dy) {
    Matrix T = Matrix.identity(3, 3);
    T.set(0, 2, dx);
    T.set(1, 2, dy); 
    m = m.times(T);
  }

  public void scale(double sx, double sy) {
    Matrix S = Matrix.identity(3, 3);
    S.set(0, 0, sx);
    S.set(1, 1, sy); 
    m = m.times(S);
  }

  public void rotate(double theta) {
    Matrix R = Matrix.identity(3, 3);
    R.set(0, 0, Math.cos(theta));
    R.set(0, 1, -Math.sin(theta));
    R.set(1, 0, Math.sin(theta));
    R.set(1, 1, Math.cos(theta));
    m = m.times(R);
  }

    public AffineTransform2D inverse() {
        return new AffineTransform2D(m.inverse());
    } 

    public Point2D apply(Point2D p) {
        Matrix X = new Matrix(3, 1);
        X.set(0, 0, p.getX());
        X.set(1, 0, p.getY());
        X.set(2, 0, 1);

        Jama.Matrix res = m.times(X);
        Point2D out = new Point2D.Double(res.get(0, 0), res.get(1, 0));
        return out;
    }


    public Point2D [] apply(Point2D [] p) {
        Point2D [] out = new Point2D [p.length];
        for (int i=0; i<p.length; ++i) {
            out[i] = apply(p[i]);
        }
        return out;
    }

    // world_extent = (xmin, xmax, ymin, ymax)
    public static AffineTransform2D worldToScreen(double [] world_extent, double [] device_extent) {
        if (world_extent.length != 4) {
            throw new IllegalArgumentException("Expected four coordinates for world_extent, got " + world_extent.length);
        }
        if (device_extent.length != 4) {
            throw new IllegalArgumentException("Expected four coordinates for device_extent, got " + device_extent.length);
        }
        double mx = (world_extent[0] + world_extent[1]) / 2.0;
        double my = (world_extent[2] + world_extent[3]) / 2.0;

        AffineTransform2D out = new AffineTransform2D(); 
        
        double size_in_x = world_extent[1] - world_extent[0];
        double size_in_y = world_extent[3] - world_extent[2];

        double size_out_x = device_extent[1] - device_extent[0];
        double size_out_y = device_extent[3] - device_extent[2];

        double sx = size_out_x / size_in_x;
        double sy = size_out_y / size_in_y;
        double s = Math.min(sx, sy);

        // Umgekehrte Reihenfolge (Matrizen werden rechts multipliziert)
        out.translate((device_extent[0] + device_extent[1]) / 2.0, (device_extent[2] + device_extent[3]) / 2.0);
        out.scale(s, -s);
        out.translate(-mx, -my);


        return out;
    }

    public String toString() {
        String s = ""; 
        s += m.get(0, 0) + "   " +  m.get(0, 1) + "   " + m.get(0, 2) + "\n";
        s += m.get(1, 0) + "   " +  m.get(1, 1) + "   " + m.get(1, 2) + "\n";
        s += m.get(2, 0) + "   " +  m.get(2, 1) + "   " + m.get(2, 2) + "\n";
        return  s;
    }


    private Matrix m;

}

