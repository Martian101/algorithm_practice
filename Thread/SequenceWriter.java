package test;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zqzhao5 on 2017/8/12.
 */
public class SequenceWriter {
    private static volatile StringBuilder buff1 = new StringBuilder("t3"); //占位，并不实际输出到文件
    private static volatile StringBuilder buff2 = new StringBuilder("t1");
    private static volatile StringBuilder buff3 = new StringBuilder("t2");
    private static List<StringBuilder> buffList = Arrays.asList(buff1, buff2, buff3);
    private static int runTotal = 10000;

    private static Lock lock = new ReentrantLock();
    private static Condition t1Sig = lock.newCondition();
    private static Condition t2Sig = lock.newCondition();
    private static Condition t3Sig = lock.newCondition();
    private static Condition plusSig = lock.newCondition();


    public static void main(String[] args) {
        new Thread(new T1()).start();
        new Thread(new T2()).start();
        new Thread(new T3()).start();
        new Thread(new Plus()).start();
        new Thread(new Printer()).start();
    }

    static class T1 implements Runnable {
        @Override
        public void run() {
            for (int m = 0; m < runTotal; m++) {
                lock.lock();
                try {
                    for (StringBuilder builder : buffList) {
                        if ((builder.lastIndexOf("+") == builder.length() - 1) && (builder.lastIndexOf("3") == builder.length() - 2)) {
                            builder.append("t1");
                        }
                    }
                    t2Sig.signal();
                    t1Sig.await();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    static class T2 implements Runnable {
        @Override
        public void run() {
            for (int m = 0; m < runTotal; m++) {
                lock.lock();
                    try {
                        for (StringBuilder builder : buffList) {
                            if((builder.lastIndexOf("+") == builder.length() - 1) && (builder.lastIndexOf("1") == builder.length() - 2)) {
                                builder.append("t2");
                            }
                        }
                        t3Sig.signal();
                        t2Sig.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                }
            }
        }
    }

    static class T3 implements Runnable {
        @Override
        public void run() {
            for (int m = 0; m < runTotal; m++) {
                lock.lock();
                    try {
                        for (StringBuilder builder : buffList) {
                            if((builder.lastIndexOf("+") == builder.length() - 1) && (builder.lastIndexOf("2") == builder.length() - 2)) {
                                builder.append("t3");
                            }
                        }
                        plusSig.signal();
                        t3Sig.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                }
            }
        }
    }

    static class Plus implements Runnable {
        @Override
        public void run() {
            for (int m = 0; m < runTotal; m++) {
                lock.lock();
                try {
                        for(StringBuilder builder : buffList) {
                            char[] chars = new char[1];
                            builder.getChars(builder.length() - 1, builder.length(), chars, 0);
                            if(chars[0] == '1' || chars[0] == '2' || chars[0] == '3') {
                                builder.append("+");
                            }
                        }
                        t1Sig.signal();
                        plusSig.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    static class Printer implements Runnable {
        List<FileWriter> writers = new ArrayList<>();

        public Printer() {
            for (int i = 1; i < 4; i++) {
                try {
                    writers.add(new FileWriter(new File("D:\\file" + i), true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            while (true) {
                    try {
                        for (int i = 0;i < buffList.size();i++) {
                            StringBuilder buff = buffList.get(i);
                            if(buff.length() > 100) {
                                lock.lock();
                                try {
                                    char[] chars = new char[1];
                                    buff.getChars(buff.length() - 1, buff.length(), chars, 0);
                                    if (chars[0] == (i == 0 ? 51 :(i == 1 ? 49 : (i == 2 ? 50 : -1))) ) { //49 --> ASCII 1
                                        writeFile(writers.get(i), buff.substring(3));
                                        buff.delete(2, buff.length());
                                    }

                                } finally {
                                    lock.unlock();
                                }
                            }
                        }
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        private void writeFile(FileWriter file, String outStr) throws FileNotFoundException {
            try {
                PrintWriter pw = new PrintWriter(file);
                try {
                    pw.append(outStr);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        pw.flush();
                        file.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
