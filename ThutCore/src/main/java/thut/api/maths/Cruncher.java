package thut.api.maths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Cruncher {
	
		public Double[] set1 = new Double[] { 123456d };
		public Integer[] set5 = new Integer[] { 123456 };
		public Object[] set6 = new Object[]	{null};
		public byte[][] set11 = new byte[][] { { 123 } };
		public short[][] set12 = new short[][]{{123}};
		public int n;
		public Boolean[] done = { Boolean.valueOf(false) };
		
		private static boolean init = true;

		public Cruncher() {
		}

		double temp = 0.0D;
		int temp2 = 0;
		Object temp6 = null;
		float temp3 = 0.0F;
		byte[] temp4 = { 0 };
		short[] temp7 = {0};

		public void sort2(Double[] vals1, short[][] quadrant) {
			if ((vals1 == null) || (vals1.length == 0)) {
				return;
			}
			this.set1 = vals1;
			this.set12 = quadrant;
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
			if ((this.set12.length == this.n)) {
				this.temp7 = this.set12[i];
				this.set12[i] = this.set12[j];
				this.set12[j] = this.temp7;
			}
		}
	
		public static void indexToVals(int radius, int index, int diffSq, int diffCb, int[] toFill)
		{
			if(diffSq==0 || radius == 0)
				return;
			int layerSize =  (2*radius+1) * (2*radius+1);
			toFill[0] = 0;
			toFill[1] = 0;
			toFill[2] = 0;
			//Fill y
			{
				if(index >= layerSize && index < diffCb - layerSize)
				{
					int temp = (index - layerSize) / (diffSq) + 1;
					temp -= radius;
					temp = Math.min(temp, radius);
					toFill[2] = temp;
				}
				else
				{
					if(index > layerSize)
					{
						toFill[2] = -radius;
					}
					else
					{
						toFill[2] = radius;
					}
				}
			}
			//Fill x
			if(!(toFill[2] == radius || toFill[2] == -radius))
			{
				
				int temp = (index)%diffSq;
				if(temp < diffSq/2)
				{
					if(temp <= radius)
					{
						toFill[0] = temp;
					}
					else if(temp > diffSq/2 - radius)
					{
						toFill[0] = -(temp - (diffSq/2));
					}
					else toFill[0] = radius;
				}
				else if(temp > diffSq/2)
				{
					temp -= diffSq/2;
					if(temp <= radius)
					{
						toFill[0] = -temp;
					}
					else if(temp > diffSq/2 - radius)
					{
						toFill[0] = (temp - (diffSq/2));
					}
					else toFill[0] = -radius;
				}
			}
			else
			{
				int temp = (index%layerSize);
				temp = temp%(2*radius+1);
				temp -= radius;
				toFill[0] = temp;
			}
			//Fill z
			if(!(toFill[2] == radius || toFill[2] == -radius))
			{
				int temp = (index)%diffSq;
				temp = (temp + 2 * radius - 1)%diffSq + 1;
				if(temp < diffSq/2)
				{
					if(temp <= radius)
					{
						toFill[1] = temp;
					}
					else if(temp > diffSq/2 - radius)
					{
						toFill[1] = -(temp - (diffSq/2));
					}
					else toFill[1] = radius;
				}
				else if(temp > diffSq/2)
				{
					temp -= diffSq/2;
					if(temp <= radius)
					{
						toFill[1] = -temp;
					}
					else if(temp > diffSq/2 - radius)
					{
						toFill[1] = (temp - (diffSq/2));
					}
					else toFill[1] = -radius;
				}
			}
			else
			{
				int temp = (index%layerSize)/(2*radius+1);
				temp -= radius;
				toFill[1] = temp;
			}
		}
		
		public static int getVectorInt(Vector3 rHat) {
			
			if(rHat.magSq() > 1000000)
			{
				new Exception().printStackTrace();
			}
			int i = (rHat.intX()) + 512;
			int j = (rHat.intY()) + 512;
			int k = (rHat.intZ()) + 512;
			
			return i + (j << 10) + (k<<20);
		}		
		
		public static int getVectorInt(int x, int y, int z) {
			
			int i = x + 512;
			int j = y + 512;
			int k = z + 512;
			
			return i + (j << 10) + (k<<20);
		}
		
		public static void fillFromInt(int[] toFill, int vec)
		{
			 toFill[0] = (vec & 1023) - 512;
			 toFill[1] = ((vec >> 10) & 1023) - 512;
			 toFill[2] = ((vec >> 20) & 1023) - 512;
		}
		
}
