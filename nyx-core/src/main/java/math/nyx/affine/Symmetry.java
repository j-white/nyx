package math.nyx.affine;

public enum Symmetry {
	    ORIGINAL,        /* 1 2
	    					3 4 */
		ROTATE_90,       /* 3 1
		    				4 2 */
		ROTATE_180,      /* 4 3
		    				2 1 */
		ROTATE_270,      /* 2 4
		    				1 3 */
		FLIP,            /* 3 4
		    				1 2 */
		ROTATE_90_FLIP,  /* 4 2
		    				3 1 */
		ROTATE_180_FLIP, /* 2 1
		    				4 3 */
		ROTATE_270_FLIP  /* 1 3
		    				2 4 */
}
