package com.jeequan.jeepay.core.constants;

public enum PayProductInterfaceModeEnum {

    SINGLE(1, "单独"),
    ROUND_ROBIN(2, "轮询");

    private final int code;
    private final String label;

    PayProductInterfaceModeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PayProductInterfaceModeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayProductInterfaceModeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

