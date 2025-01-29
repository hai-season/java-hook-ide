package org.hai.jhook;

public class Demo {
    public static void main(String[] args) throws Exception {
        new Demo().loopCalc();
    }

    public void say() {
        System.out.println("Demo hello");
        int i = 1/0;
    }

    public void loopCalc() throws Exception {
        int i = 0;
        while (true) {
            System.out.println(calc(i, i));
            i++;
            Thread.sleep(1000L);
        }
    }

    public int calc(int first, int second) {
        return first + second;
    }
}
