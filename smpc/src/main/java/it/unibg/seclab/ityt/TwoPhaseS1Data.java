package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class TwoPhaseS1Data {

  private List<DRes<SInt>> xs, ys, coefficients;

  public TwoPhaseS1Data() {}

  public void setXs(List<DRes<SInt>> xs) {
    this.xs = xs;
  }

  public List<DRes<SInt>> getXs() {
    return xs;
  }

  public void setYs(List<DRes<SInt>> ys) {
    this.ys = ys;
  }

  public List<DRes<SInt>> getYs() {
    return ys;
  }

  public void setCoefficients(List<DRes<SInt>> coefficients) {
    this.coefficients = coefficients;
  }

  public List<DRes<SInt>> getCoefficients() {
    return coefficients;
  }

}
