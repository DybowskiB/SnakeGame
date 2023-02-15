public class Result implements Comparable<Result>{

    private int length;
    private int level;

    public Result(){}
    public Result(int length, int level) {
        this.length = length;
        this.level = level;
    }

    public int getLength() {
        return length;
    }

    public int getLevel() {
        return level;
    }

    public void setResult(int length) {
        this.length = length;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int compareTo(Result r)
    {
        if(length > r.getLength()) return 1;
        else if(length == r.getLength() && level > r.getLevel()) return 1;
        else if(length == r.getLength()) return -1;
        else return -1;
    }
}
