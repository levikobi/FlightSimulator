package model.interpreter.expressions.operands;

import model.interpreter.expressions.Expression;

public class Num implements Expression {
    private double value;

    public Num(double value) {
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public double calculate() {
        return value;
    }
}
