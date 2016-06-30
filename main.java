import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;

public class main extends JFrame{
	
	public main() {
        setLayout(new BorderLayout());
        dotArea dotPlace = new dotArea();
        dotPlace.setBounds(0, 60, 600, 540);
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        dotPlace.setBorder(blackLine);
        add(dotPlace);
        JButton compute = new JButton("Toggle Computing");
        compute.setLayout(null);
        setLayout(null);
        compute.setBounds(20, 20, 140, 40);
        compute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println(e);
				dotPlace.drawLines = !dotPlace.drawLines;
				dotPlace.repaint();
				
			} 
        });
        add(compute);
        JButton clear = new JButton("Clear");
        clear.setLayout(null);
        clear.setBounds(160, 20, 70, 40);
        clear.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		dotPlace.points.clear();
        		dotPlace.repaint();
        	}
        });
        add(clear);
        SpinnerNumberModel numberModel = new SpinnerNumberModel(
                new Integer(15), // value
                new Integer(10), // min
                new Integer(30), // max
                new Integer(1) // step
        );
        JSpinner numberChooser = new JSpinner(numberModel);
        //numberChooser.setLayout(null);
        numberChooser.setBounds(430, 20, 150, 40);
        add(numberChooser);
        JButton compRand = new JButton("Compute Random Points: ");
        compRand.setLayout(null);
        compRand.setBounds(230, 20, 200, 40);
        compRand.addActionListener(new ActionListener(){
        	@Override
        	public void actionPerformed(ActionEvent e){
        		dotPlace.points.clear();
        		for(int i = 0; i < (int) numberModel.getValue(); i++){
        			int x = (int) Math.floor(Math.random() * 580);
        			int y = (int) Math.floor(Math.random() * 520);
        			dotPlace.points.add(new Point(x, y));
        		}
        		dotPlace.drawLines = true;
        		dotPlace.repaint();
        	}
        });
        add(compRand);
        
        
	}

    public static void main(String[] args) {
        main frame = new main();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 650);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setTitle("Delaunay Triangulation");
        
    }
	
	public class dotArea extends JComponent implements MouseListener{
		public dotArea(){
	        addMouseListener(this);
		}
	    public ArrayList<Point> points = new ArrayList<Point>();
		public boolean drawLines = false;
		@Override
		public void mouseClicked(MouseEvent e) {
			Point point = e.getPoint();
			points.add(point);
			repaint();
			System.out.println(point);
		}
		
		public void paint(Graphics g) {
			if (isOpaque()) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			g.setColor(Color.GRAY);
			Iterator<Point> pointIT = points.iterator();
			while(pointIT.hasNext()){
				Point point = pointIT.next();
				if (point != null) {
					g.setColor(getForeground());
					g.fillOval(point.x - 3, point.y - 3, 7, 7);
				}
			}
			if(drawLines == true){
				int count = 0;
			  	int size = points.size();
			  	for(int i = 0; i < size; i++){
			  		Point a = points.get(i);
			  		for(int j = i+1; j < size; j++){
			  			Point b = points.get(j);
			  			for(int k = j + 1; k < size; k++){
			  				Point c = points.get(k);
			  				boolean innies = false;
			  				for(int l = 0; l < size; l++){
			  					if(l == i || l == j || l == k){
			  						continue;
			  					}
			  					Point d = points.get(l);
			  					boolean ins = inside(a, b, c, d);
			  					if(ins){
			  						innies = true;
				  					break;
			  					}
			  				}
			  				if(!innies){
			  					g.drawLine(a.x, a.y, b.x, b.y);
			  					g.drawLine(a.x, a.y, c.x, c.y);
			  					g.drawLine(b.x, b.y, c.x, c.y);
			  					count++;
			  				}
			  			}
			  		}
			  	}
				//drawLines = false;
				System.out.println(count);
				System.out.println(size*(size - 1)*(size - 2)/6);
			}
		}
		    
		public Point[][] pointDivide(ArrayList<Point> pointList){
			ArrayList<Integer> sizes = new ArrayList<Integer>();
			int listSize = pointList.size();
			switch(listSize % 3){
				case 1:
					if(listSize >= 4){
						sizes.add(2); sizes.add(2);
						listSize -= 4;
					}
					break;
				case 2:
					if(listSize >= 2){
						sizes.add(2);
						listSize -=2;
					}
					break;
				default:
					break;
			}
			for(int i = 0; i < listSize/3; i++){
				sizes.add(3);
			}
			Point[][] ans = new Point[sizes.size()][];
			Iterator pointIT = pointList.iterator();
			for(int i = 0; i < sizes.size(); i++){
				ans[i] = new Point[sizes.get(i)];
				for(int j = 0; j < sizes.get(i); j++){
					ans[i][j] = (Point) pointIT.next();
				}
			}
			return ans;
		}
		
		
		public class PointCompare implements Comparator<Point> {
			public int compare(final Point a, final Point b) {
				if (a.x < b.x) {
					return -1;
				} else if (a.x > b.x) {
					return 1;
				} else {
					return 0;
				}
			}
		}
		
		public boolean inside(Point a, Point b, Point c, Point d){
			Point[] counterClock = getCounterClock(a, b, c);
			if(counterClock[0] == null){
				return true;
			} else {
				a = counterClock[0]; b = counterClock[1]; c = counterClock[2];
				BigInteger[][] matrix = makeMatrix(a, b, c, d);
				BigInteger det = determinant(matrix);
				if(det.compareTo(BigInteger.ZERO) == 1){
					return true;
				} else {
					return false;
				}
			}
		}
		
		public Point[] getCounterClock(Point a, Point b, Point c){
			MathContext mc = new MathContext(75, RoundingMode.HALF_EVEN);
			BigDecimal s1; 
			BigDecimal s2;	
			if(a.y == b.y){
				s1 = BigDecimal.ZERO;
			} else {
				s1 = BigDecimal.valueOf(-(a.x - b.x)).divide(BigDecimal.valueOf(a.y - b.y), mc);
			}
			if(a.y == c.y){
				s2 = BigDecimal.ZERO;
			} else {
				s2 = BigDecimal.valueOf(-(a.x - c.x)).divide(BigDecimal.valueOf(a.y - c.y), mc);
			}
			
			if(s1.compareTo(s2) == 0){
				return new Point[3];
			} else {
				BigDecimal x1 = BigDecimal.valueOf(a.x+b.x).divide(BigDecimal.valueOf(2), mc);
				BigDecimal x2 = BigDecimal.valueOf(a.x+c.x).divide(BigDecimal.valueOf(2), mc);
				BigDecimal y1 = BigDecimal.valueOf(a.y+b.y).divide(BigDecimal.valueOf(2), mc);
				BigDecimal y2 = BigDecimal.valueOf(a.y+c.y).divide(BigDecimal.valueOf(2), mc);
				BigDecimal x = s1.multiply(x1).subtract(s2.multiply(x2)).subtract(y1).add(y2).divide(s1.subtract(s2), mc);
				BigDecimal y = s1.multiply(x).subtract(s1.multiply(x1)).add(y1);
				
				Point[] pts = new Point[3];
				pts[0] = a; pts[1] = b; pts[2] = c;
				double[] tans = new double[3];
				tans[0] = Math.atan2(BigDecimal.valueOf(a.y).subtract(y).doubleValue(), BigDecimal.valueOf(a.x).subtract(x).doubleValue());
				tans[1] = Math.atan2(BigDecimal.valueOf(b.y).subtract(y).doubleValue(), BigDecimal.valueOf(b.x).subtract(x).doubleValue());
				tans[2] = Math.atan2(BigDecimal.valueOf(c.y).subtract(y).doubleValue(), BigDecimal.valueOf(c.x).subtract(x).doubleValue());
				for(int i = 0; i < 3; i++){
					for(int j = i+1; j < 3; j++){
						if(tans[i] > tans[j]){
							double tmp = tans[i];
							tans[i] = tans[j];
							tans[j] = tmp;
							Point tmpPt = pts[i];
							pts[i] = pts[j];
							pts[j] = tmpPt;
						}
					}
				}
				return pts;
			}
		}
		
		public BigInteger[][] makeMatrix(Point a, Point b, Point c, Point d){
			BigInteger ar[][] = new BigInteger[3][3];
			ar[0][0] = BigInteger.valueOf(a.x - d.x);                                     
			ar[0][1] = BigInteger.valueOf(a.y - d.y);                                     
			ar[0][2] = BigInteger.valueOf(a.x * a.x + a.y * a.y - d.x * d.x - d.y * d.y); 
			ar[1][0] = BigInteger.valueOf(b.x - d.x);                                     
			ar[1][1] = BigInteger.valueOf(b.y - d.y);                                     
			ar[1][2] = BigInteger.valueOf(b.x * b.x + b.y * b.y - d.x * d.x - d.y * d.y); 
			ar[2][0] = BigInteger.valueOf(c.x - d.x);                                     
			ar[2][1] = BigInteger.valueOf(c.y - d.y);                                     
			ar[2][2] = BigInteger.valueOf(c.x * c.x + c.y * c.y - d.x * d.x - d.y * d.y);
			return ar;
		}

		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {}
	}

	
	//This method was imported from http://www.sanfoundry.com/java-program-compute-determinant-matrix/
	public BigInteger determinant(BigInteger A[][])
    {
		int N = A.length;
        BigInteger det = BigInteger.ZERO;
        if(N == 1)
        {
            det = A[0][0];
        }
        else if (N == 2)
        {
            det = A[0][0].multiply(A[1][1]).subtract(A[1][0].multiply(A[0][1]));
        }
        else
        {
            det=BigInteger.ZERO;
            for(int j1=0;j1<N;j1++)
            {
                BigInteger[][] m = new BigInteger[N-1][];
                for(int k=0;k<(N-1);k++)
                {
                    m[k] = new BigInteger[N-1];
                }
                for(int i=1;i<N;i++)
                {
                    int j2=0;
                    for(int j=0;j<N;j++)
                    {
                        if(j == j1)
                            continue;
                        m[i-1][j2] = A[i][j];
                        j2++;
                    }
                }
                if(j1%2 == 0){
                    det = det.add((A[0][j1]).multiply(determinant(m)));
                } else {
                    det = det.subtract((A[0][j1]).multiply(determinant(m)));
                }
            }
        }
        return det;
    }
	
}

