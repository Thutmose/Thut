package thut.api.maths;

import java.util.ArrayList;
import java.util.List;

public class Cruncher {
	
		public Double[] set1 = new Double[] { 123456d };
		public Integer[] set5 = new Integer[] { 123456 };
		public Object[] set6 = new Object[]	{null};
		public byte[][] set11 = new byte[][] { { 123 } };
		public int n;
		public Boolean[] done = { Boolean.valueOf(false) };

		public static byte size = (byte) 100;

		public static final ArrayList<Integer> locs = new ArrayList<Integer>();
		private static boolean init = true;

		public Cruncher() {
			if (init) {
				init = false;
				populateInt();
			}
		}

		double temp = 0.0D;
		int temp2 = 0;
		Object temp6 = null;
		float temp3 = 0.0F;
		byte[] temp4 = { 0 };

		public void sort2(Double[] vals1, byte[][] quadrant) {
			if ((vals1 == null) || (vals1.length == 0)) {
				return;
			}
			this.set1 = vals1;
			this.set11 = quadrant;
			this.n = this.set1.length;

			quicksort(0, this.n - 1);
		}

		public void sort22(Double[] vals1, Integer[] vals2) {
			if ((vals1 == null) || (vals1.length == 0)) {
				return;
			}
			this.set1 = vals1;
			this.set5 = vals2;
			this.n = this.set1.length;

			quicksort(0, this.n - 1);
		}

		public void sort(Double[] vals1, Object[] vals2) {
			if ((vals1 == null) || (vals1.length == 0)) {
				return;
			}
			this.set1 = vals1;
			this.set6 = vals2;
			this.n = this.set1.length;

			quicksort(0, this.n - 1);
		}
		
		private void quicksort(int low, int high) {
			int i = low;
			int j = high;
			double pivot = this.set1[(low + (high - low) / 2)].doubleValue();
			while (i <= j) {
				while (this.set1[i].doubleValue() < pivot)
					i++;
				while (this.set1[j].doubleValue() > pivot)
					j--;
				if (i <= j) {
					exchange(i, j);
					i++;
					j--;
				}
			}
			if (low < j)
				quicksort(low, j);
			if (i < high)
				quicksort(i, high);
		}

		private void exchange(int i, int j) {
			if ((this.set1[0] != 123456d) || (this.set1.length == this.n)) {
				this.temp = this.set1[i].doubleValue();
				this.set1[i] = this.set1[j];
				this.set1[j] = Double.valueOf(this.temp);
			}
			if ((this.set5[0] != 123456) || (this.set5.length == this.n)) {
				this.temp2 = this.set5[i].intValue();
				this.set5[i] = this.set5[j];
				this.set5[j] = (this.temp2);
			}
			if ((this.set6[0] != null) || (this.set6.length == this.n)) {
				this.temp6 = this.set6[i];
				this.set6[i] = this.set6[j];
				this.set6[j] = (this.temp6);
			}
			if ((this.set11.length == this.n)) {
				this.temp4 = this.set11[i];
				this.set11[i] = this.set11[j];
				this.set11[j] = this.temp4;
			}
		}

		public static byte[][] populatePoints() {
			byte[][] quadrant;
			Double[] radii;
			List<byte[]> templist = new ArrayList<byte[]>();
			List<Double> tempRadii = new ArrayList<Double>();
			Cruncher sort = new Cruncher();

			for (byte z = (byte) -size; z <= size; z++)
				for (byte y = (byte) -size; y <= size; y++)
					for (byte x = (byte) -size; x <= size; x++) {

						double radSq = (double) (x * x + y * y + z * z);
						if (radSq > size * size)
							continue;

						templist.add(new byte[] { x, y, z });
						tempRadii.add(radSq);
					}
			quadrant = templist.toArray(new byte[0][0]);
			radii = tempRadii.toArray(new Double[0]);
			templist.clear();
			tempRadii.clear();

			sort.sort2(radii, quadrant);

			radii = sort.set1;
			quadrant = sort.set11;

			sort.set1 = null;
			sort.set11 = null;

			return quadrant;
		}

		public static void populateInt() {

			new Thread(new Runnable() {
				public void run() {

					System.out.println("Initializing explosion volume");

					Integer[] quadrant;
					Double[] radii;
					List<Integer> templist = new ArrayList<Integer>();
					List<Double> tempRadii = new ArrayList<Double>();
					Cruncher sort = new Cruncher();

					for (int z = -size; z <= size; z++)
						for (int y = -size; y <= size; y++)
							for (int x = -size; x <= size; x++) {

								double radSq = (double) (x * x + y * y + z * z);
								if (radSq > size * size)
									continue;
								int i = ((x + size) + (y + size) * 256 + (z + size) * 256 * 256);
								// System.out.println(x+" "+y+" "+z+" "+i+" "+((i&255)-size)+" "+(((i/256)&255)-size)+" "+(((i/(256*256))&255)-size)+" ");
								templist.add(i);
								tempRadii.add(radSq);
							}
					quadrant = templist.toArray(new Integer[0]);
					radii = tempRadii.toArray(new Double[0]);
					templist.clear();
					tempRadii.clear();

					sort.sort22(radii, quadrant);

					radii = sort.set1;
					quadrant = sort.set5;

					sort.set1 = null;
					sort.set5 = null;

					int[] ret = new int[quadrant.length];
					for (int i = 0; i < quadrant.length; i++)
						ret[i] = quadrant[i];

					for (int i : ret) {
						locs.add(i);
					}

					System.out.println("Explosion volume Initialized");

				}
			}).start();
		}

		public static int getIndex(Vector3 rHat, int linearFactor) {
			return (int) (rHat.x * linearFactor)
					+ linearFactor
					+ 1
					+ (((int) (rHat.y * linearFactor) + linearFactor + 1) * linearFactor)
					+ (((int) (rHat.z * linearFactor) + linearFactor + 1)
							* linearFactor * linearFactor);
		}
}
