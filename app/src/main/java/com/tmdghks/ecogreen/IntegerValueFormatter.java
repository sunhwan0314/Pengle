package com.tmdghks.ecogreen;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class IntegerValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.valueOf((int) value);  // 소수점 버리고 정수만 표시
    }
}
