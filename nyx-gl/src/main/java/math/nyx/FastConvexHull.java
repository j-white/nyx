package math.nyx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.vecmath.Point2f;

// Shamelessly stolen from https://code.google.com/p/convex-hull/source/browse/Convex+Hull/src/algorithms/FastConvexHull.java?r=4
public class FastConvexHull
{
    @SuppressWarnings("unchecked")
	public static ArrayList<Point2f> execute(ArrayList<Point2f> points) 
    {
		ArrayList<Point2f> xSorted = (ArrayList<Point2f>) points.clone();
		Collections.sort(xSorted, new XCompare());

        int n = xSorted.size();

        Point2f[] lUpper = new Point2f[n];
        
        lUpper[0] = xSorted.get(0);
        lUpper[1] = xSorted.get(1);
        
        int lUpperSize = 2;
        
        for (int i = 2; i < n; i++) {
            lUpper[lUpperSize] = xSorted.get(i);
            lUpperSize++;
            
            while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], 
            									lUpper[lUpperSize - 1])) {
                // Remove the middle point of the three last
                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                lUpperSize--;
            }
        }

        Point2f[] lLower = new Point2f[n];
        
        lLower[0] = xSorted.get(n - 1);
        lLower[1] = xSorted.get(n - 2);
        
        int lLowerSize = 2;
        
        for (int i = n - 3; i >= 0; i--) {
            lLower[lLowerSize] = xSorted.get(i);
            lLowerSize++;
            
            while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], 
            									lLower[lLowerSize - 1])) {
                // Remove the middle point of the three last
                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                lLowerSize--;
            }
        }

        ArrayList<Point2f> result = new ArrayList<Point2f>();
        
        for (int i = 0; i < lUpperSize; i++) {
            result.add(lUpper[i]);
        }
        
        for (int i = 1; i < lLowerSize - 1; i++) {
            result.add(lLower[i]);
        }
        
        return result;
    }

	private static boolean rightTurn(Point2f a, Point2f b, Point2f c) {
		return (b.x - a.x)*(c.y - a.y) - (b.y - a.y)*(c.x - a.x) > 0;
	}

	private static class XCompare implements Comparator<Point2f> {
		@Override
		public int compare(Point2f o1, Point2f o2) {
			return (new Float(o1.x)).compareTo(new Float(o2.x));
		}
	}
}