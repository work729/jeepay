package com.jeequan.jeepay.core.constants;

public enum PayProductTypeEnum {

    COLLECTION(1, "收款"),
    RECHARGE(2, "充值");

    private final int code;
    private final String label;

    PayProductTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PayProductTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayProductTypeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

