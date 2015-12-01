package thut.api.maths;

public class Cruncher
{

    public Double[]  set1  = new Double[] { 123456d };
    public Integer[] set5  = new Integer[] { 123456 };
    public Object[]  set6  = new Object[] { null };
    public byte[][]  set11 = new byte[][] { { 123 } };
    public short[][] set12 = new short[][] { { 123 } };
    public int       n;
    public Boolean[] done  = { Boolean.valueOf(false) };

    public Cruncher()
    {
    }

    double  temp  = 0.0D;
    int     temp2 = 0;
    Object  temp6 = null;
    float   temp3 = 0.0F;
    byte[]  temp4 = { 0 };
    short[] temp7 = { 0 };

    public void sort2(Double[] vals1, short[][] quadrant)
    {
        if ((vals1 == null) || (vals1.length == 0)) { return; }
        this.set1 = vals1;
        this.set12 = quadrant;
        this.n = this.set1.length;

        quicksort(0, this.n - 1);
    }

    public void sort22(Double[] vals1, Integer[] vals2)
    {
        if ((vals1 == null) || (vals1.length == 0)) { return; }
        this.set1 = vals1;
        this.set5 = vals2;
        this.n = this.set1.length;

        quicksort(0, this.n - 1);
    }

    public void sort(Double[] vals1, Object[] vals2)
    {
        if ((vals1 == null) || (vals1.length == 0)) { return; }
        this.set1 = vals1;
        this.set6 = vals2;
        this.n = this.set1.length;

        quicksort(0, this.n - 1);
    }

    private void quicksort(int low, int high)
    {
        int i = low;
        int j = high;
        double pivot = this.set1[(low + (high - low) / 2)].doubleValue();
        while (i <= j)
        {
            while (this.set1[i].doubleValue() < pivot)
                i++;
            while (this.set1[j].doubleValue() > pivot)
                j--;
            if (i <= j)
            {
                exchange(i, j);
                i++;
                j--;
            }
        }
        if (low < j) quicksort(low, j);
        if (i < high) quicksort(i, high);
    }

    private void exchange(int i, int j)
    {
        if ((this.set1[0] != 123456d) || (this.set1.length == this.n))
        {
            this.temp = this.set1[i].doubleValue();
            this.set1[i] = this.set1[j];
            this.set1[j] = Double.valueOf(this.temp);
        }
        if ((this.set5[0] != 123456) || (this.set5.length == this.n))
        {
            this.temp2 = this.set5[i].intValue();
            this.set5[i] = this.set5[j];
            this.set5[j] = (this.temp2);
        }
        if ((this.set6[0] != null) || (this.set6.length == this.n))
        {
            this.temp6 = this.set6[i];
            this.set6[i] = this.set6[j];
            this.set6[j] = (this.temp6);
        }
        if ((this.set11.length == this.n))
        {
            this.temp4 = this.set11[i];
            this.set11[i] = this.set11[j];
            this.set11[j] = this.temp4;
        }
        if ((this.set12.length == this.n))
        {
            this.temp7 = this.set12[i];
            this.set12[i] = this.set12[j];
            this.set12[j] = this.temp7;
        }
    }

    public static void indexToVals(int index, Vector3 toFill)
    {
        if(index>0)
        {
            int cr, rsd, rcd;
            int nrc;
            
            cr = (int) Cruncher.cubeRoot(index);
            cr = (cr-1)/2 + 1;

            int temp = 2*(cr)-1;
            int crc = temp*temp*temp;
            int si = index - crc;
            int crs = temp*temp;
            
            temp = 2*(cr+1)-1;
            int nrs = temp*temp;
            nrc = temp*temp*temp;
            rsd = nrs - crs;
            rcd = nrc - crc;
            indexToVals(cr, si, rsd, rcd, toFill);
        }
    }

    public static void indexToVals(int radius, int index, int diffSq, int diffCb, Vector3 toFill)
    {
        if (diffSq == 0 || radius == 0) return;
        int layerSize = (2 * radius + 1) * (2 * radius + 1);
        toFill.x = 0;
        toFill.y = 0;
        toFill.z = 0;
        // Fill y
        {
            if (index >= layerSize && index < diffCb - layerSize)
            {
                int temp = (index - layerSize) / (diffSq) + 1;
                temp -= radius;
                temp = Math.min(temp, radius);
                toFill.y = temp;
            }
            else
            {
                if (index > layerSize)
                {
                    toFill.y = -radius;
                }
                else
                {
                    toFill.y = radius;
                }
            }
        }
        // Fill x
        if (!(toFill.y == radius || toFill.y == -radius))
        {

            int temp = (index) % diffSq;
            if (temp < diffSq / 2)
            {
                if (temp <= radius)
                {
                    toFill.x = temp;
                }
                else if (temp > diffSq / 2 - radius)
                {
                    toFill.x = -(temp - (diffSq / 2));
                }
                else toFill.x = radius;
            }
            else if (temp > diffSq / 2)
            {
                temp -= diffSq / 2;
                if (temp <= radius)
                {
                    toFill.x = -temp;
                }
                else if (temp > diffSq / 2 - radius)
                {
                    toFill.x = (temp - (diffSq / 2));
                }
                else toFill.x = -radius;
            }
        }
        else
        {
            int temp = (index % layerSize);
            temp = temp % (2 * radius + 1);
            temp -= radius;
            toFill.x = temp;
        }
        // Fill z
        if (!(toFill.y == radius || toFill.y == -radius))
        {
            int temp = (index) % diffSq;
            temp = (temp + 2 * radius - 1) % diffSq + 1;
            if (temp < diffSq / 2)
            {
                if (temp <= radius)
                {
                    toFill.z = temp;
                }
                else if (temp > diffSq / 2 - radius)
                {
                    toFill.z = -(temp - (diffSq / 2));
                }
                else toFill.z = radius;
            }
            else if (temp > diffSq / 2)
            {
                temp -= diffSq / 2;
                if (temp <= radius)
                {
                    toFill.z = -temp;
                }
                else if (temp > diffSq / 2 - radius)
                {
                    toFill.z = (temp - (diffSq / 2));
                }
                else toFill.z = -radius;
            }
        }
        else
        {
            int temp = (index % layerSize) / (2 * radius + 1);
            temp -= radius;
            toFill.z = temp;
        }
    }

    public static long getVectorLong(Vector3 rHat)
    {

        if (rHat.magSq() > 1000000)
        {
            new Exception().printStackTrace();
        }
        int i = (rHat.intX()) + 0xFFF;
        int j = (rHat.intY()) + 0xFFF;
        int k = (rHat.intZ()) + 0xFFF;

        return i + (j << 12) + (k << 24);
    }

    public static int getVectorInt(Vector3 rHat)
    {

        if (rHat.magSq() > 1000000)
        {
            new Exception().printStackTrace();
        }
        int i = (rHat.intX()) + 512;
        int j = (rHat.intY()) + 512;
        int k = (rHat.intZ()) + 512;

        return i + (j << 10) + (k << 20);
    }

    public static int getVectorInt(int x, int y, int z)
    {

        int i = x + 512;
        int j = y + 512;
        int k = z + 512;

        return i + (j << 10) + (k << 20);
    }

    public static void fillFromInt(int[] toFill, int vec)
    {
        toFill[0] = (vec & 1023) - 512;
        toFill[1] = ((vec >> 10) & 1023) - 512;
        toFill[2] = ((vec >> 20) & 1023) - 512;
    }

    public static double cubeRoot(double num)
    {
        return Math.pow(num, 1/3d);
    }
}
