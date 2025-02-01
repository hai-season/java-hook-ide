package org.hai.jhook.bean;

public class Result {
    boolean success;
    String msg;
    Object data;

    public Result(boolean success) {
        this.success = success;
    }

    public static Result success() {
        return new Result(true);
    }

    public static Result fail() {
        return new Result(true);
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }
}
