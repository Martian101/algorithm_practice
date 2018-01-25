import java.util.ArrayList;
import java.util.List;

/**
 * 给定数组S，S中0表示绿灯，1表示红灯。求一个区间R[a, b)，当区间R内的红绿灯反转时，S中的绿灯数目最多。
 * 如S [0, 1, 1, 0]
 * R = [1, 3)内的红绿灯反转时绿灯最多
 */
public class SolveBulb {
    private int[] bulb;

    public static void main(String[] args) {
        int[] bulb = {0, 1, 1, 0, 1, 0, 1, 1};
        System.out.println(new SolveBulb(bulb).MostRedBulb());
    }

    public SolveBulb(int[] bulb) {
        this.bulb = bulb;
    }

    private Range MostRedBulb() {
        if(bulb == null || bulb.length == 0) {
            return new Range(-1, 0);
        }

        List<Range> rangeList = new ArrayList<Range>();
        int left = -1;
        for (int i = 0; i< bulb.length;i++) {   //第一步，取出范围
            if (bulb[i] == 0 && left == -1) {
                continue;
            }

            if(bulb[i] == 1 && left == -1) {
                left = i;
            }

            if(bulb[i] == 0 && left != -1) {
                rangeList.add(new Range(left, i));
                left = -1;
            }

            if((i + 1) == bulb.length && left != -1) {
                rangeList.add(new Range(left, bulb.length));
            }
        }

        if(rangeList.size() == 0) {
            return new Range(-1, 0);
        }

        for (int i = 1; i < rangeList.size(); i++) {    //第二步，根据第一步取出的范围贪心处理
            Range range = rangeList.get(i);
            Range rangeBefore = rangeList.get(i - 1);
            int value = rangeBefore.getSize() + range.getSize() - (range.begin - rangeBefore.end);
            if(value <= 0) {
                continue;
            }

            if(value >= range.getSize() && value >= rangeBefore.getSize()) {
                range.setBegin(rangeBefore.getBegin());
            }
        }

        Range maxRange = new Range(0, 0);
        for(int i = 0;i < rangeList.size(); i++) {  //第三步，给出贪心解
            if(rangeList.get(i).getSize() > maxRange.getSize()) {
                maxRange = rangeList.get(i);
            }
        }

       return maxRange;
    }

    private static class Range {
        private int begin;
        private int end;

        public Range(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        public int getSize() {
            return this.end - this.begin;
        }

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "begin=" + begin +
                    ", end=" + end +
                    '}';
        }
    }
}
