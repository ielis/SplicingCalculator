package org.monarchinitiative.splicing.calculate;

import java.util.List;

/**
 * This POJO represents a position-weight matrix (PWM). The PWM attributes are:
 * <ul>
 * <li><b>name</b> - name of the PWM</li>
 * <li><b>matrix</b> - internal representation of PWM values used for scoring of nucleotide sequences</li>
 * </ul>
 */
public class PositionWeightMatrix {


    private String name;

    private List<List<Double>> matrix;


    public PositionWeightMatrix() {
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public List<List<Double>> getMatrix() {
        return matrix;
    }


    public void setMatrix(List<List<Double>> matrix) {
        this.matrix = matrix;
    }

}

