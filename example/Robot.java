package per;

import jbotsim.Link;
import jbotsim.Node;
import jbotsim.Clock;
import jbotsim.event.ConnectivityListener;
import jbotsim.event.MessageListener;
import jbotsim.event.ClockListener;
import java.util.Vector;
import java.util.LinkedHashSet;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.lang.Runnable;

public class Robot extends Node implements Runnable {

    public static enum Formation {NEWS_ONLY, VERTICAL_LINE, HORIZONTAL_LINE, SQUARE, TRIANGLE, SEMICIRCLE}

    private Point2D.Double n1;
    private Point2D.Double n2;
    private Point2D.Double north = null;
    private Point2D.Double south = null;
    private Point2D.Double east = null;
    private Point2D.Double west = null;

    private Formation formation;
    private int N;
    private Point target;
    private static double step = 5;

    final static int DEFAULT_N = 20;
    final static String NORTH_COLOR = "red";
    final static String SOUTH_COLOR = "blue";
    final static String EAST_COLOR = "green";
    final static String WEST_COLOR = "yellow";
    final static String NOTHING_COLOR = "white";
    
    public Robot(){
        N = DEFAULT_N;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public void setN(int N) {
        this.N = N;
    }

    private void NEWSAlgorithm(){
        this.send(null, new Message("coord", this.getLocation(), "-1")); // Step 1: Broadcast own coordinates.
        n1 = (Point2D.Double) waitMessage("coord", "-1"); // Step 2: Receive coordinates from other robots.
        n2 = (Point2D.Double) waitMessage("coord", "-1");
        while (n2.equals(n1)) {
            n2 = (Point2D.Double) waitMessage("coord", "-1");
        }
        double Dn1 = this.getLocation().distance(n1); // Step 3: Calculate distances.
        double Dn2 = this.getLocation().distance(n2); 
        if (Dn2 > Dn1) { // Step 4.
            Point2D.Double ntemp = n1;
            n1 = n2;
            n2 = ntemp;
        }
        for (int i = 0; i < this.N; i++) {
            this.send(null, new Message("coord", this.getLocation(), String.valueOf(i))); // Step 5
            Point2D.Double n3 = (Point2D.Double) waitMessage("coord", String.valueOf(i)); // Step 6
            if (!n3.equals(n1) && !n3.equals(n2)) {
                Dn1 = this.getLocation().distance(n1); // Step 7
                Dn2 = this.getLocation().distance(n2);
                double Dtemp = this.getLocation().distance(n3); // Step 8
                if (Dtemp < Dn1) { // Step 9
                    n2 = n1;
                    n1 = n3;
                } else if (Dtemp < Dn2) { // Step 10
                    n2 = n3;
                }
            }
        } // Step 11
        if (this.getLocation().getY() > n1.getY() && this.getLocation().getY() > n2.getY()) { // Step 12
            this.north = this.getLocation();
            this.setColor(NORTH_COLOR);
        } else if (this.getLocation().getY() < n1.getY() && this.getLocation().getY() < n2.getY()) { // Step 13
            this.south = this.getLocation();
            this.setColor(SOUTH_COLOR);
        } else if (this.getLocation().getX() > n1.getX() && this.getLocation().getX() > n2.getX()) { // Step 14
            this.east = this.getLocation();
            this.setColor(EAST_COLOR);
        } else if (this.getLocation().getX() < n1.getX() && this.getLocation().getX() < n2.getX()) { // Step 15
            this.west = this.getLocation();
            this.setColor(WEST_COLOR);
        } else {
            this.setColor(NOTHING_COLOR);
        }

        // Broadcast north, south, east and west
        if (this.north == null) {
            this.north = (Point2D.Double) waitMessage("coord", "north");
        }
        this.send(null, new Message("coord", this.north, "north"));
        if (this.south == null) {
            this.south = (Point2D.Double) waitMessage("coord", "south");
        }
        this.send(null, new Message("coord", this.south, "south"));
        if (this.east == null) {
            this.east = (Point2D.Double) waitMessage("coord", "east");
        }
        this.send(null, new Message("coord", this.east, "east"));
        if (this.west == null) {
            this.west = (Point2D.Double) waitMessage("coord", "west");
        }
        this.send(null, new Message("coord", this.west, "west"));


        //String out = this.getLocation().toString() + " Finished : ";
        //out += "n1 = " + n1.toString();
        //out += "; n2 = " + n2.toString();
        //System.out.println(out);
        //System.out.println("Finished");
    }

    public void VerticalLineFormation() {
        NEWSAlgorithm(); // Step 1
        if (!this.getLocation().equals(north)) { // Step 2
            this.target = new Point((int) this.north.getX(), (int) this.getLocation().getY()); // Step 3
            this.moveToTarget(); // Steps 4 & 5
        }
    }

    public void HorizontalLineFormation() {
        NEWSAlgorithm(); // Step 1
        if (!this.getLocation().equals(east)) { // Step 2
            this.target = new Point((int) this.getLocation().getX(), (int) this.east.getY()); // Step 3
            this.moveToTarget(); // Steps 4 & 5
        }
    }

    public void TriangleFormation() {
        NEWSAlgorithm(); // Step 1
        if (!this.getLocation().equals(north) && !this.getLocation().equals(east) && !this.getLocation().equals(west)) { // Step 2
            if (this.getLocation().getX() > north.getX() && this.getLocation().getY() > east.getY()) { // Step 3
                this.target = projection(this.getLocation(), north, east);
            } else if (this.getLocation().getX() < north.getX() && this.getLocation().getY() > west.getY()) { // Step 4
                this.target = projection(this.getLocation(), north, west);
            } else { // Step 5
                this.target = projection(this.getLocation(), east, west);
            }
            this.moveToTarget(); // Steps 6 & 7
        }
    }

    public void SquareFormation() {
        NEWSAlgorithm(); // Step 1
        if (!this.getLocation().equals(north)
                && !this.getLocation().equals(south)
                && !this.getLocation().equals(east)
                && !this.getLocation().equals(west)) { // Step 2
            if (this.getLocation().getX() > north.getX() && this.getLocation().getY() > east.getY()) { // Step 3
                this.target = projection(this.getLocation(), north, east);
            } else if (this.getLocation().getX() < north.getX() && this.getLocation().getY() > west.getY()) { // Step 4
                this.target = projection(this.getLocation(), north, west);
            } else if (this.getLocation().getX() < south.getX() && this.getLocation().getY() < west.getY()) { // Step 5
                this.target = projection(this.getLocation(), south, west);
            } else { // Step 6
                this.target = projection(this.getLocation(), south, east);
            }
            this.moveToTarget(); // Steps 6 & 7
        }
    }

    public void SemiCircleFormation() {
        NEWSAlgorithm(); // Step 1
        if (!this.getLocation().equals(east)
                && !this.getLocation().equals(west)) { // Step 2
            if (!(this.getLocation().getY() > west.getY() || this.getLocation().getY() > east.getY())) { // Step 3
                this.target = projection(this.getLocation(), east, west); // Step 4
                this.moveToTarget(); // Steps 6 & 7
            }
        }
    }

    public static Point projection(Point2D.Double p, Point2D.Double p2, Point2D.Double p3) {
        // :TODO:maethor:121112: Return the projection of p on the line between p2 and p3 *with x-coordinate xi* (to implement exactly the article).
        // :DOC:maethor:121112: This algo return the orthogonal projection.
        // Line equation
        double a = (p3.getY() - p2.getY());
        double b = -(p3.getX() - p2.getX());
        double c = -(p2.getX() * p3.getY()) + (p2.getY() * p3.getX());
        // Projection coordonates
        double x = ((Math.pow(b, 2) * p.getX()) - (a * b * p.getY()) - (a * c)) / (Math.pow(a, 2) + Math.pow(b, 2));
        double y = ((Math.pow(a, 2) * p.getY()) - (a * b * p.getX()) - (b * c)) / (Math.pow(a, 2) + Math.pow(b, 2));
        return new Point((int) x, (int) y);
    }

    public void moveToTarget() {
        if (target != null) {
            this.setLocation(target.getX(), target.getY());
        }
        // :TODO:maethor:121112: Uncomment the lines above to implement exactly the article (step by step movement)
        //for (int i = 0; i < N; i++) {
        //    if (this.target != null && !this.target.equals(this.getLocation())) {
        //        this.setDirection(this.target);
        //        this.move(this.step);
        //    }
        //}
    }

    public void run(){
        switch (formation) {
            case NEWS_ONLY:
                this.NEWSAlgorithm();
                break;
            case VERTICAL_LINE:
                this.VerticalLineFormation();
                break;
            case HORIZONTAL_LINE:
                this.HorizontalLineFormation();
                break;
            case TRIANGLE:
                this.TriangleFormation();
                break;
            case SQUARE:
                this.SquareFormation();
                break;
            case SEMICIRCLE:
                this.SemiCircleFormation();
                break;
            default:
        }
    }

    private void clearMailbox(String type, String label) { // :COMMENT:maethor:121113: Could be usefull
        int i = 0;
        while(i != this.mailbox().size()) {
            Message msg = (Message) this.mailbox().get(i).content;
            if (msg.type == type && msg.label.equals(label)) {
                this.mailbox().removeElementAt(i);
            } else {
                i++;
            }
        }
    }

    private Object waitMessage(String type, String label) {
        int i = 0;
        while(true) { 
            while (i == this.mailbox().size());
            Message msg = (Message) this.mailbox().get(i).content;
            if (msg.type == type && msg.label.equals(label)) {
                this.mailbox().removeElementAt(i);
                return msg.content;
            } else {
                i++;
            }
        }
    }

    private class Message {
        public String type;
        public Object content;
        public String label;

        public Message(String type, Object content, String label) {
            this.type = type;
            this.content = content;
            this.label = label;
        }

        public String toString(){
            return "Type: " + type + "; Label: " + label + "; Content: " + content.toString();
        }
    }
}
