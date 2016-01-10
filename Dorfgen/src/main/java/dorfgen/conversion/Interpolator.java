package dorfgen.conversion;

public class Interpolator
{

    public static class CubicInterpolator
    {
        public static double getValue(double[] p, double x)
        {
            return p[1] + 0.5 * x * (p[2] - p[0]
                    + x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
        }
    }

    public static class BicubicInterpolator extends CubicInterpolator
    {
        private double[] arr  = new double[4];
        public double getValue(double[][] p, double x, double y)
        {
            arr[0] = getValue(p[0], y);
            arr[1] = getValue(p[1], y);
            arr[2] = getValue(p[2], y);
            arr[3] = getValue(p[3], y);
            return getValue(arr, x);
        }

        public int interpolate(int[][] image, int xAbs, int yAbs, int scale)
        {
            int pixelX = xAbs / scale;
            int pixelY = yAbs / scale;
            double x = (xAbs - scale * pixelX) / (double) scale, y = (yAbs - scale * pixelY) / (double) scale;
            double[][] arr = new double[4][4];
            for (int i = -1; i <= 2; i++)
                for (int k = -1; k <= 2; k++)
                {
                    int locX = pixelX + i;
                    int locY = pixelY + k;
                    if (locX >= 0 && locX < image.length && locY >= 0 && locY < image[0].length)
                    {
                        arr[i + 1][k + 1] = image[locX][locY];
                    }
                    else
                    {
                        arr[i + 1][k + 1] = image[pixelX][pixelY];
                    }
                }
            return (int) Math.round(getValue(arr, x, y));
        }

        public int interpolateBiome(int[][] image, int xAbs, int yAbs, int scale)
        {
            int pixelX = xAbs / scale;
            int pixelY = yAbs / scale;

            if(pixelX>=image.length || pixelY>=image[0].length) return 0;
            
            int val = image[pixelX][pixelY];
            double x = (xAbs - scale * pixelX) / (double) scale, y = (yAbs - scale * pixelY) / (double) scale;

            double max = -1;
            int index = -1;
            int[] biomes = new int[16];
            for (int i = -1; i < 3; i++)
                for (int k = -1; k < 3; k++)
                {
                    int locX = pixelX + i;
                    int locY = pixelY + k;
                    if (locX >= 0 && locX < image.length && locY >= 0 && locY < image[0].length)
                    {
                        biomes[(i + 1) + (k + 1) * 4] = image[locX][locY];
                    }
                }
            for (int n = 0; n < 16; n++)
            {
                int num = biomes[n];
                double[][] arr = new double[4][4];
                for (int i = 0; i < 16; i++)
                {
                    if (biomes[i] == num) arr[i % 4][i / 4] = 10;
                    else arr[i % 4][i / 4] = 0;
                }
                double temp = getValue(arr, x, y);
                if (temp > max)
                {
                    max = temp;
                    index = n;
                }
            }
            if (index >= 0) val = biomes[index];

            return val;
        }
    }

    public static class CachedBicubicInterpolator
    {
        private double a00, a01, a02, a03;
        private double a10, a11, a12, a13;
        private double a20, a21, a22, a23;
        private double a30, a31, a32, a33;

        private double[][] arr = new double[4][4];

        private int     lastX, lastY;
        private int[][] lastArr;

        public int interpolate(double x, double y)
        {
            return (int) Math.round(getValue(x, y));
        }

        public int interpolateHeight(int scale, int xAbs, int yAbs, int[][] image)
        {
            int pixelX = xAbs / scale;
            int pixelY = yAbs / scale;
            if(pixelX>=image.length || pixelY>=image[0].length) return 10;
            updateCoefficients(image, pixelX, pixelY);
            int val = image[pixelX][pixelY];
            double x = (xAbs - scale * pixelX) / (double) scale, y = (yAbs - scale * pixelY) / (double) scale;
            val = interpolate(x, y);
            return val;
        }

        private void updateCoefficients(int[][] image, int pixelX, int pixelY)
        {
            if (image == lastArr && pixelX == lastX && pixelY == lastY) { return; }
            lastArr = image;
            lastX = pixelX;
            lastY = pixelY;
            for (int i = 0; i < 16; i++)
            {
                arr[i % 4][i / 4] = image[pixelX][pixelY];
            }
            for (int i = -1; i <= 2; i++)
                for (int k = -1; k <= 2; k++)
                {
                    int locX = pixelX + i;
                    int locY = pixelY + k;
                    if (locX >= 0 && locX < image.length && locY >= 0 && locY < image[0].length)
                    {
                        arr[i + 1][k + 1] = image[locX][locY];
                    }
                }
            updateCoefficients(arr);
        }

        public void updateCoefficients(double[][] p)
        {
            a00 = p[1][1];
            a01 = -.5 * p[1][0] + .5 * p[1][2];
            a02 = p[1][0] - 2.5 * p[1][1] + 2 * p[1][2] - .5 * p[1][3];
            a03 = -.5 * p[1][0] + 1.5 * p[1][1] - 1.5 * p[1][2] + .5 * p[1][3];
            a10 = -.5 * p[0][1] + .5 * p[2][1];
            a11 = .25 * p[0][0] - .25 * p[0][2] - .25 * p[2][0] + .25 * p[2][2];
            a12 = -.5 * p[0][0] + 1.25 * p[0][1] - p[0][2] + .25 * p[0][3] + .5 * p[2][0] - 1.25 * p[2][1] + p[2][2]
                    - .25 * p[2][3];
            a13 = .25 * p[0][0] - .75 * p[0][1] + .75 * p[0][2] - .25 * p[0][3] - .25 * p[2][0] + .75 * p[2][1]
                    - .75 * p[2][2] + .25 * p[2][3];
            a20 = p[0][1] - 2.5 * p[1][1] + 2 * p[2][1] - .5 * p[3][1];
            a21 = -.5 * p[0][0] + .5 * p[0][2] + 1.25 * p[1][0] - 1.25 * p[1][2] - p[2][0] + p[2][2] + .25 * p[3][0]
                    - .25 * p[3][2];
            a22 = p[0][0] - 2.5 * p[0][1] + 2 * p[0][2] - .5 * p[0][3] - 2.5 * p[1][0] + 6.25 * p[1][1] - 5 * p[1][2]
                    + 1.25 * p[1][3] + 2 * p[2][0] - 5 * p[2][1] + 4 * p[2][2] - p[2][3] - .5 * p[3][0] + 1.25 * p[3][1]
                    - p[3][2] + .25 * p[3][3];
            a23 = -.5 * p[0][0] + 1.5 * p[0][1] - 1.5 * p[0][2] + .5 * p[0][3] + 1.25 * p[1][0] - 3.75 * p[1][1]
                    + 3.75 * p[1][2] - 1.25 * p[1][3] - p[2][0] + 3 * p[2][1] - 3 * p[2][2] + p[2][3] + .25 * p[3][0]
                    - .75 * p[3][1] + .75 * p[3][2] - .25 * p[3][3];
            a30 = -.5 * p[0][1] + 1.5 * p[1][1] - 1.5 * p[2][1] + .5 * p[3][1];
            a31 = .25 * p[0][0] - .25 * p[0][2] - .75 * p[1][0] + .75 * p[1][2] + .75 * p[2][0] - .75 * p[2][2]
                    - .25 * p[3][0] + .25 * p[3][2];
            a32 = -.5 * p[0][0] + 1.25 * p[0][1] - p[0][2] + .25 * p[0][3] + 1.5 * p[1][0] - 3.75 * p[1][1]
                    + 3 * p[1][2] - .75 * p[1][3] - 1.5 * p[2][0] + 3.75 * p[2][1] - 3 * p[2][2] + .75 * p[2][3]
                    + .5 * p[3][0] - 1.25 * p[3][1] + p[3][2] - .25 * p[3][3];
            a33 = .25 * p[0][0] - .75 * p[0][1] + .75 * p[0][2] - .25 * p[0][3] - .75 * p[1][0] + 2.25 * p[1][1]
                    - 2.25 * p[1][2] + .75 * p[1][3] + .75 * p[2][0] - 2.25 * p[2][1] + 2.25 * p[2][2] - .75 * p[2][3]
                    - .25 * p[3][0] + .75 * p[3][1] - .75 * p[3][2] + .25 * p[3][3];
        }

        public double getValue(double x, double y)
        {
            double x2 = x * x;
            double x3 = x2 * x;
            double y2 = y * y;
            double y3 = y2 * y;

            return (a00 + a01 * y + a02 * y2 + a03 * y3) + (a10 + a11 * y + a12 * y2 + a13 * y3) * x
                    + (a20 + a21 * y + a22 * y2 + a23 * y3) * x2 + (a30 + a31 * y + a32 * y2 + a33 * y3) * x3;
        }
    }
}
