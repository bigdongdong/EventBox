package moudle;


public enum ThreadMode {
    /**
     * 默认线程，接受event时与发送方线程保持一致
     */
    DEFAULT,

    /**
     * 转至主线程
     */
    MAIN,

    /**
     * 新建一个子线程
     */
    NEW_THREAD
}