package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.List;

public class SinglePhaseData {

    private List<DRes<SInt>> coeffs;
    private List<DRes<SInt>> salts;
    private List<DRes<SInt>> xs, ys, xCs, yCs;
    private DRes<SInt> Ck;
    private DRes<SInt> key;
    private SInt n, k;

    public SinglePhaseData() {}

    public void setCoeffs(List<DRes<SInt>> coeffs) {
	this.coeffs = coeffs;
	this.key = coeffs.get(0);
    }

    public List<DRes<SInt>> getCoeffs(){
	return this.coeffs;
    }

    public void setSalts(List<DRes<SInt>> salts) {
	this.salts = salts;
    }

    public List<DRes<SInt>> getSalts(){
	return this.salts;
    }

    public void setXs(List<DRes<SInt>> xs) {
	this.xs = xs;
    }

    public List<DRes<SInt>> getXs(){
	return this.xs;
    }

    public void setYs(List<DRes<SInt>> ys) {
	this.ys = ys;
    }

    public List<DRes<SInt>> getYs(){
	return this.ys;
    }

    public void setXCs(List<DRes<SInt>> xCs) {
	this.xCs = xCs;
    }

    public List<DRes<SInt>> getXCs(){
	return this.xCs;
    }

    public void setYCs(List<DRes<SInt>> yCs) {
	this.yCs = yCs;
    }

    public List<DRes<SInt>> getYCs(){
	return this.yCs;
    }
    
    public DRes<SInt> getCk() {
      return this.Ck;
    }

    public void setCk(DRes<SInt> Ck) {
	this.Ck = Ck;
    }

    public SInt getK(){
	return this.k;
    }

    public SInt getN(){
	return this.n;
    }

    public void setN(SInt n){
	this.n = n;
    }
    
    public void setKey(DRes<SInt> key){
	this.key = key;
    }
    
    public DRes<SInt> getKey(){
	return this.key;
    }
    
}
