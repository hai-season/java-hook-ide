package org.hai.jhook;

public class AppDemo {
    public static void main(String[] args) throws Exception {
        int i = 0;
        while (true) {
            new AppDemo().loopCalc(i);
            Thread.sleep(1000L);
            i++;
        }
    }

    public void say() {
        System.out.println("Demo hello");
        int i = 1 / 0;
    }

    public void loopCalc(int i) throws Exception {
        System.out.println(calc(i, i));
    }

    public int calc(int first, int second) {
        return first + second;
    }
}
