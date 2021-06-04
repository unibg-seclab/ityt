package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

public class TwoPhaseS2Data {

    private List<DRes<SInt>> coefficients;
    private DRes<SInt> x, y, salt, key, yhat;
    private DRes<SInt> Ck, Cx, Cy, Cshat;
    private DRes<SInt> mimcKey;

    public TwoPhaseS2Data() {}

    public void setCoefficients(List<DRes<SInt>> coefficients) {
	this.coefficients = coefficients;
    }

    public List<DRes<SInt>> getCoefficients() {
	return coefficients;
    }

    public void setX(DRes<SInt> x) {
	this.x = x;
    }

    public DRes<SInt> getX() {
	return this.x;
    }

    public void setY(DRes<SInt> y) {
	this.y = y;
    }

    public DRes<SInt> getY() {
	return this.y;
    }

    public void setSalt(DRes<SInt> salt) {
	this.salt = salt;
    }

    public DRes<SInt> getSalt() {
	return this.salt;
    }

    public void setYHat(DRes<SInt> yhat) {
	this.yhat = yhat;
    }

    public DRes<SInt> getYHat() {
	return this.yhat;
    }

    public void setKey(DRes<SInt> key) {
	this.key = key;
    }

    public DRes<SInt> getKey() {
	return this.key;
    }

    public void setCk(DRes<SInt> Ck) {
	this.Ck = Ck;
    }

    public DRes<SInt> getCk() {
	return this.Ck;
    }

    public void setCx(DRes<SInt> Cx) {
	this.Cx = Cx;
    }

    public DRes<SInt> getCx() {
	return this.Cx;
    }

    public void setCy(DRes<SInt> Cy) {
	this.Cy = Cy;
    }

    public DRes<SInt> getCy() {
	return this.Cy;
    }

    public void setCshat(DRes<SInt> Cshat) {
	this.Cshat = Cshat;
    }

    public DRes<SInt> getCshat() {
	return this.Cshat;
    }

    public void setMimcKey(DRes<SInt> mimcKey) {
	this.mimcKey = mimcKey;
    }

    public DRes<SInt> getMimcKey() {
	return this.mimcKey;
    }

}
