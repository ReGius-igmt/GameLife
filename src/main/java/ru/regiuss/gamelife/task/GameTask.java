package ru.regiuss.gamelife.task;

import java.util.Arrays;

import static ru.regiuss.gamelife.util.Environment.SIZE;

public class GameTask implements Runnable {

    private final int[][] matrix;
    private long delay;
    private boolean alive;
    private int age;

    public GameTask(int[][] matrix) {
        this.matrix = matrix;
    }

    public synchronized int getAge() {
        return age;
    }

    public synchronized long getDelay() {
        return delay;
    }

    public synchronized void setDelay(long delay) {
        this.delay = delay;
    }

    public synchronized boolean isAlive() {
        return alive;
    }

    public synchronized void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void run() {
        alive = true;
        int[][] actions = new int[SIZE*SIZE][3];
        while (isAlive()){
            age++;
            int a = 0;
            synchronized (matrix){
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        int s = 0;
                        for (int k = 0; k < 9; k++) {
                            int x = getCoord(i, k % 3);
                            int y = getCoord(j, k / 3);
                            s += matrix[x][y];
                        }
                        s -= matrix[i][j];
                        if(matrix[i][j] == 0 && s == 3){
                            actions[a][0] = i;
                            actions[a][1] = j;
                            actions[a++][2] = 1;
                        } else if(matrix[i][j] == 1 && s != 2 && s != 3){
                            actions[a][0] = i;
                            actions[a][1] = j;
                            actions[a++][2] = 0;
                        }
                    }
                }
                if(actions.length == 0)break;
                for (int i = 0; i < a; i++) {
                    matrix[actions[i][0]][actions[i][1]] = actions[i][2];
                }
            }
            try {
                Thread.sleep(getDelay());
                //Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getCoord(int j, int k) {
        int y = j - 1 + k;
        if(y >= SIZE) y -= SIZE;
        else if(y < 0) y = SIZE + y;
        return y;
    }
}
