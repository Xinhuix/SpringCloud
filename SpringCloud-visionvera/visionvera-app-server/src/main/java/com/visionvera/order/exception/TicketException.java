package com.visionvera.order.exception;

/**
 * Created by admin on 2019/2/27.
 */
public class TicketException extends Exception {
//    public final static String CODE_NULL = "-1";
    public final static String CODE_TICKET_CREATE = "-2";
    public final static String CODE_STAFF_LIST = "-11";
    public final static String CODE_STAFF_GROUP = "-12";
    public final static String CODE_STAFF_GROUP_MEMBERS = "-13";
    public final static String CODE_TICKET_TEMPLATE_LIST = "-14";
    public final static String CODE_TICKET_TEMPLATE_FIELD = "-15";
    public final static String CODE_TICKET_FINISH = "-21";
    public final static String CODE_GET_TICKET_BY_FILTER = "-22";
    public final static String CODE_TICKET_SEARCH = "-23";
    public final static String CODE_TICKET_DETAIL = "-24";
    public final static String CODE_TICKET_APPLY = "-25";
    public final static String CODE_TICKET_TRANSFER = "-26";
    public final static String CODE_TICKET_REPLY = "-27";
    public final static String CODE_TICKET_REOPEN = "-28";
    public final static String CODE_TICKET_MODIFY = "-29";
    public final static String CODE_TICKET_FILTER_LIST = "-30";
    public final static String CODE_TICKET_FILTER_COUNT = "-31";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";
//    public final static String CODE_TICKET_CREATE = "-21";


    private String exceptionCode;
    private int qiyuCode;

    public TicketException(String message) {
        this(message, null);
    }

    public TicketException(String message, String exceptionCode) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public TicketException(String message, String exceptionCode, int qiyuCode) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.qiyuCode = qiyuCode;
    }

    public int getQiyuCode() {
        return qiyuCode;
    }

    public void setQiyuCode(int qiyuCode) {
        this.qiyuCode = qiyuCode;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }
}
