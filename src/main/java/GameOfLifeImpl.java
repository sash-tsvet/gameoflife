import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import static jdk.internal.jshell.tool.Startup.readFile;

/**
 * @author Vasyukevich Andrey
 * @since 12.12.2017
 */
public class GameOfLifeImpl implements GameOfLife {
    int size=0;
    int iterations=0;

    Boolean[][] curGen = new Boolean[0][0];
    Boolean[][] nextGen = new Boolean[0][0];

    @Override
    public List<String> play(String inputFile) {

        try(BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();

            String[] words = line.split(" ");

            size = Integer.parseInt(words[0]);
            iterations = Integer.parseInt(words[1]);
            curGen = new Boolean[size][size];

            int c;
            int pos = 0;
            while ((c = br.read()) != -1) {
                //Since c is an integer, cast it to a char. If it isn't -1, it will be in the correct range of char.
                switch ((char)c) {
                    case '0':
                        curGen[pos/size][pos%size] = false;
                        pos ++;
                        break;
                    case  '1':
                        curGen[pos/size][pos%size] = true;
                        pos ++;
                        break;
                    default:
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialises field for drawing cells
        StdDraw.setCanvasSize(size, size);
        StdDraw.setYscale(0, size);
        StdDraw.setXscale(0, size);
        StdDraw.setPenRadius(0);
        StdDraw.setPenColor(StdDraw.GREEN);

        long start = System.currentTimeMillis();
        long time = System.currentTimeMillis() - start;

        // infinitely draws field
        while (iterations>0) {
            curGen = countNextGen(size, size);
            iterations--;
            StdDraw.show(0);
            StdDraw.clear();
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (curGen[row][col] == true) {
                        StdDraw.point(col, row);
                    }
                }
            }
            StdDraw.show(0);
            System.out.println(System.currentTimeMillis() - time + "ms, iterations left: " + iterations);
            time = System.currentTimeMillis();
        }

        time = System.currentTimeMillis() - start;
        System.out.println(time + "ms");

        List<String> returnvalue = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < size; j++) {
                if (curGen[i][j]) sb.append("1");
                else sb.append("0");
            }
            returnvalue.add(sb.toString());
        }


        return returnvalue;
    }

    int parallelevel = 1;
    public Boolean[][] countNextGen(int rowsNum, int colsNum) {
        nextGen = new Boolean[rowsNum][];
        for(int i = 0; i < rowsNum; i++)
        {
            Boolean[] aMatrix = curGen[i];
            int   aLength = aMatrix.length;
            nextGen[i] = new Boolean[aLength];
            System.arraycopy(aMatrix, 0, nextGen[i], 0, aLength);
        }

        SubUpdateThread[] subUpdaters = new SubUpdateThread[rowsNum/parallelevel + 1];
        for(int i = 0; i < rowsNum/parallelevel + 1; i++){
            subUpdaters[i] = new SubUpdateThread(i);
            subUpdaters[i].start();
        }

        for(int i = 0; i < subUpdaters.length; i++){
            try {
                subUpdaters[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return nextGen;
    }


    public static int countCellNeighbours(Boolean[][] curGen, int rowsNum, int colsNum, int row, int col) {
        int numOfNeighbours = 0;
        if (curGen[((row - 1+rowsNum)%rowsNum)][(col - 1+colsNum)%colsNum]) {
            numOfNeighbours++;
        }
        if (curGen[((row - 1+rowsNum)%rowsNum)][col]) {
            numOfNeighbours++;
        }
        if (curGen[((row - 1+rowsNum)%rowsNum)][(col + 1)%colsNum]) {
            numOfNeighbours++;
        }
        if (curGen[row][(col - 1+colsNum)%colsNum]) {
            numOfNeighbours++;
        }
        if (curGen[row][(col + 1)%colsNum]) {
            numOfNeighbours++;
        }
        if (curGen[((row + 1)%rowsNum)][(col - 1+colsNum)%colsNum]) {
            numOfNeighbours++;
        }
        if (curGen[((row + 1)%rowsNum)][col]) {
            numOfNeighbours++;
        }
        if (curGen[((row + 1)%rowsNum)][(col + 1)%colsNum]) {
            numOfNeighbours++;
        }

        return numOfNeighbours;
    }

    private class SubUpdateThread extends Thread{
        private int iNum;

        public SubUpdateThread(int iNum){
            this.iNum = iNum;
        }

        @Override
        public void run() {

            for (int row = iNum*parallelevel; (row < (iNum+1)*parallelevel)&&(row<size); row++) {
                for (int col = 0; col < size; col++) {

                    int numOfNeighbours = countCellNeighbours(curGen, size, size, row, col);

                    // under or overpopulation, cell dies
                    if ((numOfNeighbours < 2) || (numOfNeighbours > 3)) {
                        nextGen[row][col] = false;
                    }

                    // cell lives on to next generation
                    if (numOfNeighbours == 2) {
                        nextGen[row][col] = curGen[row][col];
                    }

                    // cell either stays alive, or is born
                    if (numOfNeighbours == 3) {
                        nextGen[row][col] = true;
                    }
                }
            }
        }
    }
}
