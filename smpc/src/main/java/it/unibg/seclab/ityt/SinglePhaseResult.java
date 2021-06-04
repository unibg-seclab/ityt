package it.unibg.seclab.ityt;

import dk.alexandra.fresco.framework.DRes;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;

public class SinglePhaseResult {

    private List<DRes<BigInteger>> xs, ys, xCs, yCs;
    private DRes<BigInteger> Ck;    
    private int n, k;

    public SinglePhaseResult() {
	this.xCs = this.yCs = null;
    }

    public void setXs(List<DRes<BigInteger>> xs) {
	this.xs = xs;
    }

    public void setYs(List<DRes<BigInteger>> ys) {
	this.ys = ys;
    }

    public void setXCs(List<DRes<BigInteger>> xCs) {
	this.xCs = xCs;
    }

    public void setYCs(List<DRes<BigInteger>> yCs) {
	this.yCs = yCs;
    }

    public void setCk(DRes<BigInteger> Ck){
	this.Ck = Ck;
    }

    public void setN(int n){
	this.n = n;
    }
    
    public void setK(int k){
	this.k = k;
    }

    @Override
    public String toString() {
	List<BigInteger> xcoms = xCs != null ? this.xCs.stream().map(DRes::out).collect(Collectors.toList()) : null;
	List<BigInteger> ycoms = yCs != null ? this.yCs.stream().map(DRes::out).collect(Collectors.toList()) : null;
	List<String> cs = new ArrayList<>(this.n - 1);
	if (xcoms != null && ycoms != null) {
	    for (int i = 0; i < this.n - 1; i++) {
		String s1 = xcoms.get(i) != null ? xcoms.get(i).toString() : null;
		String s2 = ycoms.get(i) != null ? ycoms.get(i).toString() : null;
		String res = (s1 != null && s2 != null) ? s1 + "||" + s2 : "null";
		cs.add(res);
	    }
	} else {
	    cs = IntStream
		.range(0, this.n-1)
		.mapToObj(i -> "null")
		.collect(Collectors.toList());
	}
	
	return "SinglePhaseResult {" +
	    "\n  x: " + this.xs.stream().map(DRes::out).collect(Collectors.toList()) + 
	    "\n  y: " + this.ys.stream().map(DRes::out).collect(Collectors.toList()) +
	    "\n  Cs: " + cs.toString() +
	    "\n  Ck: " + this.Ck.out() +
	    "\n  n: " + this.n +
	    "\n  k: " + this.k +
	    "\n}";
    }
}
